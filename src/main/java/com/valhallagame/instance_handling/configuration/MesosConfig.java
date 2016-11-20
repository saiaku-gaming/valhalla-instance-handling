package com.valhallagame.instance_handling.configuration;

import javax.validation.constraints.Min;

public class MesosConfig {
	
	@Min(0)
	private double failoverTimeout;

	public double getFailoverTimeout() {
		return failoverTimeout;
	}

	public void setFailoverTimeout(double failoverTimeout) {
		this.failoverTimeout = failoverTimeout;
	}
}
