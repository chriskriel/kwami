package net.kwami.pipe;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TcpEchoServer {

	public static void main(String[] args) throws Exception {
		Message msg = null;
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress("0.0.0.0", 58080));
		SocketChannel socketChannel = serverChannel.accept();
		try (Pipe pipe = new TcpPipe(null, socketChannel)) {
			while (true) {
				try {
					msg = pipe.read();
				} catch (Exception e) {
					if (e.getMessage().equals(Pipe.END_OF_STREAM)) {
						System.out.println("client closed connection");
						break;
					}
					throw e;
				}
				String data = msg.getData();
				data += ", back from server new";
				msg.setData(data);
				pipe.write(msg);
			}
		}
	}

}
