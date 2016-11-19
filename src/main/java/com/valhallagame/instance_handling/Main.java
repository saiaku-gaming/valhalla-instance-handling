package com.valhallagame.instance_handling;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.valhallagame.instance_handling.configuration.InstanceHandlingConfiguration;
import com.valhallagame.instance_handling.healthcheck.TemplateHealthCheck;
import com.valhallagame.instance_handling.mesos.ValhallaMesosSchedulerClient;
import com.valhallagame.instance_handling.rest.InstanceResource;
import com.valhallagame.instance_handling.service.MesosService;
import com.valhallagame.instance_handling.services.InstanceService;

import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Main extends Application<InstanceHandlingConfiguration> {

	static Logger log = LoggerFactory.getLogger(Main.class);

	private static final int RESTART_PORT = 55556;

	public static void main(String[] args) throws Exception {
		log.info("Running with arguments: " + String.join(",", args));
		Socket s = new Socket();
		try {
			s.connect(new InetSocketAddress("localhost", RESTART_PORT));
			Thread.sleep(100);
		} catch (ConnectException e) {
			// nothing to restart - ignore
		} finally {
			s.close();
		}
		startRestartThread();

		new Main().run("server", "instance-handling.yml");
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
		final DBIFactory factory = new DBIFactory();
		final DBI jdbi = factory.build(environment, configuration.getDatabase(), "postgresql");

		final InstanceResource instanceResource = new InstanceResource();

		setupDependencyInjection(environment.jersey());

		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());

		environment.healthChecks().register("template", healthCheck);
		environment.jersey().register(instanceResource);

	}

	private static void startRestartThread() {
		// TODO Security?
		new Thread() {

			@Override
			public void run() {
				try {
					ServerSocket s = new ServerSocket(RESTART_PORT);
					try {
						s.accept();
					} finally {
						s.close();
					}
				} catch (Exception e) {
					log.error("", e);
				}
				System.exit(0);
			}
		}.start();
	}

	private static void setupDependencyInjection(JerseyEnvironment env) {
		env.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(InstanceService.class).to(InstanceService.class);
			}
		});

		env.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(MesosService.class).to(MesosService.class);
			}
		});

		env.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(new ValhallaMesosSchedulerClient()).to(ValhallaMesosSchedulerClient.class);
			}
		});
	}

}
