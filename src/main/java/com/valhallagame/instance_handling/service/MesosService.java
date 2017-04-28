package com.valhallagame.instance_handling.service;


import org.apache.mesos.v1.Protos.FrameworkID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instance_handling.model.MesosFramework;
import com.valhallagame.instance_handling.repository.MesosRepository;

@Service
public class MesosService {
	
	@Autowired
	private MesosRepository mesosRepository;
	
	public void save(MesosFramework mesosFramework) {
		mesosRepository.save(mesosFramework);
	}
	
	public MesosFramework getMesosFramework(FrameworkID frameworkId) {
		return getMesosFramework(frameworkId.getValue());
	}
	
	public MesosFramework getMesosFramework(String mesosFrameworkId) {
		return mesosRepository.findById(mesosFrameworkId);
	}
	
	public MesosFramework getLatestFramework() {
		return mesosRepository.getLatestMesosFramework();
	}
	
	public MesosFramework getLatestValidFramework(double failoverTimeout) {
		return mesosRepository.getLatestValidMesosFramework(failoverTimeout);
	}
}
