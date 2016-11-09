package com.valhallagame.instance_handling;

import static com.mesosphere.mesos.rx.java.SinkOperations.sink;
import static com.mesosphere.mesos.rx.java.protobuf.SchedulerCalls.decline;
import static com.mesosphere.mesos.rx.java.protobuf.SchedulerCalls.subscribe;
import static com.mesosphere.mesos.rx.java.util.UserAgentEntries.literal;
import static java.util.stream.Collectors.groupingBy;
import static rx.Observable.from;
import static rx.Observable.just;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.mesos.v1.Protos;
import org.apache.mesos.v1.Protos.AgentID;
import org.apache.mesos.v1.Protos.ContainerInfo.DockerInfo.PortMapping.Builder;
import org.apache.mesos.v1.Protos.FrameworkID;
import org.apache.mesos.v1.Protos.FrameworkInfo;
import org.apache.mesos.v1.Protos.Offer;
import org.apache.mesos.v1.Protos.OfferID;
import org.apache.mesos.v1.Protos.Resource;
import org.apache.mesos.v1.Protos.TaskID;
import org.apache.mesos.v1.Protos.TaskInfo;
import org.apache.mesos.v1.Protos.TaskState;
import org.apache.mesos.v1.Protos.TaskStatus;
import org.apache.mesos.v1.scheduler.Protos.Call;
import org.apache.mesos.v1.scheduler.Protos.Event;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mesosphere.mesos.rx.java.AwaitableSubscription;
import com.mesosphere.mesos.rx.java.MesosClient;
import com.mesosphere.mesos.rx.java.MesosClientBuilder;
import com.mesosphere.mesos.rx.java.SinkOperation;
import com.mesosphere.mesos.rx.java.SinkOperations;
import com.mesosphere.mesos.rx.java.protobuf.ProtoUtils;
import com.mesosphere.mesos.rx.java.protobuf.ProtobufMesosClientBuilder;
import com.mesosphere.mesos.rx.java.protobuf.SchedulerCalls;

import rx.Observable;

public class MesosScheduler implements Closeable {

	public static final String MESOS_MASTER = "http://mesos-master.valhalla-game.com:5050";
	private static final String MESOS_MASTER_SCHEDULER = MESOS_MASTER + "/api/v1/scheduler";
	private static final URI MESOS_MASTER_S_URI = URI.create(MESOS_MASTER_SCHEDULER);
	private static final String FRAMEWORK = "valhalla1";
	private static final double CPUS_PER_INSTANCE = 1;
	private static final String MESOS_ROLE = "*";
	private static final double MB_RAM_PER_INSTANCE = 240;

	private static final Logger log = LoggerFactory.getLogger(MesosScheduler.class);

	private List<TestInstance> instanceQueue = Collections.synchronizedList(new ArrayList<TestInstance>());
	private AwaitableSubscription openStream;
	private Thread subscriberThread;

	public void connect() {
		final FrameworkID frameworkID = FrameworkID.newBuilder().setValue(FRAMEWORK + System.currentTimeMillis())
				.build();

		final State<FrameworkID, TaskID, TaskState> stateObject = new State<>(frameworkID, MESOS_ROLE,
				CPUS_PER_INSTANCE, MB_RAM_PER_INSTANCE);

		final MesosClientBuilder<Call, Event> clientBuilder = ProtobufMesosClientBuilder.schedulerUsingProtos()
				.mesosUri(MESOS_MASTER_S_URI).applicationUserAgentEntry(literal("na", "na"));

		FrameworkInfo frameworkInfo = Protos.FrameworkInfo.newBuilder().setId(stateObject.getFwId())
				.setUser(Optional.ofNullable(System.getenv("user")).orElse("root")) // https://issues.apache.org/jira/browse/MESOS-3747
				.setName(FRAMEWORK).setFailoverTimeout(0).setRole(stateObject.getResourceRole()).build();

		final Call subscribeCall = subscribe(stateObject.getFwId(), frameworkInfo);

		final Observable<State<FrameworkID, TaskID, TaskState>> stateObservable = just(stateObject).repeat();

		clientBuilder.subscribe(subscribeCall).processStream(unicastEvents -> {
			final Observable<Event> events = unicastEvents.share();

			final Observable<Optional<SinkOperation<Call>>> offerEvaluations = events
					.filter(event -> event.getType() == Event.Type.OFFERS)
					.flatMap(event -> from(event.getOffers().getOffersList())).zipWith(stateObservable, Pair::create)
					.map(this::handleOffer).map(Optional::of);

			final Observable<Optional<SinkOperation<Call>>> updateStatusAck = events
					.filter(event -> event.getType() == Event.Type.UPDATE && event.getUpdate().getStatus().hasUuid())
					.zipWith(stateObservable, Pair::create)
					.doOnNext((Pair<Event, State<FrameworkID, TaskID, TaskState>> t) -> {
						final Event event = t._1;
						final State<FrameworkID, TaskID, TaskState> state = t._2;
						final TaskStatus status = event.getUpdate().getStatus();
						state.put(status.getTaskId(), status.getState());
					}).map((Pair<Event, State<FrameworkID, TaskID, TaskState>> t) -> {
						final TaskStatus status = t._1.getUpdate().getStatus();
						return SchedulerCalls.ackUpdate(t._2.getFwId(), status.getUuid(), status.getAgentId(),
								status.getTaskId());
					}).map(SinkOperations::create).map(Optional::of);

			final Observable<Optional<SinkOperation<Call>>> errorLogger = events
					.filter(event -> event.getType() == Event.Type.ERROR || (event.getType() == Event.Type.UPDATE
							&& event.getUpdate().getStatus().getState() == TaskState.TASK_ERROR))
					.doOnNext(e -> log.warn("Task Error: {}", ProtoUtils.protoToString(e))).map(e -> Optional.empty());

			return offerEvaluations.mergeWith(updateStatusAck).mergeWith(errorLogger);
		});

		try {
			MesosClient<Call, Event> client = clientBuilder.build();
			openStream = client.openStream();
			openStream.await();
		} catch (Throwable e) {
			log.error("Error in valhalla framework!", e);
		}
	}

	private SinkOperation<Call> handleOffer(final Pair<Offer, State<FrameworkID, TaskID, TaskState>> t) {
		final Offer offer = t._1;
		final State<FrameworkID, TaskID, TaskState> state = t._2;
		final FrameworkID frameworkId = state.getFwId();
		final AgentID agentId = offer.getAgentId();
		final List<OfferID> ids = Arrays.asList(offer.getId());
		final Map<String, List<Resource>> resources = offer.getResourcesList().stream()
				.collect(groupingBy(Resource::getName));
		final List<Resource> cpuList = resources.get("cpus");
		final List<Resource> memList = resources.get("mem");
		final List<Resource> ports = resources.get("ports");

		if (cpuList != null && !cpuList.isEmpty() && memList != null && !memList.isEmpty()
				&& cpuList.size() == memList.size()) {
			final List<TaskInfo> tasks = new ArrayList<>();
			if (cpuList.size() > 0 && instanceQueue.size() > 0) {
				final Resource cpus = cpuList.get(0);
				final Resource mem = memList.get(0);
				int port = (int) ports.get(0).getRanges().getRangeList().stream().findAny()
						.map(range -> range.getBegin()).orElse(0l).longValue();
				double availableCpu = cpus.getScalar().getValue();
				double availableMem = mem.getScalar().getValue();
				final double cpusPerTask = state.getCpusPerTask();
				final double memMbPerTask = state.getMemMbPerTask();
				if (availableCpu >= cpusPerTask && availableMem >= memMbPerTask && port != 0l) {
					TestInstance instance = instanceQueue.remove(0);
					tasks.add(runValhallaInstance(agentId, instance, cpus.getRole(), cpusPerTask, mem.getRole(),
							memMbPerTask, port));
				}

			}

			if (!tasks.isEmpty()) {
				log.info("Launching {} tasks", tasks.size());
				return sink(runTasks(frameworkId, ids, tasks),
						() -> tasks.forEach(task -> state.put(task.getTaskId(), TaskState.TASK_STAGING)),
						(e) -> log.warn("", e));
			} else {
				return sink(decline(frameworkId, ids));
			}
		} else {
			return sink(decline(frameworkId, ids));
		}
	}

	private static Call runTasks(@NotNull final FrameworkID frameworkId, @NotNull final List<OfferID> offerIds,
			@NotNull final List<TaskInfo> tasks) {
		return Call.newBuilder().setFrameworkId(frameworkId).setType(Call.Type.ACCEPT)
				.setAccept(
						Call.Accept.newBuilder().addAllOfferIds(offerIds)
								.addOperations(Offer.Operation.newBuilder().setType(Offer.Operation.Type.LAUNCH)
										.setLaunch(Offer.Operation.Launch.newBuilder().addAllTaskInfos(tasks))))
				.build();
	}

	private TaskInfo runValhallaInstance(final AgentID agentId, final TestInstance instance, final String cpusRole,
			final double cpus, final String memRole, final double mem, final int portNumber) {

		// generate a unique task ID
		Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(instance.getTaskId()).build();

		log.info("Launching task {}", taskId.getValue());

		// docker image info
		Protos.ContainerInfo.DockerInfo.Builder dockerInfoBuilder = Protos.ContainerInfo.DockerInfo.newBuilder();
		dockerInfoBuilder.setImage("phrozen/valhalla-linux-server:" + instance.getVersion());
		dockerInfoBuilder.setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE);

		dockerInfoBuilder.addParameters(Protos.Parameter.newBuilder().setKey("rm").setValue("true").build());
		dockerInfoBuilder
				.addParameters(Protos.Parameter.newBuilder().setKey("oom-kill-disable").setValue("true").build());

		Builder portMappingBuilder = Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder();
		portMappingBuilder.setHostPort(portNumber);
		portMappingBuilder.setContainerPort(7777);
		portMappingBuilder.setProtocol("udp");
		dockerInfoBuilder.addPortMappings(portMappingBuilder.build());

		// container info
		Protos.ContainerInfo.Builder containerInfoBuilder = Protos.ContainerInfo.newBuilder();
		containerInfoBuilder.setType(Protos.ContainerInfo.Type.DOCKER);
		containerInfoBuilder.setDocker(dockerInfoBuilder.build());

		// Will replace a file on the instance so that the instance know
		// its instance id.
		String pre = String
				.format("sed -i 's/DEVELOPMENT_INSTANCE/%s/g' /opt/unreal-server/valhalla/Config/DefaultGame.ini && "
						+ " sed -i 's/SERVER_SECRET/%s/g' /opt/unreal-server/valhalla/Config/DefaultGame.ini && "
						+ " sed -i 's/persistent.valhalla-game.com/%s/g' /opt/unreal-server/valhalla/Config/DefaultGame.ini ",

						instance.getId(), System.getProperty("valhalla.server.secret"),
						instance.getPersistentServerUrl());
		log.info("pre: " + pre);

		// create task to run
		return Protos.TaskInfo.newBuilder().setName("task " + taskId.getValue()).setTaskId(taskId).setAgentId(agentId)
				.addResources(Protos.Resource.newBuilder().setName("cpus").setType(Protos.Value.Type.SCALAR)
						.setScalar(Protos.Value.Scalar.newBuilder().setValue(CPUS_PER_INSTANCE)))
				.addResources(Protos.Resource.newBuilder().setName("mem").setType(Protos.Value.Type.SCALAR)
						.setScalar(Protos.Value.Scalar.newBuilder().setValue(MB_RAM_PER_INSTANCE)))
				.addResources(Protos.Resource.newBuilder().setName("ports").setType(Protos.Value.Type.RANGES)
						.setRanges(Protos.Value.Ranges.newBuilder()
								.addRange(Protos.Value.Range.newBuilder().setBegin(portNumber).setEnd(portNumber))))
				.setContainer(containerInfoBuilder)
				.setCommand(
						Protos.CommandInfo.newBuilder().setShell(false).setValue(instance.getLevel()).addArguments(pre))
				.build();
	}

	public void queueInstance(TestInstance ins) {
		this.instanceQueue.add(ins);
		if (openStream == null || openStream.isUnsubscribed()) {

			// Donno if this can happen, lets stay safe.
			if (subscriberThread != null) {
				subscriberThread.interrupt();
			}

			subscriberThread = new Thread() {
				@Override
				public void run() {
					connect();
				}
			};
			subscriberThread.start();
		}
	}

	@Override
	public void close() throws IOException {
		if (openStream != null) {
			if (!openStream.isUnsubscribed()) {
				openStream.unsubscribe();
			}
		}
	}

}
