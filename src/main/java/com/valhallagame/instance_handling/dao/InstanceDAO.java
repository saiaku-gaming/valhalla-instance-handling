package com.valhallagame.instance_handling.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface InstanceDAO {

	void close();
	
	@SqlUpdate("INSERT INTO instance (level, version, state, address, port, task_id) VALUES (:level, :version, :state, :address, :port, :task_id)")
	void add(@Bind("level") String level, @Bind("version") String version, @Bind("state") String state, 
			@Bind("address") String address, @Bind("port") int port, @Bind("task_id") String taskId);
}
