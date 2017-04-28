package com.valhallagame.instance_handling.dao;

//import org.skife.jdbi.v2.sqlobject.Bind;
//import org.skife.jdbi.v2.sqlobject.SqlQuery;
//import org.skife.jdbi.v2.sqlobject.SqlUpdate;
//
//public interface InstanceDAO {
//
//	void close();
//	
//	@SqlUpdate("INSERT INTO instance (level, version, state, address, port, task_id) VALUES (:level, :version, :state, :address, :port, :task_id)")
//	void add(@Bind("level") String level, @Bind("version") String version, @Bind("state") String state, 
//			 @Bind("address") String address, @Bind("port") int port, @Bind("task_id") String taskId);
//	
//	@SqlUpdate("DELETE FROM instance WHERE id = :id")
//	void remove(@Bind("id") int id);
//	
//	@SqlQuery("SELECT task_id FROM task WHERE instance_id = :instance_id")
//	String getTaskId(@Bind("instance_id") int instanceId);
//	
//	@SqlUpdate("INSERT INTO task (task_id, instance_id, task_state) VALUES (:task_id, :instance_id, cast(:task_state AS task_state))")
//	void addTask(@Bind("task_id") String taskId, @Bind("instance_id") int instanceId, @Bind("task_state") String taskState);
//	
//	@SqlUpdate("UPDATE task SET task_state=cast(:task_state AS task_state) WHERE task_id = :task_id")
//	void updateTaskState(@Bind("task_id") String taskId, @Bind("task_state") String taskState);
//	
//	@SqlQuery("SELECT instance_id FROM task WHERE task_id = :task_id")
//	int getInstanceId(@Bind("task_id") String taskId);
//}
