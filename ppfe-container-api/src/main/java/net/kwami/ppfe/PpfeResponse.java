package net.kwami.ppfe;

import net.kwami.utils.MyProperties;

public class PpfeResponse {
	private Outcome outcome;
	private MyProperties data;

	public PpfeResponse() {
		super();
		outcome = new Outcome(ReturnCode.SUCCESS);
		data = new MyProperties();	
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
