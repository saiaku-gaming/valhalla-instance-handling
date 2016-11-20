package com.valhallagame.instance_handling.handlers;

import org.jvnet.hk2.annotations.Service;

import com.valhallagame.instance_handling.dao.InstanceDAO;
import com.valhallagame.instance_handling.model.Instance;

/**
 * 
 * This is a step between the DAO and the consumer. Just a good place to store
 * logic.
 *
 */
@Service
public class InstanceHandler {
	
	private InstanceDAO dao;

	public InstanceHandler(InstanceDAO dao) {
		this.dao = dao;
	}
	
	public Instance getInstance(int instanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove(int instanceId) {
		// TODO Auto-generated method stub

	}

}
