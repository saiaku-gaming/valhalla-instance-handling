package com.valhallagame.instance_handling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple utility helper for converting stuff to json.
 *
 */
public class JS {

	private static ObjectMapper mapper = new ObjectMapper();

	public JS() {

	}

	public static Response message(Status status, String message) {
		return Response.status(status).entity(JS.message(message)).build();
	}

	public static Response message(Status status, Object o) {
		return Response.status(status).entity(JS.parse(o)).build();
	}

	public static JsonMessage message(String message) {
		return new JsonMessage(message);
	}

	public static JsonBool bool(boolean b) {
		return new JsonBool(b);
	}

	public static class JsonMessage {
		public JsonMessage() {
		}

		private String message;

		public JsonMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	public static class JsonBool {
		public JsonBool() {
		}

		private boolean bool;

		public JsonBool(boolean bool) {
			this.setBool(bool);
		}

		public boolean isBool() {
			return bool;
		}

		public void setBool(boolean bool) {
			this.bool = bool;
		}
	}

	/**
	 * Used to create a wrapping object around an array so that we only send
	 * json objects. this simplifies reading it.
	 */
	public static ObjectNode wrap(Object... objects) {
		ObjectNode node = mapper.createObjectNode();

		for (int i = 0; i < objects.length; i += 2) {
			node.set((String) objects[i], mapper.valueToTree(objects[i + 1]));
		}

		return node;
	}

	public static JsonNode parse(Object o) {
		return mapper.valueToTree(o);
	}

}
