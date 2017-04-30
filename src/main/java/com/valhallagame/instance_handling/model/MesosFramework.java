package com.valhallagame.instance_handling.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mesos_framework")
public class MesosFramework {

	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "ts")
	private Date timestamp;
	
	public MesosFramework(String id) {
		this(id, new Date());
	}
}
