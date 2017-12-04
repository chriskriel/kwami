package net.kwami.ppfe;

public abstract class PpfeApplication implements Runnable {
	private String appName = "unkown";
	private PpfeContainer container;

	public PpfeApplication() {
	}

	public PpfeContainer getContainer() {
		return container;
	}

	public void setContainer(PpfeContainer container) {
		this.container = container;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
}

