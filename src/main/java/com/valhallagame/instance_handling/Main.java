package com.valhallagame.instance_handling;

import org.skife.jdbi.v2.DBI;

import com.valhallagame.instance_handling.configuration.InstanceHandlingConfiguration;
import com.valhallagame.instance_handling.healthcheck.TemplateHealthCheck;
import com.valhallagame.instance_handling.rest.InstanceResource;

import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Main extends Application<InstanceHandlingConfiguration> {
	
	public static void main(String[] args) throws Exception {
		start();
	}
	
	@Override
	public String getName() {
		return "Instance Handling";
	}
	
	@Override
	public void initialize(Bootstrap<InstanceHandlingConfiguration> bootstrap) {
		//nothing to do yet and dont really know what to do here
	}
	
	@Override
	public void run(InstanceHandlingConfiguration configuration, Environment environment) {
		final DBIFactory factory = new DBIFactory();
		final DBI jdbi = factory.build(environment, configuration.getDatabase(), "postgresql");
		
		final InstanceResource instanceResource = new InstanceResource();
		
		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		
		environment.healthChecks().register("template", healthCheck);
		environment.jersey().register(instanceResource);
	}
	
	public static void start() throws Exception {
		new Main().run("server", "hello-world.yml");
	}
}
