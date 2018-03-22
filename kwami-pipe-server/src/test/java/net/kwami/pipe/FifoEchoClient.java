package net.kwami.pipe;

public class FifoEchoClient {

	public static void main(String[] args) throws Exception {
		Message msg = new Message(10000, "");
		try (Pipe pipe = new FifoPipe(null, 
				"/home/chris/git/kwami/general/kwami-pipe-server/work/echo.responses", 
				"/home/chris/git/kwami/general/kwami-pipe-server/work/echo.requests")) {
			for (int i = 0; i < 1000; i++) {
				msg.setData("message no: " + String.valueOf(i));
				pipe.write(msg);
				msg.setId(msg.getId() + 1);;
			}
			for (int i = 0; i < 1000; i++) {
				msg = pipe.read();
				System.out.printf("id=%d,len=%d,text='%s'\n", msg.getId(), msg.getData().length(), msg.getData());
			}
		}
	}
}
