package net.kwami.pipe;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.pipe.TcpPipe;

public class TcpEchoClient {

	public static void main(String[] args) throws Exception {
		Message msg = new Message(10000, "");
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 58080));
		try (MessagePipe pipe = new TcpPipe(null, socketChannel)) {
			for (int i = 0; i < 10; i++) {
				msg.setData("message no: " + String.valueOf(i));
				pipe.write(buffer, msg);
				msg.setId(msg.getId() + 1);
			}
			for (int i = 0; i < 10; i++) {
				msg = pipe.read(buffer);
				System.out.printf("id=%d,len=%d,text='%s'\n", msg.getId(), msg.getData().length(), msg.getData());
			}
		}
	}
}
