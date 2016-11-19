package com.valhallagame.instance_handling.messages;

public class QueueInstance {

	public QueueInstance() {
		// json constr;
	}

	public QueueInstance(String level, String version, String persistentServerUrl) {
		this.level = level;
		this.version = version;
		this.persistentServerUrl = persistentServerUrl;
	}

	public String persistentServerUrl;
	public String level;
	public String version;
}
