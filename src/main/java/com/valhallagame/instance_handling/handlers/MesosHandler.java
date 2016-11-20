package com.valhallagame.instance_handling.handlers;

import com.valhallagame.instance_handling.dao.MesosDAO;
import com.valhallagame.instance_handling.mesos.ValhallaMesosSchedulerClient;
import com.valhallagame.instance_handling.model.Instance;

public class MesosHandler {

	private ValhallaMesosSchedulerClient client;
	private MesosDAO dao;
	
	public MesosHandler(ValhallaMesosSchedulerClient client, MesosDAO dao) {
		this.client = client;
		this.dao = dao;
	}

	public void queue(Instance instance) {
		// TODO Auto-generated method stub
	}

	public void kill(Instance instance) {
		// TODO Auto-generated method stub
	}
}
