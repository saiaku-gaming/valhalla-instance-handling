package com.valhallagame.instance_handling.mesos;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.protobuf.AbstractMessage;
import com.squareup.okhttp.*;
import com.squareup.okhttp.Request;
import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.instance_handling.messages.InstanceUpdate;
import com.valhallagame.instance_handling.model.MesosFramework;
import com.valhallagame.instance_handling.service.MesosService;
import com.valhallagame.instance_handling.service.TaskService;
import com.valhallagame.mesos.scheduler_client.MesosSchedulerClient;
import org.apache.mesos.v1.Protos;
import org.apache.mesos.v1.Protos.*;
import org.apache.mesos.v1.Protos.ContainerInfo.DockerInfo.PortMapping.Builder;
import org.apache.mesos.v1.Protos.Offer.Operation;
import org.apache.mesos.v1.scheduler.Protos.Event.Failure;
import org.apache.mesos.v1.scheduler.Protos.Event.Message;
import org.apache.mesos.v1.scheduler.Protos.Event.Subscribed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ValhallaMesosSchedulerClient extends MesosSchedulerClient {

    private static final Logger log = LoggerFactory.getLogger(ValhallaMesosSchedulerClient.class);

    private static final double CPUS_PER_INSTANCE = 0.3D;

    private static final double MB_RAM_PER_INSTANCE = 10.0D;

    private List<InstanceAdd> instanceQueue = Collections.synchronizedList(new ArrayList<InstanceAdd>());

    @Autowired
    private TaskService taskService;

    @Autowired
    private MesosService mesosService;

    @Value("${mesos.failover-timeout}")
    private double failoverTimeout;

    private ObjectMapper mapper = new ObjectMapper();
    private URL slaveUrl;
    private URL taskUrl;
    private FrameworkID frameworkId;
    private String persistentBaseUrl;

    @PostConstruct
    public void init() {
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        try {
            this.slaveUrl = new URL(System.getProperty("mesos-master-url", "http://mesos-master.valhalla-game.com:5050")
                    + "/master/slaves");
            this.taskUrl = new URL(System.getProperty("mesos-master-url", "http://mesos-master.valhalla-game.com:5050")
                    + "/master/tasks");
        } catch (MalformedURLException e) {
            log.error("dang it", e);
        }

        persistentBaseUrl = System.getProperties().getProperty("persistent-url", "http://localhost:1234");

        Optional<MesosFramework> mesosFrameworkOpt = mesosService.getLatestValidFramework(failoverTimeout);

        try {
            subscribe(new URL(System.getProperty("mesos-master-url", "http://mesos-master.valhalla-game.com:5050")
                            + "/api/v1/scheduler"), failoverTimeout,
                    "Valhalla", mesosFrameworkOpt.map(MesosFramework::getId).orElse(null));
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("fuck", e);
        }
    }

    public void queueInstance(InstanceAdd instanceAdd) {
        this.instanceQueue.add(instanceAdd);
    }

    @Override
    public void receivedSubscribed(Subscribed subscribed) {
        MesosFramework mesosFramework = mesosService.getMesosFramework(subscribed.getFrameworkId())
                .orElse(new MesosFramework(subscribed.getFrameworkId().getValue()));
        mesosService.save(mesosFramework);
        frameworkId = subscribed.getFrameworkId();
        log.info("Whoho, I am subscribed on framework id: " + subscribed.getFrameworkId());
    }

    public void kill(String taskId) {
        log.info("Killing " + taskId);
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

            accept(acceptedOffers, Collections.singletonList(op));
        }

        if (!declinedOffers.isEmpty()) {
            decline(declinedOffers);
        }
    }

    @Override
    public void receivedInverseOffers(List<InverseOffer> offers) {
        // we dont care, we have already tried to respond to offer
        List<String> s = offers.stream().map(AbstractMessage::toString).collect(Collectors.toList());
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
        Optional<com.valhallagame.instance_handling.model.Task> taskOpt = taskService.getTask(update.getTaskId().getValue());

        if (!taskOpt.isPresent()) {
            log.error("receivedUpdate: could not find task with id " + update.getTaskId().getValue());
            return;
        }

        taskOpt.get().setTaskState(update.getState().name());
        taskService.save(taskOpt.get());
        Optional<MesosFramework> mesosFrameworkOpt = mesosService.getMesosFramework(frameworkId);
        if (mesosFrameworkOpt.isPresent()) {
            mesosFrameworkOpt.get().setTimestamp(new Date());
            mesosService.save(mesosFrameworkOpt.get());
        } else {
            log.error("receivedUpdate: could not find framework with id " + frameworkId.getValue());
        }
        notifyPersistant(update);
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
        log.debug("Heartbeat received.");
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
            int port = ports.get(0).getRanges().getRangeList().stream().findAny().map(Protos.Value.Range::getBegin)
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
        Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(instanceAdd.getTaskId()).build();

        log.info("Launching task {}", taskId.getValue());

        // docker image info
        Protos.ContainerInfo.DockerInfo.Builder dockerInfoBuilder = Protos.ContainerInfo.DockerInfo.newBuilder();
        dockerInfoBuilder.setImage("phrozen/valhalla-linux-server:" + instanceAdd.getVersion());
        dockerInfoBuilder.setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE);

        dockerInfoBuilder.addParameters(Protos.Parameter.newBuilder().setKey("rm").setValue("true").build());
        dockerInfoBuilder
                .addParameters(Protos.Parameter.newBuilder().setKey("oom-kill-disable").setValue("true").build());

        dockerInfoBuilder.addParameters(Protos.Parameter.newBuilder().setKey("log-driver").setValue("gelf").build());

        dockerInfoBuilder.addParameters(Protos.Parameter.newBuilder().setKey("log-opt").setValue("gelf-address=udp://valhalla-game.com:12201").build());


        Builder portMappingBuilder = Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder();
        portMappingBuilder.setHostPort(portNumber);
        portMappingBuilder.setContainerPort(7777);
        portMappingBuilder.setProtocol("udp");
        dockerInfoBuilder.addPortMappings(portMappingBuilder.build());

        // container info
        Protos.ContainerInfo.Builder containerInfoBuilder = Protos.ContainerInfo.newBuilder();
        containerInfoBuilder.setType(Protos.ContainerInfo.Type.DOCKER);
        containerInfoBuilder.setDocker(dockerInfoBuilder.build());

        //This might be a way to make sure that docker does not limit the ram usage of a container
        RLimitInfo rLimit = RLimitInfo.newBuilder().addRlimits(RLimitInfo.RLimit.newBuilder().clearHard().clearSoft().setType(RLimitInfo.RLimit.Type.valueOf(RLimitInfo.RLimit.Type.RLMT_AS_VALUE))).build();
        containerInfoBuilder.setRlimitInfo(rLimit);

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
                        Protos.CommandInfo.newBuilder().setShell(false).setValue(instanceAdd.getLevel()).addArguments(
                                pre))
                .build();
    }

    private void notifyPersistant(TaskStatus update) {

        Optional<com.valhallagame.instance_handling.model.Task> taskOpt = taskService.getTask(update.getTaskId().getValue());

        if (!taskOpt.isPresent()) {
            log.error("notifyPersistant: could not find task with id " + update.getTaskId().getValue());
            return;
        }


        Integer instanceId = taskOpt.get().getInstanceId();
        Slave slave = getSlave(update.getAgentId().getValue());

        log.info("TaskId is : " + update.getTaskId().getValue());

        Task task = getTask(update.getTaskId().getValue());

        log.info("Got task: " + task );

        InstanceUpdate message = new InstanceUpdate(instanceId, update.getState().name(),
                (slave != null ? slave.hostname : "0.0.0.0"),
                (task != null ? task.container.docker.portMappings.stream().findAny().map(m -> m.hostPort).orElse(-1) : -1));

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        try {
            Request request = new Request.Builder()
                    .url(persistentBaseUrl + "/v1/server-instance-controller/update")
                    .header("session", System.getProperty("valhalla.server.secret"))
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mapper.writeValueAsString(message)))
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                log.error("Something went wrong on instance update to persistent, code: " + response.code());
            }
        } catch (IOException e) {
            log.error("Something when wrong when trying to update persistent");
            e.printStackTrace();
        }
    }

    private Slave getSlave(String agentId) {

        try {

            HttpURLConnection conn = (HttpURLConnection) slaveUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            String data = convertStreamToString(conn.getInputStream());
            Slaves slaves = mapper.readValue(data, Slaves.class);

            conn.disconnect();

            Optional<Slave> slave = slaves.slaves.stream().filter(s -> s.id.equals(agentId)).findFirst();

            return slave.orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Task getTask(String agentId) {

        try {

            HttpURLConnection conn = (HttpURLConnection) taskUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            String data = convertStreamToString(conn.getInputStream());

            log.info("getTask returned" + data);

            Tasks tasks = mapper.readValue(data, Tasks.class);

            conn.disconnect();

            Optional<Task> task = tasks.tasks.stream().filter(t -> t.id.equals(agentId)).findFirst();

            return task.orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }

    /****************************************
     * Everything below is just helper beans
     ****************************************/

    private static class Attributes {

    }

    private static class Slaves {
        List<Slave> slaves;
        List<Slave> recoveredSlaves;

        @Override
        public String toString() {
            return "Slaves [slaves=" + slaves + ", recoveredSlaves=" + recoveredSlaves + "]";
        }
    }

    private static class Scalar {
        double value;

        @Override
        public String toString() {
            return "Scalar [value=" + value + "]";
        }
    }

    private static class Range {
        int begin;
        int end;

        @Override
        public String toString() {
            return "Range [begin=" + begin + ", end=" + end + "]";
        }
    }

    private static class Ranges {
        List<Range> range;

        @Override
        public String toString() {
            return "Ranges [range=" + range + "]";
        }
    }

    private static class ResourcesFull {
        String name;
        String type;
        Scalar scalar;
        String role;
        Ranges ranges;
        AllocationInfo allocationInfo;

        @Override
        public String toString() {
            return "ResourcesFull [name=" + name + ", type=" + type + ", scalar=" + scalar + ", role=" + role
                    + ", ranges=" + ranges + ", allocationInfo=" + allocationInfo + "]";
        }
    }

    private static class AllocationInfo {
        String role;

        @Override
        public String toString() {
            return "AllocationInfo [role=" + role + "]";
        }
    }

    private static class Slave {
        String id;
        String pid;
        String hostname;
        int port;
        double registered_time;
        double reregistered_time;
        Resources resources;
        Resources usedResources;
        Resources offeredResources;
        Resources reservedResources;
        Resources unreservedResources;
        Attributes attributes;
        boolean active;
        String version;
        ResourcesFull reservedResourcesFull;
        List<ResourcesFull> usedResourcesFull;
        List<ResourcesFull> offeredResourcesFull;

        @Override
        public String toString() {
            return "Slave [id=" + id + ", pid=" + pid + ", hostname=" + hostname + ", port=" + port + ", registered_time="
                    + registered_time + ", reregistered_time=" + reregistered_time + ", resources=" + resources
                    + ", usedResources=" + usedResources + ", offeredResources=" + offeredResources
                    + ", reservedResources=" + reservedResources + ", unreservedResources=" + unreservedResources
                    + ", attributes=" + attributes + ", active=" + active + ", version=" + version
                    + ", reservedResourcesFull=" + reservedResourcesFull + ", usedResourcesFull=" + usedResourcesFull
                    + ", offeredResourcesFull=" + offeredResourcesFull + "]";
        }
    }

    private static class Resources {
        int disk;
        int mem;
        int gpus;
        int cpus;
        String ports;

        @Override
        public String toString() {
            return "Resources [disk=" + disk + ", mem=" + mem + ", gpus=" + gpus + ", cpus=" + cpus + ", ports=" + ports
                    + "]";
        }
    }

    private static class Label {
        String key;
        String value;

        @Override
        public String toString() {
            return "Label [key=" + key + ", value=" + value + "]";
        }
    }

    private static class IpAddress {
        String ipAddress;

        @Override
        public String toString() {
            return "IpAddress [ipAddress=" + ipAddress + "]";
        }
    }

    private static class NetworkInfo {
        List<IpAddress> ipAddresses;

        @Override
        public String toString() {
            return "NetworkInfo [ipAddresses=" + ipAddresses + "]";
        }
    }

    private static class ContainerStatus {
        List<NetworkInfo> networkInfos;

        @Override
        public String toString() {
            return "ContainerStatus [networkInfos=" + networkInfos + "]";
        }
    }

    private static class State {
        String state;
        double timestamp;
        List<Label> labels;
        ContainerStatus containerStatus;

        @Override
        public String toString() {
            return "State [state=" + state + ", timestamp=" + timestamp + ", labels=" + labels + ", containerStatus="
                    + containerStatus + "]";
        }
    }

    private static class Parameter {
        String key;
        String value;

        @Override
        public String toString() {
            return "Parameter [key=" + key + ", value=" + value + "]";
        }
    }

    private static class PortMapping {
        int hostPort;
        int containerPort;
        String protocol;

        @Override
        public String toString() {
            return "PortMapping [hostPort=" + hostPort + ", containerPort=" + containerPort + ", protocol=" + protocol
                    + "]";
        }
    }

    private static class Docker {
        String image;
        String network;
        boolean privileged;
        List<Parameter> parameters;
        List<PortMapping> portMappings;
        boolean forcePullImage;

        @Override
        public String toString() {
            return "Docker [image=" + image + ", network=" + network + ", privileged=" + privileged + ", parameters="
                    + parameters + ", portMappings=" + portMappings + ", forcePullImage=" + forcePullImage + "]";
        }
    }

    private static class Container {
        String type;
        Docker docker;

        @Override
        public String toString() {
            return "Container [type=" + type + ", docker=" + docker + "]";
        }
    }

    private static class Port {
        int number;
        String protocol;

        @Override
        public String toString() {
            return "Port [number=" + number + ", protocol=" + protocol + "]";
        }
    }

    private static class Ports {
        List<Port> ports;

        @Override
        public String toString() {
            return "Ports [ports=" + ports + "]";
        }
    }

    private static class Discovery {
        String visibility;
        String name;
        Ports ports;

        @Override
        public String toString() {
            return "Discovery [visibility=" + visibility + ", name=" + name + ", ports=" + ports + "]";
        }
    }

    private static class Task {
        String id;
        String name;
        String frameworkId;
        String executorId;
        String slaveId;
        String state;
        Resources resources;
        List<State> statuses;
        Discovery discovery;
        Container container;

        @Override
        public String toString() {
            return "Task [id=" + id + ", name=" + name + ", frameworkId=" + frameworkId + ", executorId=" + executorId
                    + ", slaveId=" + slaveId + ", state=" + state + ", resources=" + resources + ", statuses="
                    + statuses + ", discovery=" + discovery + ", container=" + container + "]";
        }
    }

    private static class Tasks {
        List<Task> tasks;

        @Override
        public String toString() {
            return "Tasks [tasks=" + tasks + "]";
        }
    }

}
