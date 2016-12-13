package com.valhallagame.instance_handling.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.valhallagame.instance_handling.handlers.InstanceHandler;
import com.valhallagame.instance_handling.handlers.MesosHandler;
import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.instance_handling.messages.InstanceParameter;
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
	public Response start(InstanceAdd instanceAdd) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm");
		String date = sdf.format(new Date());

		if (instanceAdd.getTaskId() == null || instanceAdd.getTaskId().equals("")) {
			String taskId = instanceAdd.getInstanceId() + " v:" + instanceAdd.getVersion() + " s:" + instanceAdd
					.getPersistentServerUrl() + " l:" + instanceAdd.getLevel() + " t:" + date;
			instanceAdd.setTaskId(taskId);
		}

		mesosHandler.queue(instanceAdd);
		instanceHandler.addTask(instanceAdd.getTaskId(), instanceAdd.getInstanceId(), null);
		return JS.message(Status.OK, "Instance queued");
	}

	@POST
	@Timed
	@Path("kill-instance")
	@ApiOperation(value = "Kills an instance without any checks. Pure killing!")
	public Response killInstance(InstanceParameter instanceParameter) {
		String taskId = instanceHandler.getTaskId(instanceParameter.getInstanceId());
		mesosHandler.kill(taskId);
		instanceHandler.remove(instanceParameter.getInstanceId());
		return JS.message(Status.OK, "It died");
	}
}
