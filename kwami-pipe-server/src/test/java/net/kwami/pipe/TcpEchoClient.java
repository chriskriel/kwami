package net.kwami.pipe;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TcpEchoClient {

	public static void main(String[] args) throws Exception {
		Message msg = new Message(10000, "");
		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 58080));
		try (Pipe pipe = new TcpPipe(null, socketChannel)) {
			for (int i = 0; i < 10; i++) {
				msg.setData("message no: " + String.valueOf(i));
				pipe.write(msg);
				msg.setId(msg.getId() + 1);
			}
			for (int i = 0; i < 10; i++) {
				msg = pipe.read();
				System.out.printf("id=%d,len=%d,text='%s'\n", msg.getId(), msg.getData().length(), msg.getData());
			}
		}
	}
}
