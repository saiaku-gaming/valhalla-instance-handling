package com.valhallagame.instance_handling;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class App {

	static Logger log = LoggerFactory.getLogger(App.class);

	private static final int RESTART_PORT = 55557;
	

	public static void main(String[] args) throws Exception {
		loadSystemProperties(args);
		
		SpringApplication.run(App.class, args);
		
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
	}

	private static void loadSystemProperties(String[] args) {

		if(args.length > 0) {
			// override system properties with local properties
			try (InputStream inputStream = new FileInputStream(args[0])) {
				System.getProperties().load(inputStream);
			} catch (IOException e) {
				log.error("", e);
			}
		}
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

}
