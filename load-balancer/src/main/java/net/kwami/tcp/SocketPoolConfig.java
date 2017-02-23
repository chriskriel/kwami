package net.kwami.tcp;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.google.gson.GsonBuilder;

public class SocketPoolConfig extends GenericObjectPoolConfig {
	private String serverIp;
	private int serverPort;
	private int connectTimeoutMs;
	private int readTimeoutMs;

	public SocketPoolConfig() {
		super();
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public int getReadTimeoutMs() {
		return readTimeoutMs;
	}

	public void setReadTimeoutMs(int readTimeoutMs) {
		this.readTimeoutMs = readTimeoutMs;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public static void main(String[] args) {
		SocketPoolConfig config = new SocketPoolConfig();
		config.connectTimeoutMs = 60000;
		config.readTimeoutMs = 10000;
		config.serverIp = "120.0.0.1";
		config.serverPort = 64646;
		config.setBlockWhenExhausted(true);
		config.setMaxIdle(10);
		System.out.println(config.toString());
	}

}
