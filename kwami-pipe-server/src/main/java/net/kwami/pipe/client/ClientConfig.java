package net.kwami.pipe.client;

import com.google.gson.GsonBuilder;

public class ClientConfig {
	private int maxOutstanding = 500;
	private int maxTransmitQueue = 50;

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
	}

	public int getMaxOutstanding() {
		return maxOutstanding;
	}

	public void setMaxOutstanding(int maxOutstanding) {
		this.maxOutstanding = maxOutstanding;
	}

	public int getMaxTransmitQueue() {
		return maxTransmitQueue;
	}

	public void setMaxTransmitQueue(int maxTransmitQueue) {
		this.maxTransmitQueue = maxTransmitQueue;
	}
}
