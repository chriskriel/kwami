package net.kwami.pipe;

import java.nio.ByteBuffer;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;

public class FifoEchoServer {

	public static void main(String[] args) throws Exception {
		Message msg = null;
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try (Pipe pipe = new FifoPipe(null, "test/echo.requests", "test/echo.responses")) {
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
