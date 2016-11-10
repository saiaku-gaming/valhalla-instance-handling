package com.valhallagame.instance_handling.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.valhallagame.instance_handling.instance.Instance;
import com.valhallagame.instance_handling.instance.InstanceHandler;
import com.valhallagame.instance_handling.messages.InstanceStart;
import com.valhallagame.instance_handling.utils.JS;

@Path("/v1/instance-resource")
@Produces(MediaType.APPLICATION_JSON)
public class InstanceResource {
	
	@POST
	@Timed
	@Path("start")
	public Response start2(InstanceStart instanceStart) {
		InstanceHandler.getInstanceHandler().queue(new Instance(instanceStart.level, instanceStart.version, instanceStart.persistentServerUrl));
		return JS.message(Status.OK, "Server started");
	}
}
