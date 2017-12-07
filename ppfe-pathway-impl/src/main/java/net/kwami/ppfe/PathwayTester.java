package net.kwami.ppfe;

import net.kwami.pathsend.PathwayClient;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyProperties;

public class PathwayTester extends Thread {
	private int msgPerThread;

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: ./test.sh <threads> <messagesPerThread>");
			return;
		}
		int threads = Integer.parseInt(args[0]);
		int msgPerThread = Integer.parseInt(args[1]);
		PathwayTester[] testThreads = new PathwayTester[threads];
		for (int i = 0; i < threads; i++) {
			testThreads[i] = new PathwayTester();
			testThreads[i].setDaemon(true);
			testThreads[i].setName("tester-" + String.valueOf(i));
			testThreads[i].msgPerThread = msgPerThread;
			testThreads[i].start();
		}
		for (int i = 0; i < threads; i++) {
			try {
				testThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		String destinationName = "Router";
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		PpfeResponse response = new PpfeResponse();
		Outcome outcome = response.getOutcome();
		Destination destSelected = config.getDestinations().get(destinationName);
		if (destSelected == null) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(String.format("Destination '%s' was requested but there is no configuration for it",
					destinationName));
			return;
		}
		int timeoutCentiSecs = Integer.parseInt(String.valueOf(60000)) / 10;
		PathwayClient pwClient = new PathwayClient(destSelected.getUri(), timeoutCentiSecs, 120000);
		PpfeRequest request = new PpfeRequest();
		MyProperties parameters = request.getData();
		parameters.setProperty("SQL", "select * from users where username = ? browse access");
		parameters.setProperty("PARM-CNT", "1");
		parameters.setProperty("P0", "ckriel");
		request.setData(parameters);
		for (int i = 0; i < msgPerThread; i++) {
			long now = System.currentTimeMillis();
			sendRequest(pwClient, request, response);
			System.out.println(Thread.currentThread().getName() + " :   L A T E N C Y :   "
					+ String.valueOf(System.currentTimeMillis() - now));
		}
	}

	private static void sendRequest(PathwayClient pwClient, PpfeRequest request, PpfeResponse response) {
		response.clear();
		Outcome outcome = response.getOutcome();
		try {
			System.out.println("sending to " + pwClient.getServerPath());
			pwClient.transceive(0, request.getData(), response.getData());
		} catch (Exception e) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
		}
		if (response.getOutcome().getReturnCode() == ReturnCode.SUCCESS)
			System.out.println(response.getData().getProperty("SQL-RESULT"));
		else if (response.getOutcome().getReturnCode() == ReturnCode.TIMEOUT)
			System.out.println("timed out!");
		else
			System.out.println("Failed: " + response.getOutcome().getMessage());
	}
}
