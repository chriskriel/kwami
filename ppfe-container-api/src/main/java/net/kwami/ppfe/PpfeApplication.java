package net.kwami.ppfe;

public abstract class PpfeApplication implements Runnable {
	private PpfeContainer container;

	public PpfeApplication() {
	}

	public PpfeContainer getContainer() {
		return container;
	}

	public void setContainer(PpfeContainer container) {
		this.container = container;
	}
}
