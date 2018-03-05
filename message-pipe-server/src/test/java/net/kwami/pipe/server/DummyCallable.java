package net.kwami.pipe.server;

import net.kwami.pipe.Message;

public class DummyCallable implements MyCallable {
	private CallableMessage parameter;

	@Override
	public void setParameter(CallableMessage parameter) {
		this.parameter = parameter;
	}

	@Override
	public CallableMessage call() throws Exception {
		Message msg = parameter.getMsg();
		msg.setData(msg.getData() + " was seen by container");
		return parameter;
	}


}
