package net.kwami.pipe;

import java.nio.ByteBuffer;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;

public class FifoEchoClient {

	public static void main(String[] args) throws Exception {
		Message msg = new Message(10000, "");
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try (MessagePipe pipe = new FifoPipe(null, 
				"/home/chris/git/kwami/general/message-pipe-server/fifo/EchoClient.responses", 
				"/home/chris/git/kwami/general/message-pipe-server/fifo/EchoClient.requests")) {
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
