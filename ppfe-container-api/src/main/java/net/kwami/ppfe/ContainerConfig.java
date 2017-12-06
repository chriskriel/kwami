package net.kwami.ppfe;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;

public class ContainerConfig {
	private Map<String, String> containers = new HashMap<>();
	private Map<String, Application> applications = new HashMap<>();
	private Map<String, Destination> destinations = new HashMap<>();

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}

	public Map<String, Destination> getDestinations() {
		return destinations;
	}

	public void setDestinations(Map<String, Destination> destinations) {
		this.destinations = destinations;
	}

	public void addApplication(String name, Application application) {
		applications.put(name, application);
	}

	public Application getApplication(String name) {
		return applications.get(name);
	}

	public void addDestination(String name, Destination destination) {
		destinations.put(name, destination);
	}

	public Destination getDestination(String name) {
		return destinations.get(name);
	}

	public Map<String, String> getContainers() {
		return containers;
	}

	public void setContainers(Map<String, String> containers) {
		this.containers = containers;
	}

	public void addContainer(String name, String className) {
		containers.put(name, className);
	}

	public String getContainer(String name) {
		return containers.get(name);
	}
}
