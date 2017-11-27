package net.kwami.ppfe;

import net.kwami.utils.MyProperties;

public class PpfeMessage {
	private Object context;
	private Outcome outcome;
	private MyProperties data = new MyProperties();

	public PpfeMessage() {
		super();
		outcome = new Outcome(ReturnCode.SUCCESS);
	}

	public PpfeMessage(PpfeMessage from) {
		this.outcome = from.outcome;
		this.data = from.data;
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

	public Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}
}
