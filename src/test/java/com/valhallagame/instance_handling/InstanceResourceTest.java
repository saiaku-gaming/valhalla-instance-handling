package com.valhallagame.instance_handling;

import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.instance_handling.messages.InstanceParameter;

public class InstanceResourceTest extends BaseIntegrationTest {

	@Test
	public void startTest() throws IOException {
		InstanceAdd message = new InstanceAdd(new Random().nextInt(), "TrialMap", "latest", "persistent.valhalla-game.com");
		
		OkHttpClient client = new OkHttpClient();
		ObjectMapper mapper = new ObjectMapper();
		
		String json = mapper.writeValueAsString(message);
		
		Request request = new Request.Builder()
				.url("http://localhost:4321/v1/instance-resource/queue-instance")
				.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
				.build();
		
		Response response = client.newCall(request).execute();
		
		System.out.println("response: " + response.body().string());
		
		Assert.assertEquals(200, response.code());
	}
	
	@Test
	public void queueAndKillInstance() throws IOException {
		int instanceId = new Random().nextInt();
		InstanceAdd message = new InstanceAdd(instanceId, "TrialMap", "latest", "persistent.valhalla-game.com");
		
		OkHttpClient client = new OkHttpClient();
		ObjectMapper mapper = new ObjectMapper();
		
		String json = mapper.writeValueAsString(message);
		
		Request request = new Request.Builder()
				.url("http://localhost:4321/v1/instance-resource/queue-instance")
				.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
				.build();
		
		Response response = client.newCall(request).execute();
		
		System.out.println("response: " + response.body().string());
		Assert.assertEquals(200, response.code());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}

		InstanceParameter message2 = new InstanceParameter(instanceId);
		
		json = mapper.writeValueAsString(message2);
		
		request = new Request.Builder()
				.url("http://localhost:4321/v1/instance-resource/kill-instance")
				.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
				.build();
		
		response = client.newCall(request).execute();
		
		System.out.println("response: " + response.body().string());
		Assert.assertEquals(200, response.code());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
	}
}
