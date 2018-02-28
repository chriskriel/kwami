package net.kwami.pipe.server;

public class DummyCallable implements StringCallable {
	private String parameter;

	@Override
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public String call() throws Exception {
		return parameter + " was seen by container";
	}


}
