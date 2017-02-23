package net.kwami.tcp;

import com.google.gson.GsonBuilder;

public class Message {
	private long id;
	private String payload;

	public Message(long id, String payload) {
		super();
		this.id = id;
		this.payload = payload;
	}

	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}

	public String getPayload() {
		return payload;
	}

	public long getId() {
		return id;
	}

}
