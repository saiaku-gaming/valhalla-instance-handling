package com.valhallagame.instance_handling.handlers;

import javax.inject.Inject;

import com.valhallagame.instance_handling.mesos.ValhallaMesosSchedulerClient;
import com.valhallagame.instance_handling.model.Instance;

public class MesosHandler {

	@Inject
	ValhallaMesosSchedulerClient client;

	public void queue(Instance instance) {
		// TODO Auto-generated method stub
	}

	public void kill(Instance instance) {
		// TODO Auto-generated method stub
	}
}
