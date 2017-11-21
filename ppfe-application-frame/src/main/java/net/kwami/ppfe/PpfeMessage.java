package net.kwami.ppfe;

import net.kwami.utils.ParameterBuffer;

public class PpfeMessage {
	private Object context;
	private Outcome outcome;
	private ParameterBuffer data;

	public PpfeMessage() {
		super();
		outcome = new Outcome(ReturnCode.SUCCESS);
	}
	
	public PpfeMessage(PpfeMessage from) {
		this.context = from.context;
		this.outcome = from.outcome;
		this.data = from.data;
	}

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

	public Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}
}
