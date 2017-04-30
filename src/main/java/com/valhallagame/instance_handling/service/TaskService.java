package com.valhallagame.instance_handling.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instance_handling.model.Task;
import com.valhallagame.instance_handling.repository.TaskRepository;

@Service
public class TaskService {

	@Autowired
	private TaskRepository taskRepository;
	
	public Optional<Task> getTask(String taskId) {
		return taskRepository.findByTaskId(taskId);
	}
	
	public Optional<Task> getTask(Integer instanceId) {
		return taskRepository.findByInstanceId(instanceId);
	}
	
	public void save(Task task) {
		taskRepository.save(task);
	}
	
}
