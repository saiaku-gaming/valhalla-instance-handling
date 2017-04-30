package com.valhallagame.instance_handling.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instance_handling.model.Instance;
import com.valhallagame.instance_handling.repository.InstanceRepository;

@Service
public class InstanceService {

	@Autowired
	private InstanceRepository instanceRepository;
	
	public Instance getInstance(String taskId) {
		return instanceRepository.findByTaskId(taskId);
	}
	
	public Instance getInstance(Integer instanceId) {
		return instanceRepository.findById(instanceId);
	}
	
}
