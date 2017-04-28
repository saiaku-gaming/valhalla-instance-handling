package com.valhallagame.instance_handling.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.instance_handling.model.Instance;

public interface InstanceRepository extends JpaRepository<Instance, Integer> {
	public Instance findById(Integer id);
	public Instance findByTaskId(String taskId);
}
