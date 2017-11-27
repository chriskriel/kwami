package net.kwami.ppfe;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.tandem.ext.guardian.GError;
import com.tandem.ext.guardian.GuardianException;
import com.tandem.ext.guardian.Receive;
import com.tandem.ext.guardian.ReceiveInfo;
import com.tandem.ext.guardian.ReceiveNoOpeners;

import net.kwami.pathsend.PathwayClient;
import net.kwami.utils.Configurator;
import net.kwami.utils.HexDumper;
import net.kwami.utils.MyLogger;
import net.kwami.utils.MyProperties;
import net.kwami.utils.ParameterBuffer;

public class PathwayContainer implements PpfeContainer {
	private static final MyLogger LOGGER = new MyLogger(PathwayContainer.class);
	private boolean serverTerminating = false;
	private Receive $receive = null;
	private DataSource dataSource;

	public PathwayContainer() throws Exception {
		super();
		MyProperties properties = Configurator.get(MyProperties.class, "/Properties.js");
		String resourceName = properties.getProperty("datasourceConfig", "DevRms");
		PoolProperties pp = Configurator.get(PoolProperties.class, "/" + resourceName);
		DataSource ds = new DataSource();
		ds.setPoolProperties(pp);
		this.dataSource = ds;
	}
	
	public static void main(String[] args) {
		try {
			PathwayContainer me = new PathwayContainer();
			me.start();
			while (!me.serverTerminating) {
				Thread.sleep(10000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void start() {
		MyProperties properties = Configurator.get(MyProperties.class, "/Properties.js");
		int receiveDepth = properties.getIntProperty("receiveDepth", 10);
		try {
			$receive = Receive.getInstance();
			$receive.setSystemMessageMask(Receive.SMM_OPEN);
			$receive.setReceiveDepth(receiveDepth);
			try {
				$receive.open();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (GuardianException ex) {
			LOGGER.error(ex, "Unable to open $RECEIVE");
			return;
		}
		try {
			for (int i = 0; i < $receive.getReceiveDepth(); i++) {
				LOGGER.trace("starting thread " + i);
				Thread t = new Thread(createApplication());
				t.setDaemon(true);
				t.start();
			}
		} catch (InterruptedException e) {
			try {
				LOGGER.error("Server interrupted, going to cancel read from $Receive file");
				serverTerminating = true;
				$receive.cancelRead();
			} catch (Exception ge) {
			}
			LOGGER.error("Terminating normally after interrupt");
			return;
		} catch (Exception e) {
			LOGGER.error(e, "Terminating abnormally because of Exception");
			serverTerminating = true;
			return;
		}
	}
	
	private PpfeApplication createApplication() throws Exception {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application thisApp = config.getApplications().get(0);
		@SuppressWarnings("rawtypes")
		Class appClass = Class.forName(thisApp.getClassName());
		PpfeApplication ppfeApp = (PpfeApplication)appClass.newInstance();
		ppfeApp.setContainer(this);
		return ppfeApp;
	}
	
	@Override
	public PpfeResponse sendRequest(String destinationName, PpfeRequest ppfeRequest) {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		PpfeResponse ppfeResponse = new PpfeResponse();
		Outcome outcome = ppfeResponse.getOutcome();
		Destination destSelected = null;
		for (Destination dest : config.getDestinations()) {
			if (dest.getName().equals(destinationName)) {
				destSelected = dest;
				break;
			}
		}
		if (destSelected == null) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(String.format("Destination '%s' was requested but there is no configuration for it", destinationName));
			return ppfeResponse;
		}
		ParameterBuffer requestBuffer = toParameterBuffer(ppfeRequest.getData());
		ParameterBuffer responseBuffer = null;
		int timeoutCentiSecs = Integer.parseInt(String.valueOf(destSelected.getClientTimeoutMillis())) / 10;
		try {
			LOGGER.trace("sending to: %s", destSelected.getUri());
			PathwayClient pwClient = new PathwayClient(timeoutCentiSecs, destSelected.getLatencyThresholdMillis());
			responseBuffer = pwClient.transceive(destSelected.getUri(), requestBuffer);
			LOGGER.trace("sendRequest.response=%s", new HexDumper().buildHexDump(responseBuffer.toByteArray()).toString());
			MyProperties responseProperties = toProperties(responseBuffer);
			ppfeResponse.setData(responseProperties);
		} catch (Exception e) {
			String err = e.toString();
			if (err.contains("File system error 40"))
				err = "TIMEOUT: " + err;
			LOGGER.error(e, "sending request");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(err);
		}
		return ppfeResponse;
	}

	@Override
	public synchronized PpfeRequest getRequest() {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application app = config.getApplications().get(0);
		ReceiveInfo ri = null;
		PpfeRequest ppfeRequest = null;
		int bytesReadCount = 0;
		byte[] maxMsg = new byte[app.getMaxMessageSize()];
		short sysNum;

		if (!serverTerminating) {
			try {
				while (bytesReadCount == 0) {
					bytesReadCount = $receive.read(maxMsg, maxMsg.length);
					ri = $receive.getLastMessageInfo();
					if (ri.isSystemMessage()) {
						bytesReadCount = 0;
						sysNum = ri.getSystemMessageNumber(maxMsg);
						if (sysNum == ReceiveInfo.SYSMSG_OPEN) {
							$receive.reply(null, 0, GError.EOK);
							continue;
						} else
							continue;
					} else {
						ppfeRequest = new PpfeRequest();
						ppfeRequest.setData(toProperties(ParameterBuffer.wrap(maxMsg, 0, bytesReadCount)));
						ppfeRequest.setContext(ri);
					}
				}
			} catch (ReceiveNoOpeners ex) {
				try {
					$receive.cancelRead();
				} catch (Exception e) {
				}
				LOGGER.info("Terminating normally (no more work)");
				serverTerminating = true;
				
			} catch (Exception ex) {
				try {
					$receive.cancelRead();
				} catch (Exception e) {
				}
				LOGGER.error(ex, "Terminating after Exception");
				serverTerminating = true;
			}
		}
		return ppfeRequest;
	}
	
	private MyProperties toProperties(ParameterBuffer buffer) {
		MyProperties result = new MyProperties();
		Set<String> keys = buffer.keySet();
		for (String key : keys) {
			try {
				result.setProperty(key, buffer.getStringValue(key));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private ParameterBuffer toParameterBuffer(MyProperties properties) {
		ParameterBuffer result = new ParameterBuffer((short)0);
		for (String name : properties.stringPropertyNames()) {
			try {
				result.addParameter(name, properties.getProperty(name, ""), true);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public Outcome sendReply(Object requestContext, PpfeResponse message) {
		Outcome outcome = new Outcome(ReturnCode.SUCCESS, "replied with %d bytes");
		try {
			ParameterBuffer buffer = toParameterBuffer(message.getData());
			byte[] response = buffer.toByteArray();
			LOGGER.trace("about to write " + response.length + " chars to $Receive");
			int bytesSent = $receive.reply(response, response.length, (ReceiveInfo)requestContext, GError.EOK);
			outcome.setReturnCode(ReturnCode.SUCCESS);
			outcome.setMessage(String.format(outcome.getMessage(), bytesSent));
		} catch (Exception ex) {
			LOGGER.error(ex, "sending Reply");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(ex.toString());
		}
		return outcome;
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}
}
