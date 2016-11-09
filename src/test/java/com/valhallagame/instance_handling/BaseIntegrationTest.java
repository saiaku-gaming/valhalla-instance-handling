package com.valhallagame.instance_handling;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class BaseIntegrationTest {
	
	private static WebTarget target;
	
	public WebTarget getTarget() {
		return target;
	}

	@BeforeClass
	public static void setUp() throws Exception {
		HelloWorldApplication.start();
		Client c = ClientBuilder.newClient();
		target = c.target("http://localhost:8080");
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		System.exit(1);
	}
}
