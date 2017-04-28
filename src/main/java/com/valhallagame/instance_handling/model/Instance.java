package com.valhallagame.instance_handling.model;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
@Table(name = "instance")
public class Instance {

	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "ts")
	private Date timestamp;
	
	@Column(name = "level")
	private String level;
	
	@Column(name = "version")
	private String version;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "port")
	private Integer port;
	
	@Column(name = "task_id")
	private String taskId;
	
	private String persistentServerUrl;
	private Instant serverCallbackTimestamp;
	private boolean ready;
	
	public Instance() {
	}

	public Instance(int id, String level, String version, String persistentServerUrl) {
		this.id = id;
		this.level = level;
		this.version = version;
		this.timestamp = new Date();
		this.setTaskId(UUID.randomUUID().toString());
		this.serverCallbackTimestamp = Instant.EPOCH;
		this.persistentServerUrl = persistentServerUrl;
		this.ready = false;
	}
}
