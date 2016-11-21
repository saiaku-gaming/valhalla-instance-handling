package com.valhallagame.instance_handling.handlers;


import org.apache.mesos.v1.Protos.FrameworkID;

import com.valhallagame.instance_handling.configuration.MesosConfig;
import com.valhallagame.instance_handling.dao.MesosDAO;
import com.valhallagame.instance_handling.mesos.ValhallaMesosSchedulerClient;
import com.valhallagame.instance_handling.messages.InstanceAdd;

public class MesosHandler {

	private ValhallaMesosSchedulerClient client;
	private MesosDAO dao;
	
	public MesosHandler(MesosDAO dao, MesosConfig mesosConfig, InstanceHandler instanceHandler) {
		this.dao = dao;
		
		this.client = new ValhallaMesosSchedulerClient(this, instanceHandler, mesosConfig.getFailoverTimeout());
	}

	public void queue(InstanceAdd instanceAdd) {
		client.queueInstance(instanceAdd);
	}

	public void kill(String taskId) {
		client.kill(taskId);
	}
	
	public void insertFrameworkId(FrameworkID frameworkId) {
		dao.insert(frameworkId.getValue());
	}
	
	public String getLatestFrameworkId() {
		return dao.getLatestFramework();
	}
	
	public String getLatestValidFrameworkId(double failoverTimeout) {
		return dao.getLatestValidFramework(failoverTimeout);
	}
}
