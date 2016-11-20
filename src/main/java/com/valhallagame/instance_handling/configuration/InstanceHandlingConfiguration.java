package com.valhallagame.instance_handling.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

public class InstanceHandlingConfiguration extends Configuration {

	@NotEmpty
	private String template;

	@NotEmpty
	private String defaultName = "ValhallaInstanceHandling";

	@Valid
	@NotNull
	private DataSourceFactory database = new DataSourceFactory();

	@JsonProperty
	public String getTemplate() {
		return template;
	}

	@JsonProperty
	public void setTemplate(String template) {
		this.template = template;
	}

	@JsonProperty
	public String getDefaultName() {
		return defaultName;
	}

	@JsonProperty
	public void setDefaultName(String name) {
		this.defaultName = name;
	}

	@JsonProperty
	public void setDataSourceFactory(DataSourceFactory factory) {
		this.database = factory;
	}

	@JsonProperty
	public DataSourceFactory getDatabase() {
		return database;
	}
}
