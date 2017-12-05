package net.kwami.ppfe;

import com.google.gson.GsonBuilder;

import net.kwami.utils.MyProperties;

public class PpfeRequest {
	private Object context;
	private MyProperties data;

	public PpfeRequest() {
		super();
		data = new MyProperties();
	}
	
	public void clear() {
		data.clear();
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

	@Override
	public String toString() {
		return new GsonBuilder().disableHtmlEscaping().create().toJson(this);
	}
}
