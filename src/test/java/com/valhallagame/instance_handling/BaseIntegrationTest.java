package com.valhallagame.instance_handling;

import org.junit.BeforeClass;

import com.squareup.okhttp.OkHttpClient;

public abstract class BaseIntegrationTest {

	private static OkHttpClient client;

	public OkHttpClient getClient() {
		return client;
	}

	@BeforeClass
	public static void setUp() throws Exception {
		App.main(new String[] {});
		client = new OkHttpClient();
	}
}
