package com.valhallagame.instance_handling.messages;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class InstanceParameter {

	@ApiModelProperty(required = true)
	private final int instanceId;

	public InstanceParameter(int instanceId) {
		this.instanceId = instanceId;
	}

	public int getInstanceId() {
		return instanceId;
	}
}
