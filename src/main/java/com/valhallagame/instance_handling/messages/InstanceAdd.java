package com.valhallagame.instance_handling.messages;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class InstanceAdd {
	
	@ApiModelProperty(required = true)
	private int instanceId;
	
	@ApiModelProperty(required = true)
	private String version;
	
	@ApiModelProperty(required = true)
	private String persistentServerUrl;
	
	@ApiModelProperty(required = true)
	private String level;
	
	@ApiModelProperty(required = true)
	private String taskId;

	public InstanceAdd(){
	}
	
	public InstanceAdd(int instanceId, String level, String version, String persistentServerUrl, String taskId) {
		this.instanceId = instanceId;
		this.level = level;
		this.version = version;
		this.persistentServerUrl = persistentServerUrl;
		this.taskId = taskId;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public String getVersion() {
		return version;
	}

	public String getPersistentServerUrl() {
		return persistentServerUrl;
	}
	
	public String getLevel() {
		return level;
	}
	
	public String getTaskId() {
		return taskId;
	}
}
