package com.valhallagame.instance_handling;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {
	private TestDAO testDAO;
	
	public TestResource(TestDAO testDAO) {
		this.testDAO = testDAO;
	}
	
	@GET
	@Timed
	@Path("create")
	public String create() {
		testDAO.createSomethingTable();
		return "created something table";
	}
	
	@GET
	@Timed
	@Path("insert")
	public String insert(@QueryParam("id") Optional<Integer> id, @QueryParam("name") Optional<String> name) {
		testDAO.insert(id.get(), name.get());
		return "inserted a thing";
	}
	
	@GET
	@Timed
	@Path("select")
	public String select(@QueryParam("id") Optional<Integer> id) {
		return testDAO.findNameById(id.get());
	}
}
