package net.kwami.ppfe;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import net.kwami.pathsend.PathwayClient;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyProperties;
import net.kwami.utils.ParameterBuffer;

public class PathwayTester extends Thread {

	public static void main(String[] args) {
		PathwayTester[] testThreads = new PathwayTester[10];
		for (int i = 0; i < 10; i++) {
			testThreads[i] = new PathwayTester();
			testThreads[i].setDaemon(true);
			testThreads[i].setName("T H R E A D   " + String.valueOf(i));
			testThreads[i].start();
		}
		for (int i = 0; i < 10; i++) {
			try {
				testThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		for (int i = 0; i < 10; i++) {
			long now = System.currentTimeMillis();
			sendRequest();
			System.out.println(Thread.currentThread().getName() + " :   L A T E N C Y :   "
					+ String.valueOf(System.currentTimeMillis() - now));
		}
	}

	private static void sendRequest() {
		PpfeMessage request = new PpfeMessage();
		MyProperties parameters = new MyProperties();
		parameters.setProperty("SQL", "select * from users where username = ? browse access");
		parameters.setProperty("PARM-CNT", "1");
		parameters.setProperty("P0", "ckriel");
		request.setData(parameters);
		PpfeMessage response = sendRequest("Router", request, 60000);
		if (response.getOutcome().getReturnCode() == ReturnCode.SUCCESS)
			System.out.println(response.getData().getProperty("SQL-RESULT"));
		else if (response.getOutcome().getReturnCode() == ReturnCode.TIMEOUT)
			System.out.println("timed out!");
		else
			System.out.println("Failed: " + response.getOutcome().getMessage());
	}

	public static PpfeMessage sendRequest(String destinationName, PpfeMessage message, long timeoutMillis) {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		PpfeMessage response = new PpfeMessage();
		Outcome outcome = response.getOutcome();
		Destination destSelected = null;
		for (Destination dest : config.getDestinations()) {
			if (dest.getName().equals(destinationName)) {
				destSelected = dest;
				break;
			}
		}
		if (destSelected == null) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(String.format("Destination '%s' was requested but there is no configuration for it",
					destinationName));
			return response;
		}
		ParameterBuffer requestBuffer = toParameterBuffer(message.getData());
		ParameterBuffer responseBuffer = null;
		int timeoutCentiSecs = Integer.parseInt(String.valueOf(timeoutMillis)) / 10;
		try {
			PathwayClient pwClient = new PathwayClient(timeoutCentiSecs, 120000);
			System.out.println("sending to " + destSelected.getUri());
			responseBuffer = pwClient.transceive(destSelected.getUri(), requestBuffer);
			MyProperties responseProperties = toProperties(responseBuffer);
			response.setData(responseProperties);
		} catch (Exception e) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
		}
		return response;
	}

	private static MyProperties toProperties(ParameterBuffer buffer) {
		MyProperties result = new MyProperties();
		Set<String> keys = buffer.keySet();
		for (String key : keys) {
			try {
				result.setProperty(key, buffer.getStringValue(key));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private static ParameterBuffer toParameterBuffer(MyProperties properties) {
		ParameterBuffer result = new ParameterBuffer((short) 0);
		for (String name : properties.stringPropertyNames()) {
			try {
				result.addParameter(name, properties.getProperty(name, ""), true);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
