package net.kwami.ppfe;

public class Application {
	private String name;
	private String className;
	private int maxRequestSize = 2048;
	private int maxResponseSize = 2048;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getMaxRequestSize() {
		return maxRequestSize;
	}

	public void setMaxRequestSize(int maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}

	public int getMaxResponseSize() {
		return maxResponseSize;
	}

	public void setMaxResponseSize(int maxResponseSize) {
		this.maxResponseSize = maxResponseSize;
	}
}
