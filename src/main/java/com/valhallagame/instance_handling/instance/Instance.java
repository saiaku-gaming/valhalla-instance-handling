package com.valhallagame.instance_handling.instance;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;


public class Instance {
	
	private int id;
	private String taskId;
	private String version;
	private String level;
	private String persistentServerUrl;
	private Instant timestamp;
	private Instant serverCallbackTimestamp;
	private boolean ready;
	
	public Instance(String level, String version, String persistentServerUrl) {
		// this is not how ids should be set butt-fuck-it
		this.id = new Random().nextInt();
		this.level = level;
		this.version = version;
		this.timestamp = Instant.now();
		this.setTaskId(UUID.randomUUID().toString());
		this.serverCallbackTimestamp = Instant.EPOCH;
		this.persistentServerUrl = persistentServerUrl;
		this.ready = false;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public String getPersistentServerUrl() {
		return persistentServerUrl;
	}

	public void setPersistentServerUrl(String persistentServerUrl) {
		this.persistentServerUrl = persistentServerUrl;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public Instant getServerCallbackTimestamp() {
		return serverCallbackTimestamp;
	}

	public void setServerCallbackTimestamp(Instant serverCallbackTimestamp) {
		this.serverCallbackTimestamp = serverCallbackTimestamp;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
