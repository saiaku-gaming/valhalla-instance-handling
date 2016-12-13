package com.valhallagame.instance_handling;

import java.util.Random;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.instance_handling.messages.InstanceParameter;
import com.valhallagame.instance_handling.model.Instance;

import junit.framework.Assert;

public class InstanceResourceTest extends BaseIntegrationTest {

	@Test
	public void startTest() {
		Instance message = new Instance(new Random().nextInt(), "TrialMap", "latest", "persistent.valhalla-game.com");
		Response resp = getTarget().path("/v1/instance-resource/start").request().post(Entity.json(message));
		System.out.println("response " + resp.readEntity(String.class));
		Assert.assertEquals(200, resp.getStatus());
	}

	@Test
	public void queueAndKillInstance() {
		int instanceId = new Random().nextInt();
		InstanceAdd message = new InstanceAdd(instanceId, "TrialMap", "latest", "persistent.valhalla-game.com");
		Response resp = getTarget().path("/v1/instance-resource/queue-instance").request().post(Entity.json(message));
		System.out.println("response " + resp.readEntity(String.class));
		Assert.assertEquals(200, resp.getStatus());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}

		InstanceParameter message2 = new InstanceParameter(instanceId);
		resp = getTarget().path("/v1/instance-resource/kill-instance").request().post(Entity.json(message2));
		System.out.println("response " + resp.readEntity(String.class));
		Assert.assertEquals(200, resp.getStatus());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
	}
}
