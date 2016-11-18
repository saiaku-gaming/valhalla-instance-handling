package com.valhallagame.instance_handling;

import com.valhallagame.instance_handling.configuration.InstanceHandlingConfiguration;
import com.valhallagame.instance_handling.healthcheck.TemplateHealthCheck;
import com.valhallagame.instance_handling.rest.InstanceResource;
import com.valhallagame.persistent.ApiException;
import com.valhallagame.persistent.client.CharacterServiceApi;
import com.valhallagame.persistent.client.model.CharNameParameter;
import com.valhallagame.persistent.client.model.JsonMessage;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Main extends Application<InstanceHandlingConfiguration> {

	public static void main(String[] args) throws Exception {
		// start();

		CharacterServiceApi apiInstance = new CharacterServiceApi();

		CharNameParameter body = new CharNameParameter();
		body.setName("Nisse");
		try {
			JsonMessage result = apiInstance.characterAvailable(body);
			System.out.println(result);
		} catch (ApiException e) {
			System.err.println("Exception when calling CharacterServiceApi#characterAvailable");
			e.printStackTrace();
		}

	}

	@Override
	public String getName() {
		return "Instance Handling";
	}

	@Override
	public void initialize(Bootstrap<InstanceHandlingConfiguration> bootstrap) {
		// nothing to do yet and dont really know what to do here
	}

	@Override
	public void run(InstanceHandlingConfiguration configuration, Environment environment) {
		// This is to setup DAO but we don't have any yet
		// final DBIFactory factory = new DBIFactory();
		// final DBI jdbi = factory.build(environment,
		// configuration.getDatabase(), "postgresql");

		final InstanceResource instanceResource = new InstanceResource();

		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());

		environment.healthChecks().register("template", healthCheck);
		environment.jersey().register(instanceResource);

	}

	public static void start() throws Exception {
		new Main().run("server", "instance-handling.yml");
	}
}
