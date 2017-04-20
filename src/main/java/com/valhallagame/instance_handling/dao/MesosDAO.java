package com.valhallagame.instance_handling.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface MesosDAO {
	@SqlUpdate("INSERT INTO mesos_framework (id) VALUES (:id) ON CONFLICT (id) DO UPDATE SET ts=now()")
	void upsertFrameworkId(@Bind("id") String name);

	@SqlQuery("SELECT id FROM mesos_framework ORDER BY ts DESC LIMIT 1")
	String getLatestFramework();
	
	@SqlQuery("SELECT id FROM mesos_framework WHERE EXTRACT(EPOCH FROM (now() - ts)) < :failoverTimeout ORDER BY ts DESC LIMIT 1")
	String getLatestValidFramework(@Bind("failoverTimeout") double failoverTimeout);
}
