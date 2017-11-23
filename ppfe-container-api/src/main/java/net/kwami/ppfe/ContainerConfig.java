package net.kwami.ppfe;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;

public class ContainerConfig {
	private List<Application> applications = new ArrayList<>();
	private List<Destination> destinations = new ArrayList<>();

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
	}

	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}

	public List<Destination> getDestinations() {
		return destinations;
	}

	public void setDestinations(List<Destination> destinations) {
		this.destinations = destinations;
	}
	
	public void addApplication(Application application) {
		applications.add(application);
	}
	
	public void addDestination(Destination destination) {
		destinations.add(destination);
	}
}
