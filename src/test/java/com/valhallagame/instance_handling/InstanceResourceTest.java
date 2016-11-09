package com.valhallagame.instance_handling;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;

import junit.framework.Assert;

public class InstanceResourceTest extends BaseIntegrationTest {

	@Test
	public void startTest() {
		InstanceStart message = new InstanceStart("TrialMap", "latest", "persistent.valhalla-game.com");
		Response resp = getTarget().path("/v1/instance-resource/start").request().post(Entity.json(message));
		System.out.println("response " + resp.readEntity(String.class));
		Assert.assertEquals(200, resp.getStatus());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		InstanceHandler instanceHandler = InstanceHandler.getInstanceHandler();
		instanceHandler.close();
	}
}
