package net.kwami.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ObjectReceiverTester implements ObjectReceiverListener<TestObj>{

	@Override
	public void processObject(TestObj obj) {
		System.out.println("process event where x=" + obj.x);
		
	}

	public static void main(String[] args) {
		ObjectReceiverTester me = new ObjectReceiverTester();
		ObjectReceiver<TestObj> objectReceiver = new ObjectReceiver<>(me);
		objectReceiver.start();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input = null;
			while ((input = reader.readLine()) != null) {
				if (input.equalsIgnoreCase("go")) {
					objectReceiver.go(new TestObj());
					continue;
				} else if (input.equalsIgnoreCase("end")) {
					objectReceiver.kill();
					objectReceiver.interrupt();
					objectReceiver.join();
					break;
				}
				System.out.println("go or end");
			}
			System.out.println("main is ending");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
