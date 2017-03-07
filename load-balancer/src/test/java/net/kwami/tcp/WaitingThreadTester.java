package net.kwami.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WaitingThreadTester extends WaitingThread<String> {

	@Override
	protected void doTask(String data) throws Exception {
		System.out.println("processing " + data);		
	}

	public static void main(String[] args) {
		try {
			WaitingThreadTester waitingThread = new WaitingThreadTester();
			waitingThread.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input = null;
			while ((input = reader.readLine()) != null) {
				if (input.startsWith("process ")) {
					waitingThread.process(input.split(" ")[1]);
				} else if (input.equalsIgnoreCase("end")) {
					waitingThread.kill(true);
					break;
				}
			}
			System.out.println("main is ending");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
