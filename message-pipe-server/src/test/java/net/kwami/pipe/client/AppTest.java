package net.kwami.pipe.client;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.server.Command;

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
		String remoteHost = InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS).getHostAddress();
		InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteHost, 58080);
		RemoteEndpoint endpoint = new RemoteEndpoint(48080, remoteSocketAddress);
		PipeClient client = new PipeClient(endpoint, 10);
		client.sendRequest("one message", 10);
	}
}
