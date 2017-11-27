package net.kwami.ppfe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tester {

	public static void main(String[] args) {
		tomcat();
		pathway();
	}
	
	private static void pathway() {
		ContainerConfig config = new ContainerConfig();
		Application app = new Application();
		app.setName("router");
		app.setClassName("net.kwami.ppfe.RelayApplication");
		config.addApplication(app);
		app = new Application();
		app.setName("sqlInterpreter");
		app.setClassName("net.kwami.ppfe.SqlInterpreter");
		config.addApplication(app);
		Destination dest = new Destination();
		dest.setName("localRouter");
		dest.setApplicationName("router");
		dest.setUri("/localRouter");
		dest.setLatencyThresholdMillis(8000);
		dest.setClientTimeoutMillis(20000);
		config.addDestination(dest);
		dest = new Destination();
		dest.setName("remoteSql");
		dest.setApplicationName("sqlInterpreter");
		dest.setLatencyThresholdMillis(8000);
		dest.setClientTimeoutMillis(20000);
		Destination.Remote remoteDest = new Destination.Remote();
		remoteDest.setScheme("http");
		remoteDest.setHostName("host2.bcx.co.za");
		remoteDest.setPort(18080);
		dest.setUri("/ppfe");
		dest.setRemote(remoteDest);
		config.addDestination(dest);
		dest.setLatencyThresholdMillis(8000);
		dest.setClientTimeoutMillis(20000);
		String json = config.toString();
		System.out.println(json);
		Gson gson = new GsonBuilder().create();
		config = gson.fromJson(json, ContainerConfig.class);
		System.out.println(json);	
	}
	
	private static void tomcat() {
		ContainerConfig config = new ContainerConfig();
		Application app = new Application();
		app.setName("router");
		app.setClassName("net.kwami.ppfe.RelayApplication");
		config.addApplication(app);
		app = new Application();
		app.setName("sqlInterpreter");
		app.setClassName("net.kwami.ppfe.SqlInterpreter");
		config.addApplication(app);
		Destination dest = new Destination();
		dest.setName("localRouter");
		dest.setApplicationName("router");
		dest.setUri("/localRouter");
		config.addDestination(dest);
		dest = new Destination();
		dest.setName("remoteSql");
		dest.setApplicationName("sqlInterpreter");
		Destination.Remote remoteDest = new Destination.Remote();
		remoteDest.setScheme("http");
		remoteDest.setHostName("host2.bcx.co.za");
		remoteDest.setPort(18080);
		dest.setUri("/ppfe");
		dest.setRemote(remoteDest);
		config.addDestination(dest);
		String json = config.toString();
		System.out.println(json);
		Gson gson = new GsonBuilder().create();
		config = gson.fromJson(json, ContainerConfig.class);
		System.out.println(json);	
	}
}
