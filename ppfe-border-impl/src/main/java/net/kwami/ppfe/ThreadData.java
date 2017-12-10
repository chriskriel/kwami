package net.kwami.ppfe;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThreadData {
	private HttpServletRequest httpRequest;
	private HttpServletResponse httpResponse;
	private Map<String, PpfeApplication> applications;
	private Deque<String> inputStack;
	private Deque<String> outputStack;
	private Deque<String> appNameStack;

	public ThreadData() {
		super();
		inputStack = new ArrayDeque<String>();
		outputStack = new ArrayDeque<String>();
		appNameStack = new ArrayDeque<String>();
		applications = new HashMap<String, PpfeApplication>();
	}

	public Deque<String> getInputStack() {
		return inputStack;
	}

	public void setInputStack(Deque<String> inputStack) {
		this.inputStack = inputStack;
	}

	public Deque<String> getOutputStack() {
		return outputStack;
	}

	public void setOutputStack(Deque<String> outputStack) {
		this.outputStack = outputStack;
	}

	public Deque<String> getAppNameStack() {
		return appNameStack;
	}

	public void setAppNameStack(Deque<String> appNameStack) {
		this.appNameStack = appNameStack;
	}

	public Map<String, PpfeApplication> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, PpfeApplication> applications) {
		this.applications = applications;
	}
	
	public ThreadData addApplication(String key, PpfeApplication value) {
		applications.put(key, value);
		return this;
	}

	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpServletRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public HttpServletResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpServletResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

}
