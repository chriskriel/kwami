package net.kwami.ppfe;

import net.kwami.utils.MyProperties;

public class PpfeRequest {
	private Object context;
	private MyProperties data;

	public PpfeRequest() {
		super();
	}

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

	public MyProperties getData() {
		return data;
	}

	public void setData(MyProperties data) {
		this.data = data;
	}
}
