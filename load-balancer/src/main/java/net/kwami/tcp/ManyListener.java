package net.kwami.tcp;

import java.util.ArrayList;
import java.util.List;

public class ManyListener {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: java ManyListener [port ]+");
			return;
		}
		List<Listener> listeners = new ArrayList<Listener>();
		for (int i = 0; i < args.length; i++) {
			try {
				Listener lt = new Listener(args[i]);
				listeners.add(lt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		while (true) {
			try {
				Thread.sleep(120000L);
			} catch (InterruptedException e) {
			}
		}
	}
}
