package com.valhallagame.instance_handling;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestDAO {

	@SqlUpdate("CREATE TABLE something (id INTEGER PRIMARY KEY, name TEXT)")
	void createSomethingTable();
	
	@SqlUpdate("INSERT INTO something (id, name) VALUES (:id, :name)")
	void insert(@Bind("id") int id, @Bind("name") String name);
	
	@SqlQuery("SELECT name FROM something WHERE id = :id")
	String findNameById(@Bind("id") int id);
}
