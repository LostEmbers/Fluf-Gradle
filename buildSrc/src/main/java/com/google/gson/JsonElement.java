package com.google.gson;

public class JsonElement {
	public boolean isJsonArray() {
		return false;
	}
	
	public boolean isJsonObject() {
		return false;
	}
	
	public boolean isJsonPrimitive() {
		return false;
	}
	
	public boolean isJsonNull() {
		return false;
	}
	
	public String getAsString() {
		return this.toString();
	}
	
	public JsonPrimitive getAsJsonPrimitive() {
		return (JsonPrimitive) this;
	}
	
	public JsonObject getAsJsonObject() {
		return (JsonObject) this;
	}
}