package net.kwami.pipe.server;

import java.util.concurrent.Callable;

public class DummyCallable implements Callable<String> {
	private String request;

	public DummyCallable(String request) {
		super();
		this.request = request;
	}

	@Override
	public String call() throws Exception {
		return request + ", was seen by container";
	}


}
