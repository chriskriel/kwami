package net.kwami.pipe.client;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		String remoteHost = "127.0.0.1";
		InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteHost, 58080);
		System.out.println(remoteSocketAddress.toString());
	}
}
