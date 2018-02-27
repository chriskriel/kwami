package net.kwami.pipe;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.pipe.TcpPipe;

public class TcpEchoServer {

	public static void main(String[] args) throws Exception {
		Message msg = null;
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress("0.0.0.0", 58080));
		SocketChannel socketChannel = serverChannel.accept();
		try (MessagePipe pipe = new TcpPipe(null, socketChannel)) {
			while (true) {
				try {
					msg = pipe.read(buffer);
				} catch (Exception e) {
					if (e.getMessage().equals(MessagePipe.END_OF_STREAM)) {
						System.out.println("client closed connection");
						break;
					}
					throw e;
				}
				String data = msg.getData();
				data += ", back from server new";
				msg.setData(data);
				pipe.write(buffer, msg);
			}
		}
	}

}
