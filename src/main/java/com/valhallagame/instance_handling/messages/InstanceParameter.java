package com.valhallagame.instance_handling.messages;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

@ApiModel
@ToString
public class InstanceParameter {

	@ApiModelProperty(required = true)
	private int instanceId;

	public InstanceParameter(){
	}
	
	public InstanceParameter(int instanceId) {
		this.instanceId = instanceId;
	}

	public int getInstanceId() {
		return instanceId;
	}
}
