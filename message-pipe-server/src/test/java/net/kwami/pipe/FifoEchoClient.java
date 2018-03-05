package net.kwami.pipe;

import java.nio.ByteBuffer;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;

public class FifoEchoClient {

	public static void main(String[] args) throws Exception {
		Message msg = new Message(10000, "");
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try (Pipe pipe = new FifoPipe(null, 
				"/home/chris/git/kwami/general/message-pipe-server/test/echo.responses", 
				"/home/chris/git/kwami/general/message-pipe-server/test/echo.requests")) {
			for (int i = 0; i < 1000; i++) {
				msg.setData("message no: " + String.valueOf(i));
				pipe.write(buffer, msg);
				msg.setId(msg.getId() + 1);;
			}
			for (int i = 0; i < 1000; i++) {
				msg = pipe.read(buffer);
				System.out.printf("id=%d,len=%d,text='%s'\n", msg.getId(), msg.getData().length(), msg.getData());
			}
		}
	}
}
