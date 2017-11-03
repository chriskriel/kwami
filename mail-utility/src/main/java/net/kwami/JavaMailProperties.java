package net.kwami;

import com.google.gson.GsonBuilder;

import net.kwami.utils.MyProperties;

public class JavaMailProperties {

	private MyProperties smtpProperties = new MyProperties();
	private String fromAddress;

	public MyProperties getSmtpProperties() {
		return smtpProperties;
	}

	public void setSmtpProperties(MyProperties smtpProperties) {
		this.smtpProperties = smtpProperties;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
	}
}
