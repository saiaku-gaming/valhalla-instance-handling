package com.valhallagame.instance_handling.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.instance_handling.model.Task;

public interface TaskRepository extends JpaRepository<Task, String> {
	public Task findByTaskId(String taskId);
	public Task findByInstanceId(Integer instanceId);
}
