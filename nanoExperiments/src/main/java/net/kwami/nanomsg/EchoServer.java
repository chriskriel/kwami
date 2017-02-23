package net.kwami.nanomsg;

import java.util.concurrent.TimeUnit;

import nanomsg.reqrep.RepSocket;

public class EchoServer {
	public static void main(String[] args) {
		RepSocket sock = new RepSocket();
		sock.bind("ipc:///tmp/sock");
		int timeout = (int) TimeUnit.MINUTES.toMillis(5);

		try {
			while (true) {
				byte[] receivedData = null;
				try {
					receivedData = sock.recvBytes(true);
				} catch (nanomsg.exceptions.IOException e) {
					if (e.getMessage() != null && e.getMessage().contains("timed out")) {
						sock.setRecvTimeout(timeout);
						continue;
					} else
						throw e;
				}
				sock.send(receivedData);
			}
		} finally {
			sock.close();
		}
	}
}