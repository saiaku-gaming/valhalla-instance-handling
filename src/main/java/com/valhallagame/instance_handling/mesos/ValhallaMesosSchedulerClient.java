package com.valhallagame.instance_handling.mesos;

import static java.util.stream.Collectors.groupingBy;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.mesos.v1.Protos;
import org.apache.mesos.v1.Protos.AgentID;
import org.apache.mesos.v1.Protos.ContainerInfo.DockerInfo.PortMapping.Builder;
import org.apache.mesos.v1.Protos.InverseOffer;
import org.apache.mesos.v1.Protos.Offer;
import org.apache.mesos.v1.Protos.Offer.Operation;
import org.apache.mesos.v1.Protos.OfferID;
import org.apache.mesos.v1.Protos.Resource;
import org.apache.mesos.v1.Protos.TaskID;
import org.apache.mesos.v1.Protos.TaskInfo;
import org.apache.mesos.v1.Protos.TaskStatus;
import org.apache.mesos.v1.scheduler.Protos.Event.Failure;
import org.apache.mesos.v1.scheduler.Protos.Event.Message;
import org.apache.mesos.v1.scheduler.Protos.Event.Subscribed;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.valhallagame.instance_handling.handlers.InstanceHandler;
import com.valhallagame.instance_handling.handlers.MesosHandler;
import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.mesos.scheduler_client.MesosSchedulerClient;

@Service
public class ValhallaMesosSchedulerClient extends MesosSchedulerClient {

	private static final Logger log = LoggerFactory.getLogger(ValhallaMesosSchedulerClient.class);

	private static final double CPUS_PER_INSTANCE = 0;

	private static final double MB_RAM_PER_INSTANCE = 0;

	private List<InstanceAdd> instanceQueue = Collections.synchronizedList(new ArrayList<InstanceAdd>());
	
	private MesosHandler mesosHandler;
	private InstanceHandler instanceHandler;

	public ValhallaMesosSchedulerClient(MesosHandler mesosHandler, InstanceHandler instanceHandler, double failoverTimeout) {
		this.mesosHandler = mesosHandler;
		this.instanceHandler = instanceHandler;
		
		try {
			subscribe(new URL("http://mesos-master.valhalla-game.com:5050/api/v1/scheduler"), failoverTimeout,
					"Valhalla", mesosHandler.getLatestValidFrameworkId(failoverTimeout));
		} catch (MalformedURLException | URISyntaxException e) {
			log.error("fuck", e);
		}
	}

	public void queueInstance(InstanceAdd instanceAdd) {
		this.instanceQueue.add(instanceAdd);
	}

	@Override
	public void receivedSubscribed(Subscribed subscribed) {
		mesosHandler.insertFrameworkId(subscribed.getFrameworkId());
		log.info("Whoho, I am subscribed on framework id: " + subscribed.getFrameworkId());
	}

	public void kill(String taskId) {
		this.kill(TaskID.newBuilder().setValue(taskId).build());
	}

	@Override
	public void receivedOffers(List<Offer> offers) {

		final List<OfferID> acceptedOffers = new ArrayList<>();
		final List<TaskInfo> tasksInfos = new ArrayList<>();
		final List<OfferID> declinedOffers = new ArrayList<>();

		for (Offer offer : offers) {
			final OfferID offerId = offer.getId();

			// Gets a task if needed and offer contains enough resources.
			Optional<TaskInfo> taskInfo = getTask(offer);

			if (taskInfo.isPresent()) {
				acceptedOffers.add(offerId);
				tasksInfos.add(taskInfo.get());
			} else {
				declinedOffers.add(offerId);
			}
		}

		if (!acceptedOffers.isEmpty()) {
			// Launch all tasks directly
			Operation op = Offer.Operation.newBuilder().setType(Offer.Operation.Type.LAUNCH)
					.setLaunch(Offer.Operation.Launch.newBuilder().addAllTaskInfos(tasksInfos)).build();

			accept(acceptedOffers, Arrays.asList(op));
		}

		if (!declinedOffers.isEmpty()) {
			decline(declinedOffers);
		}
	}

	@Override
	public void receivedInverseOffers(List<InverseOffer> offers) {
		// we dont care, we have already tried to respond to offer
		List<String> s = offers.stream().map(f -> f.toString()).collect(Collectors.toList());
		log.info("Rescind InverseOffer " + String.join(", ", s));
	}

	@Override
	public void receivedRescind(OfferID offerId) {
		// we dont care, we have already tried to respond to offer
		log.info("Rescind Offer " + offerId);
	}

	@Override
	public void receivedRescindInverseOffer(OfferID offerId) {
		// we dont care, we have already tried to respond to offer
		log.info("Rescind Inverse Offer " + offerId);
	}

	@Override
	public void receivedUpdate(TaskStatus update) {
		instanceHandler.updateTaskState(update.getTaskId().toString(), update.getState().name());
		log.info(update.toString());
	}

	@Override
	public void receivedMessage(Message message) {
		log.info("Received message: " + message);
	}

	@Override
	public void receivedFailure(Failure failure) {
		log.error(failure.toString());
	}

	@Override
	public void receivedError(String message) {
		log.error(message);
	}

	@Override
	public void receivedHeartbeat() {
		log.debug("Heartbead received.");
	}

	/**
	 * Returns a valhalla task if there is enough resources and there is a
	 * instance queued up.
	 */
	private Optional<TaskInfo> getTask(Offer offer) {

		final AgentID agentId = offer.getAgentId();
		final Map<String, List<Resource>> resources = offer.getResourcesList().stream()
				.collect(groupingBy(Resource::getName));

		final List<Resource> cpuList = resources.get("cpus");
		final List<Resource> memList = resources.get("mem");
		final List<Resource> ports = resources.get("ports");

		if (cpuList != null && !cpuList.isEmpty() && memList != null && !memList.isEmpty()
				&& cpuList.size() == memList.size() && instanceQueue.size() > 0) {

			final Resource cpus = cpuList.get(0);
			final Resource mem = memList.get(0);
			int port = ports.get(0).getRanges().getRangeList().stream().findAny().map(range -> range.getBegin())
					.orElse(0L).intValue();
			double availableCpu = cpus.getScalar().getValue();
			double availableMem = mem.getScalar().getValue();
			if (availableCpu >= CPUS_PER_INSTANCE && availableMem >= MB_RAM_PER_INSTANCE && port != 0) {
				InstanceAdd instanceAdd = instanceQueue.remove(0);
				return Optional.of(createValhallaTaskInfo(agentId, instanceAdd, port));
			}
		}
		return Optional.empty();
	}

	/**
	 * Creates a task based on instance data that is used to tell mesos what to
	 * run.
	 */
	private static TaskInfo createValhallaTaskInfo(final AgentID agentId, final InstanceAdd instanceAdd,
			final int portNumber) {

		// generate a unique task ID
		Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(UUID.randomUUID().toString()).build();

		log.info("Launching task {}", taskId.getValue());

		// docker image info
		Protos.ContainerInfo.DockerInfo.Builder dockerInfoBuilder = Protos.ContainerInfo.DockerInfo.newBuilder();
		dockerInfoBuilder.setImage("phrozen/valhalla-linux-server:" + instanceAdd.getVersion());
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
		// its instance id and where to call home. This is run on the docker
		// container as soon as it starts.
		String pre = String
				.format("sed -i 's/DEVELOPMENT_INSTANCE/%s/g' /opt/unreal-server/valhalla/Config/DefaultGame.ini && "
						+ " sed -i 's/SERVER_SECRET/%s/g' /opt/unreal-server/valhalla/Config/DefaultGame.ini && "
						+ " sed -i 's/persistent.valhalla-game.com/%s/g' /opt/unreal-server/valhalla/Config/DefaultGame.ini ",

						instanceAdd.getInstanceId(), System.getProperty("valhalla.server.secret"),
						instanceAdd.getPersistentServerUrl());

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
						Protos.CommandInfo.newBuilder().setShell(false).setValue(instanceAdd.getLevel()).addArguments(pre))
				.build();
	}

}
