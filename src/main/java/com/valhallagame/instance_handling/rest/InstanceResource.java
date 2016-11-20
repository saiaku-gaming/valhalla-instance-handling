package com.valhallagame.instance_handling.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.valhallagame.instance_handling.handlers.InstanceHandler;
import com.valhallagame.instance_handling.handlers.MesosHandler;
import com.valhallagame.instance_handling.messages.InstanceParameter;
import com.valhallagame.instance_handling.model.Instance;
import com.valhallagame.instance_handling.utils.JS;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/v1/instance-resource")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "InstanceQueueHandling")
public class InstanceResource {

	private InstanceHandler instanceHandler;
	private MesosHandler mesosHandler;
	
	public InstanceResource(InstanceHandler instanceHandler, MesosHandler mesosHandler) {
		this.instanceHandler = instanceHandler;
		this.mesosHandler = mesosHandler;
	}

	@POST
	@Timed
	@Path("queue-instance")
	@ApiOperation(value = "Queues an instance.")
	public Response start(Instance instance) {
		mesosHandler.queue(instance);
		return JS.message(Status.OK, "Instance queued");
	}

	@POST
	@Timed
	@Path("kill-instance")
	@ApiOperation(value = "Kills an instance without any checks. Pure killing!")
	public Response killInstance(InstanceParameter instanceParameter) {
		Instance instance = instanceHandler.getInstance(instanceParameter.getInstanceId());
		mesosHandler.kill(instance);
		return JS.message(Status.OK, "It died");
	}

	//not needed?
	@POST
	@Timed
	@Path("remove-instance")
	@ApiOperation(value = "Carefully removes an instance.", notes = "Makes sure player count is zero and waits a minute to kill so that no one is currently connecting.")
	public Response removeInstance(InstanceParameter instance) {
		instanceHandler.remove(instance.getInstanceId());
		return JS.message(Status.OK, "Instance removed.");
	}

}
