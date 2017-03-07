package net.kwami.tcp;

public interface ObjectReceiverListener<T> {
	
	public void processObject(T obj);
}
