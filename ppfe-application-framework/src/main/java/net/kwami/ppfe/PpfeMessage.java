package net.kwami.ppfe;

import net.kwami.utils.ParameterBuffer;

public class PpfeMessage {
	private Object context;
	private ParameterBuffer data;

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

	public ParameterBuffer getData() {
		return data;
	}

	public void setData(ParameterBuffer data) {
		this.data = data;
	}
}
