package com.valhallagame.instance_handling.messages;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class InstanceUpdate {
	
	@ApiModelProperty(required = true)
	private int instanceId;
	
	@ApiModelProperty(required = true)
	private String state;
	
	@ApiModelProperty(required = true)
	private String url;
	
	@ApiModelProperty(required = true)
	private int port;

	public InstanceUpdate(){
	}
	
	public InstanceUpdate(int instanceId, String state, String url, int port) {
		this.instanceId = instanceId;
		this.state = state;
		this.url = url;
		this.port = port;
	}

	public int getInstanceId() {
		return instanceId;
	}
	
	public String getState() {
		return state;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getPort() {
		return port;
	}
}
