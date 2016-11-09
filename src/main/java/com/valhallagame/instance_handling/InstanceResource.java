package com.valhallagame.instance_handling;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

@Path("/v1/instance-resource")
@Produces(MediaType.APPLICATION_JSON)
public class InstanceResource {
	
	@GET
	@Timed
	@Path("start")
	public String start(@QueryParam("level") String level,@QueryParam("version") String version,@QueryParam("persistentServerUrl") String persistentServerUrl) {
		
		InstanceHandler.getInstanceHandler().queue(new TestInstance(level, version, persistentServerUrl));
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		
		InstanceHandler.getInstanceHandler().close();
		
		return "ServerStarted and Closed";
	}
}
