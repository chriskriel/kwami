package net.kwami.pipe;

public class FifoEchoServer {

	public static void main(String[] args) throws Exception {
		Message msg = null;
		try (Pipe pipe = new FifoPipe(null, "work/echo.requests", "work/echo.responses")) {
			while (true) {
				msg = pipe.read();
				String data = msg.getData();
				data += ", back from server new";
				msg.setData(data);
				pipe.write(msg);
			}
		}
	}

}
