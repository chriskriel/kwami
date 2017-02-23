package net.kwami.tcp;

import static org.junit.Assert.*;

import org.junit.Test;

public class JsonTester {

	@Test
	public void testSocketPoolConfig() {
		SocketPoolConfig config = new SocketPoolConfig();
		config.setConnectTimeoutMs(60000);
		config.setReadTimeoutMs(10000);
		config.setServerIp("120.0.0.1");
		config.setServerPort(64646);
		config.setBlockWhenExhausted(true);
		config.setMaxIdle(10);
		String json = config.toString();
		assertNotNull("no json string", json);
		System.out.println("SocketPoolConfig = " + json);
	}

	@Test
	public void testListener() {
		Listener config = new Listener();
		String json = config.toString();
		assertNotNull("no json string", json);
		System.out.println("Listener = " + json);
	}

}
