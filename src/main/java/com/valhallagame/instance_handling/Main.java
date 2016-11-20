package com.valhallagame.instance_handling;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.valhallagame.instance_handling.configuration.InstanceHandlingConfiguration;
import com.valhallagame.instance_handling.dao.InstanceDAO;
import com.valhallagame.instance_handling.handlers.InstanceHandler;
import com.valhallagame.instance_handling.handlers.MesosHandler;
import com.valhallagame.instance_handling.healthcheck.TemplateHealthCheck;
import com.valhallagame.instance_handling.mesos.ValhallaMesosSchedulerClient;
import com.valhallagame.instance_handling.rest.InstanceResource;

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

		loadSystemProperties();

		new Main().run("server", "instance-handling.yml");
	}

	private static void loadSystemProperties() {
		// override system properties with local properties
		// Maybe /home/valhalla should be working directory or something?
		try (InputStream inputStream = new FileInputStream("/home/valhalla/server.properties")) {
			System.getProperties().load(inputStream);
		} catch (IOException e) {
			log.error("", e);
		}

	}

	private static void updateDB(DataSource dataSource) {
		log.info("Patching database.");

		// Create the Flyway instance
		Flyway flyway = new Flyway();

		// Point it to the database
		flyway.setDataSource(dataSource);

		// if debug machine
		if (System.getProperty("valhalla.server.secret", "SERVER_SECRET").equals("SERVER_SECRET")) {
			flyway.clean();
		}

		flyway.migrate();
		log.info("Patching database complete.");
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
		updateDB(configuration.getDatabase().build(environment.metrics(), "postgresql"));

		final DBIFactory factory = new DBIFactory();

		final DBI jdbi = factory.build(environment, configuration.getDatabase(), "postgresql");

		setupDependencyInjection(environment.jersey());

		registerHealthchecks(environment, configuration);
		
		registerResources(environment, jdbi);
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
				bind(InstanceHandler.class).to(InstanceHandler.class);
			}
		});

		env.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(MesosHandler.class).to(MesosHandler.class);
			}
		});

		env.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(new ValhallaMesosSchedulerClient()).to(ValhallaMesosSchedulerClient.class);
			}
		});
	}
	
	private static void registerHealthchecks(Environment environment, InstanceHandlingConfiguration configuration) {
		
		final TemplateHealthCheck templateHealthCheck = new TemplateHealthCheck(configuration.getTemplate());

		environment.healthChecks().register("template", templateHealthCheck);
	}
	
	private static void registerResources(Environment environment, DBI jdbi) {
		
		final InstanceDAO instanceDAO = jdbi.onDemand(InstanceDAO.class);
		
		environment.jersey().register(new InstanceResource(instanceDAO));
	}

}
