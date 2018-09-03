package net.kwami.pipe.server;

import java.util.concurrent.Callable;

public interface MyCallable extends Callable<CallableMessage> {

	public void setParameter(CallableMessage parameter);

}
