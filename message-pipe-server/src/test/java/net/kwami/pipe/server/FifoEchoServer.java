package net.kwami.pipe.server;

import java.nio.ByteBuffer;

import net.kwami.pipe.server.FifoPipe;
import net.kwami.pipe.server.Message;
import net.kwami.pipe.server.MessagePipe;

public class FifoEchoServer {

	public static void main(String[] args) throws Exception {
		Message msg = null;
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try (MessagePipe pipe = new FifoPipe(null, "fifo/EchoClient.requests", "fifo/EchoClient.responses")) {
			while (true) {
				msg = pipe.read(buffer);
				String data = msg.getData();
				data += ", back from server new";
				msg.setData(data);
				pipe.write(buffer, msg);
			}
		}
	}

}
