package net.kwami.pathsend;

import com.google.gson.GsonBuilder;
import com.tandem.tsmp.TsmpServer;

import net.kwami.utils.HexDumper;
import net.kwami.utils.MyLogger;
import net.kwami.utils.ParameterBuffer;

public class PathwayClient {

	private static final MyLogger logger = new MyLogger(PathwayClient.class);
	private long latencyThresholdMillis;
	private int timeoutCentiSecs = 100;
	private int receiveBufSize;

	public PathwayClient() {
		super();
	}

	public PathwayClient(int timeoutSecsX100, long latencyThresholdMillis) {
		this(timeoutSecsX100, latencyThresholdMillis, Short.MAX_VALUE / 16);
	}

	public PathwayClient(int timeoutSecsX100, long latencyThresholdMillis, int receiveBufSize) {
		super();
		this.receiveBufSize = receiveBufSize;
		this.timeoutCentiSecs = timeoutSecsX100;
		this.latencyThresholdMillis = latencyThresholdMillis;
		logger.debug("timeoutCentiSecs=%d, latencyThreshold=%dms", timeoutSecsX100, latencyThresholdMillis);
	}

	public ParameterBuffer transceive(String serverPath, ParameterBuffer reqBuf) throws Exception {
		logger.debug("serverPath=%s,msgId=%d", serverPath, reqBuf.getMsgId());
		HexDumper hexDumper = new HexDumper();
		String[] serverPathParts = serverPath.split("\\.");
		String pathmonName = serverPathParts[0].trim().toUpperCase();
		String serverName = serverPathParts[1].trim().toUpperCase();
		long latency = 0, startTime = System.currentTimeMillis();
		byte[] receiveBuffer = new byte[receiveBufSize];
		byte[] payload = reqBuf.toByteArray();
		logger.trace("requestBytes:(length=%d)\n%s", payload.length, hexDumper.buildHexDump(payload));

		TsmpServer server = new TsmpServer(pathmonName, serverName);
		server.setTimeout(timeoutCentiSecs);
		int responseLength = server.service(payload, payload.length, receiveBuffer);

		if (responseLength > receiveBufSize) {
			String errorMsg = String.format("failure: responseLength of %d > receiveBufSize of %d", responseLength,
					receiveBufSize);
			throw new Exception(errorMsg);
		}
		logger.trace("responseBytes:(length=%d)\n%s", responseLength,
				hexDumper.buildHexDump(receiveBuffer, responseLength));
		latency = System.currentTimeMillis() - startTime;
		ParameterBuffer respBuf = ParameterBuffer.wrap(receiveBuffer, 0, responseLength);
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

	public int getReceiveBufSize() {
		return receiveBufSize;
	}

	public void setReceiveBufSize(int receiveBufSize) {
		this.receiveBufSize = receiveBufSize;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
}
