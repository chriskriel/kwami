package net.kwami.tcp;

import com.google.gson.GsonBuilder;

public class ContainerMessage<T> {
	private long origin;
	private String appName;
	private String payload;

	public ContainerMessage(long origin, String appName, Object payload) {
		super();
		this.origin = origin;
		this.appName = appName;
		this.payload = new GsonBuilder().create().toJson(payload);
	}

	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}

	public T getObject(Class<T> clazz) {
		return (T) new GsonBuilder().create().fromJson(payload, clazz);
	}

	public long getOrigin() {
		return origin;
	}

	public void setOrigin(long origin) {
		this.origin = origin;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
}
