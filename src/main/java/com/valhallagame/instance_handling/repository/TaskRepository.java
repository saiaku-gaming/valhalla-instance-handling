package com.valhallagame.instance_handling.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.instance_handling.model.Task;

public interface TaskRepository extends JpaRepository<Task, String> {
	public Optional<Task> findByTaskId(String taskId);
	public Optional<Task> findByInstanceId(Integer instanceId);
}
