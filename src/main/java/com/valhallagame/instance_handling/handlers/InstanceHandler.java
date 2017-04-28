package com.valhallagame.instance_handling.handlers;

//import org.jvnet.hk2.annotations.Service;
//
//import com.valhallagame.instance_handling.dao.InstanceDAO;
//
///**
// * 
// * This is a step between the DAO and the consumer. Just a good place to store
// * logic.
// *
// */
//@Service
//public class InstanceHandler {
//	
//	private InstanceDAO dao;
//
//	public InstanceHandler(InstanceDAO dao) {
//		this.dao = dao;
//	}
//
//	public void addTask(String taskId, int instanceId, String taskState) {
//		dao.addTask(taskId, instanceId, taskState);
//	}
//	
//	public void updateTaskState(String taskId, String taskState) {
//		dao.updateTaskState(taskId, taskState);
//	}
//	
//	public String getTaskId(int instanceId) {
//		return dao.getTaskId(instanceId);
//	}
//	
//	public void remove(int instanceId) {
//		dao.remove(instanceId);
//	}
//	
//	public int getInstanceId(String taskId) {
//		return dao.getInstanceId(taskId);
//	}
//}
