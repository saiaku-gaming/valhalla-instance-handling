package com.valhallagame.instance_handling;

import org.skife.jdbi.v2.DBI;

import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
	
	public static void main(String[] args) throws Exception {
		new HelloWorldApplication().run("server", "hello-world.yml");
	}
	
	@Override
	public String getName() {
		return "hello-world";
	}
	
	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
		//nothing to do yet
	}
	
	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment) {
		final DBIFactory factory = new DBIFactory();
		final DBI jdbi = factory.build(environment, configuration.getDatabase(), "postgresql");
		final TestDAO testDAO = jdbi.onDemand(TestDAO.class);
		
		final HelloWorldResource helloWorldResource = new HelloWorldResource(configuration.getTemplate(), configuration.getDefaultName());
		final TestResource testResource = new TestResource(testDAO);
		
		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		
		environment.healthChecks().register("template", healthCheck);
		environment.jersey().register(helloWorldResource);
		environment.jersey().register(testResource);
	}
}
