package net.kwami.pathsend;

import com.google.gson.GsonBuilder;
import com.tandem.tsmp.TsmpServer;

import net.kwami.utils.HexDumper;
import net.kwami.utils.MyLogger;

public class PathwayClient {

	private static final MyLogger logger = new MyLogger(PathwayClient.class);
	private long latencyThresholdMillis;
	private int timeoutCentiSecs = 100;

	public PathwayClient() {
		super();
	}

	public PathwayClient(int timeoutSecsX100, long latencyThresholdMillis) {
		super();
		this.timeoutCentiSecs = timeoutSecsX100;
		this.latencyThresholdMillis = latencyThresholdMillis;
		logger.debug("timeoutCentiSecs=%d, latencyThreshold=%dms", latencyThresholdMillis, timeoutSecsX100);
	}

	public PpfeParameterBuffer transceive(String serverPath, PpfeParameterBuffer reqBuf) throws Exception {
		logger.debug("serverPath={},msgId={}", serverPath, reqBuf.getMsgId());
		HexDumper hexDumper = new HexDumper();
		String[] serverPathParts = serverPath.split(":");
		String pathmonName = serverPathParts[0].trim().toUpperCase();
		String serverName = serverPathParts[1].trim().toUpperCase();
		long latency = 0, startTime = System.currentTimeMillis();
		byte[] receiveBuffer = new byte[32767];
		byte[] payload = reqBuf.toByteArray();
		logger.trace("payload.length=%d", payload.length);
		logger.trace("requestBytes=%s", hexDumper.buildHexDump(payload));
		TsmpServer server = new TsmpServer(pathmonName, serverName);
		server.setTimeout(timeoutCentiSecs);
		int responseLength = server.service(payload, payload.length, receiveBuffer);
		latency = System.currentTimeMillis() - startTime;
		PpfeParameterBuffer respBuf = PpfeParameterBuffer.wrap(receiveBuffer, 0, responseLength);
		byte[] responseBytes = respBuf.toByteArray();
		logger.trace("responseBytes=%s", hexDumper.buildHexDump(responseBytes));
		if (latency > latencyThresholdMillis)
			logger.warn("Latency of %dms on server %s:%s exceeded threshold of %dms", latency, pathmonName, serverName,
					latencyThresholdMillis);
		return respBuf;
	}

	public long getLatencyThresholdMillis() {
		return latencyThresholdMillis;
	}

	public void setLatencyThresholdMillis(long latencyThresholdMillis) {
		this.latencyThresholdMillis = latencyThresholdMillis;
	}

	public int getTimeoutCentiSecs() {
		return timeoutCentiSecs;
	}

	public void setTimeoutCentiSecs(int timeoutCentiSecs) {
		this.timeoutCentiSecs = timeoutCentiSecs;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
}
