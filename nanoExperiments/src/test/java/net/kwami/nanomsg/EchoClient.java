package net.kwami.nanomsg;

import static org.junit.Assert.*;

import org.junit.Test;

import nanomsg.reqrep.ReqSocket;

public class EchoClient {

	@Test
	public void test() {
		ReqSocket sock = new ReqSocket();
		sock.connect("ipc:///tmp/sock");

		for (int i = 0; i < 5; i++) {
			sock.send("Hello!" + i);
			System.out.println("Received:" + sock.recvString());
		}
		sock.close();
		assertNotNull(sock);
	}
}
