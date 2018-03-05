package net.kwami.pipe.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

	@Before
	public void setUp() throws Exception {
//		Runtime.getRuntime().exec("java net.kwami.pipe.server.Server&");
	}

	@After
	public void tearDown() throws Exception {
//		Command c = new Command(Command.Cmd.SHUTDOWN);
//		String remoteHost = InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS).getHostAddress();
//		InetSocketAddress endpoint = new InetSocketAddress(remoteHost, 58080);
//		ByteBuffer buffer = ByteBuffer.allocate(128);
//		SocketChannel socketChannel = SocketChannel.open(endpoint);
//		buffer.put(c.getBytes());
//		buffer.flip();
//		socketChannel.write(buffer);
//		buffer.clear();
//		socketChannel.read(buffer);
	}

	@Test
	public void testEcho() throws Exception {
	}
}
