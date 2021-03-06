package net.kwami.pipe.server;

import java.util.concurrent.TimeUnit;

import com.google.gson.GsonBuilder;

public class ServerConfig {
	private long keepAliveTime = 10000;
	private int port = 58080;
	private int corePoolSize = 10;
	private int maxPoolSize = 10;
	private int submitQueueSize = 10;
	private int commandBufferSize = 256;
	private TimeUnit keepAliveTimeUnit = TimeUnit.DAYS;
	private String callableImplementation = "net.kwami.pipe.server.DummyCallable";

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
	}

	public String getCallableImplementation() {
		return callableImplementation;
	}

	public void setCallableImplementation(String callableImplementation) {
		this.callableImplementation = callableImplementation;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getSubmitQueueSize() {
		return submitQueueSize;
	}

	public void setSubmitQueueSize(int submitQueueSize) {
		this.submitQueueSize = submitQueueSize;
	}

	public int getCommandBufferSize() {
		return commandBufferSize;
	}

	public void setCommandBufferSize(int commandBufferSize) {
		this.commandBufferSize = commandBufferSize;
	}

	public TimeUnit getKeepAliveTimeUnit() {
		return keepAliveTimeUnit;
	}

	public void setKeepAliveTimeUnit(TimeUnit keepAliveTimeUnit) {
		this.keepAliveTimeUnit = keepAliveTimeUnit;
	}
}
