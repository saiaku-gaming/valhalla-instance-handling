package com.valhallagame.instance_handling.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
@Table(name = "mesos_framework")
public class MesosFramework {

	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "ts")
	private Date timestamp;
}
