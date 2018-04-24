package net.kwami.pipe;

import static org.junit.Assert.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class TestHostAddress {

	@Test
	public void test() {
		try {
			System.out.println(Inet4Address.getByName("machine-address").getHostAddress());
			System.out.println(InetAddress.getByName("machine-address").getHostAddress());
		} catch (UnknownHostException e) {
			fail(e.toString());
		}
	}

}
