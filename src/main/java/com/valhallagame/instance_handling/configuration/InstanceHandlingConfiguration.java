package com.valhallagame.instance_handling.configuration;

//import io.dropwizard.Configuration;
//import io.dropwizard.db.DataSourceFactory;
//
//public class InstanceHandlingConfiguration extends Configuration {
//
//	@NotEmpty
//	private String template;
//
//	@NotEmpty
//	private String defaultName = "ValhallaInstanceHandling";
//
//	@Valid
//	@NotNull
//	private DataSourceFactory database = new DataSourceFactory();
//	
//	@Valid
//	@NotNull
//	private MesosConfig mesos = new MesosConfig();
//
//	@JsonProperty
//	public String getTemplate() {
//		return template;
//	}
//
//	@JsonProperty
//	public void setTemplate(String template) {
//		this.template = template;
//	}
//
//	@JsonProperty
//	public String getDefaultName() {
//		return defaultName;
//	}
//
//	@JsonProperty
//	public void setDefaultName(String name) {
//		this.defaultName = name;
//	}
//
//	@JsonProperty
//	public void setDataSourceFactory(DataSourceFactory factory) {
//		this.database = factory;
//	}
//
//	@JsonProperty
//	public DataSourceFactory getDatabase() {
//		return database;
//	}
//
//	@JsonProperty
//	public MesosConfig getMesos() {
//		return mesos;
//	}
//
//	@JsonProperty
//	public void setMesos(MesosConfig mesos) {
//		this.mesos = mesos;
//	}
//}
