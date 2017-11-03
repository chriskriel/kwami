package net.kwami;

import net.kwami.pathsend.PpfeParameterBuffer;

public class PpfeMessage {
	private Object context;
	private PpfeParameterBuffer data;

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

	public PpfeParameterBuffer getData() {
		return data;
	}

	public void setData(PpfeParameterBuffer data) {
		this.data = data;
	}
}
