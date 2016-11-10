package com.valhallagame.instance_handling.messages;

public class InstanceStart {

	public InstanceStart() {
		// json constr;
	}

	public InstanceStart(String level, String version, String persistentServerUrl) {
		this.level = level;
		this.version = version;
		this.persistentServerUrl = persistentServerUrl;
	}

	public String persistentServerUrl;
	public String level;
	public String version;
}
