package net.kwami.ppfe;

public class Destination {

	public static class Remote {
		private String scheme;
		private String hostName;
		private int port;

		public Remote() {
		}

		public String getScheme() {
			return scheme;
		}

		public void setScheme(String scheme) {
			this.scheme = scheme;
		}

		public String getHostName() {
			return hostName;
		}

		public void setHostName(String hostName) {
			this.hostName = hostName;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

	}

	private String applicationName;
	private String uri;
	private int clientTimeoutMillis = 15000;
	private int latencyThresholdMillis = 8000;
	private Remote remote;

	public Destination() {
		super();
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Remote getRemote() {
		return remote;
	}

	public void setRemote(Remote remote) {
		this.remote = remote;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getClientTimeoutMillis() {
		return clientTimeoutMillis;
	}

	public void setClientTimeoutMillis(int clientTimeoutMillis) {
		this.clientTimeoutMillis = clientTimeoutMillis;
	}

	public int getLatencyThresholdMillis() {
		return latencyThresholdMillis;
	}

	public void setLatencyThresholdMillis(int latencyThresholdMillis) {
		this.latencyThresholdMillis = latencyThresholdMillis;
	}
}
