package net.kwami.ppfe;

public class Outcome {
	private ReturnCode returnCode;
	private String message;

	public Outcome() {
		super();
	}

	public Outcome(ReturnCode returnCode) {
		super();
		this.returnCode = returnCode;
		this.message = "";
	}

	public Outcome(ReturnCode returnCode, String message) {
		super();
		this.returnCode = returnCode;
		this.message = message;
	}

	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(ReturnCode returnCode) {
		this.returnCode = returnCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Outcome [returnCode=" + returnCode + ", message=" + message + "]";
	}

}
