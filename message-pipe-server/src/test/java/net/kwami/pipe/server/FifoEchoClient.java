package net.kwami.pipe.server;

import java.nio.ByteBuffer;

import net.kwami.pipe.server.FifoPipe;
import net.kwami.pipe.server.Message;
import net.kwami.pipe.server.MessagePipe;

public class FifoEchoClient {

	public static void main(String[] args) throws Exception {
		Message msg = new Message(10000, "");
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try (MessagePipe pipe = new FifoPipe(null, "fifo/EchoClient.responses", "fifo/EchoClient.requests")) {
			for (int i = 0; i < 10; i++) {
				msg.setData("message no: " + String.valueOf(i));
				pipe.write(buffer, msg);
				msg.setId(msg.getId() + 1);;
			}
			for (int i = 0; i < 10; i++) {
				msg = pipe.read(buffer);
				System.out.printf("id=%d,len=%d,text='%s'\n", msg.getId(), msg.getData().length(), msg.getData());
			}
		}
	}
}
