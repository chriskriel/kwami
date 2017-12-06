package net.kwami.ppfe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThreadData {
	private HttpServletRequest request;
	private HttpServletResponse response;
	private List<PpfeApplication> applications;
	private Deque<String> inputStack;
	private Deque<String> outputStack;
	private Deque<String> appNameStack;

	public ThreadData() {
		super();
		inputStack = new ArrayDeque<String>();
		outputStack = new ArrayDeque<String>();
		appNameStack = new ArrayDeque<String>();
		applications = new ArrayList<PpfeApplication>();
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public List<PpfeApplication> getApplications() {
		return applications;
	}

	public void setApplications(List<PpfeApplication> applications) {
		this.applications = applications;
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

}
