package net.kwami.pathsend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.GsonBuilder;
import com.tandem.tsmp.TsmpServer;

import net.kwami.utils.MemoryDumpMessage;
import net.kwami.utils.MyProperties;

public final class PathwayClient {

	private static final Logger LOGGER = LogManager.getLogger();
	private final long latencyThresholdMillis;
	private final int timeoutCentiSecs;
	private final byte[] receiveBuffer;
	private final String serverPath;
	private final ParameterBuffer requestParameterBuffer;
	private final TsmpServer tsmpServer;

	public PathwayClient(String serverPath, int timeoutSecsX100, long latencyThresholdMillis) {
		this(serverPath, timeoutSecsX100, latencyThresholdMillis, Short.MAX_VALUE / 16, Short.MAX_VALUE / 16);
	}

	public PathwayClient(String serverPath, int timeoutSecsX100, long latencyThresholdMillis, int requestBufSize,
			int receiveBufSize) {
		super();
		LOGGER.debug("serverPath={},timeoutCentiSecs={},latencyThreshold={}ms,requestBufSize={},receiveBufSize={}",
				serverPath, timeoutSecsX100, latencyThresholdMillis, requestBufSize, receiveBufSize);
		receiveBuffer = new byte[receiveBufSize];
		requestParameterBuffer = new ParameterBuffer(0, requestBufSize);
		this.timeoutCentiSecs = timeoutSecsX100;
		this.latencyThresholdMillis = latencyThresholdMillis;
		this.serverPath = serverPath;
		String[] serverPathParts = serverPath.split("\\.");
		String pathmonName = serverPathParts[0].trim().toUpperCase();
		String serverName = serverPathParts[1].trim().toUpperCase();
		tsmpServer = new TsmpServer(pathmonName, serverName);
		tsmpServer.setTimeout(timeoutCentiSecs);
	}

	public final void transceive(int messageId, MyProperties requestParameters, MyProperties responseParameters)
			throws Exception {
		LOGGER.traceEntry("msgId={},requestParameters={}", messageId, requestParameters.toString());
		requestParameterBuffer.initialize(messageId, requestParameters);
		long latency = 0, startTime = System.currentTimeMillis();
		int requestLength = requestParameterBuffer.position();
		byte[] requestBuffer = requestParameterBuffer.array();
		LOGGER.trace(new MemoryDumpMessage("requestBytes:", requestBuffer, requestLength));
		int responseLength = tsmpServer.service(requestBuffer, requestLength, receiveBuffer);

		if (responseLength > receiveBuffer.length) {
			String errorMsg = String.format("failure: responseLength of %d > receiveBufSize of %d", responseLength,
					receiveBuffer.length);
			throw new Exception(errorMsg);
		}
		LOGGER.trace(new MemoryDumpMessage("responseBytes", receiveBuffer, responseLength));
		latency = System.currentTimeMillis() - startTime;
		ParameterBuffer respBuf = ParameterBuffer.wrap(receiveBuffer, 0, responseLength);
		if (latency > latencyThresholdMillis)
			LOGGER.warn("Latency of {}ms on server {} exceeded threshold of {}ms", latency, serverPath,
					latencyThresholdMillis);
		respBuf.extractPropertiesInto(responseParameters);
	}

	public final long getLatencyThresholdMillis() {
		return latencyThresholdMillis;
	}

	public final int getTimeoutCentiSecs() {
		return timeoutCentiSecs;
	}

	@Override
	public final String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public final String getServerPath() {
		return serverPath;
	}
}
