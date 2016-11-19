package com.valhallagame.instance_handling;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.valhallagame.instance_handling.messages.QueueInstance;

import junit.framework.Assert;

public class InstanceResourceTest extends BaseIntegrationTest {

	@Test
	public void startTest() {
		QueueInstance message = new QueueInstance("TrialMap", "latest", "persistent.valhalla-game.com");
		Response resp = getTarget().path("/v1/instance-resource/start").request().post(Entity.json(message));
		System.out.println("response " + resp.readEntity(String.class));
		Assert.assertEquals(200, resp.getStatus());
	}
}
