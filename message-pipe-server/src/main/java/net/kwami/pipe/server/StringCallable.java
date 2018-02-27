package net.kwami.pipe.server;

import java.util.concurrent.Callable;

public interface StringCallable extends Callable<String> {

	public void setParameter(String parameter);

}
