package com.valhallagame.instance_handling.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
@Table(name = "task")
public class Task {

	@Id
	@Column(name = "task_id")
	private String taskId;
	
	@Column(name = "instance_id")
	private Integer instanceId;
	
	@Column(name = "task_state")
	private String taskState;
}
