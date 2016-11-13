package com.valhallagame.instance_handling.instance;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InstanceHandler implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(InstanceHandler.class);
	
	private static InstanceHandler instanceHandler = new InstanceHandler();

	private InstanceMesosClient framework;
	
	private boolean closed;

	public boolean isClosed() {
		return closed;
	}

	private InstanceHandler() {
		framework = new InstanceMesosClient();
	};

	public static InstanceHandler getInstanceHandler() {
		if (instanceHandler == null || instanceHandler.isClosed()) {
			instanceHandler = new InstanceHandler();
		}
		return instanceHandler;
	}

	public void kill(Instance instance) {
		framework.kill(instance);
	}

	public void queue(Instance ins) {
		framework.queueInstance(ins);
	}

	@Override
	public void close() {
		try {
			framework.close();
		} catch (IOException e) {
			log.error("closing problems", e);
		}
		closed = true;
	}

}
