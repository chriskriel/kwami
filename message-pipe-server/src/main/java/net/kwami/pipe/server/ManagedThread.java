package net.kwami.pipe.server;

public abstract class ManagedThread extends Thread {
	protected boolean mustRun = true;
	protected boolean mustBlock = false;

	public void terminate() {
		mustRun = false;
		this.interrupt();
	}

	public void block() {
		this.mustBlock = true;
	}

	public void unblock() {
		this.notify();
	}
}
