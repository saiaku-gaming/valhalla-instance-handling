package com.valhallagame.instance_handling.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface MesosDAO {
	@SqlUpdate("insert into mesos_framework (id) values (:id)")
	void insert(@Bind("id") String name);

	@SqlQuery("select id from mesos_framework order by ts desc limit 1")
	String getLatestFramework(@Bind("id") int id);
}
