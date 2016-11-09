package com.valhallagame.instance_handling;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;

@Path("/v1/instance-resource")
@Produces(MediaType.APPLICATION_JSON)
public class InstanceResource {
	
	@POST
	@Timed
	@Path("start")
	public Response start2(InstanceStart instanceStart) {
		InstanceHandler.getInstanceHandler().queue(new TestInstance(instanceStart.level, instanceStart.version, instanceStart.persistentServerUrl));
		return JS.message(Status.OK, "Server started");
	}
}
