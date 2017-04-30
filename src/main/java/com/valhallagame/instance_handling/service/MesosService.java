package com.valhallagame.instance_handling.service;


import java.util.Optional;

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
	
	public Optional<MesosFramework> getMesosFramework(FrameworkID frameworkId) {
		return getMesosFramework(frameworkId.getValue());
	}
	
	public Optional<MesosFramework> getMesosFramework(String mesosFrameworkId) {
		return mesosRepository.findById(mesosFrameworkId);
	}
	
	public Optional<MesosFramework> getLatestFramework() {
		return mesosRepository.getLatestMesosFramework();
	}
	
	public Optional<MesosFramework> getLatestValidFramework(double failoverTimeout) {
		return mesosRepository.getLatestValidMesosFramework(failoverTimeout);
	}
}
