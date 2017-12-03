package net.kwami.ppfe;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tandem.ext.guardian.GError;
import com.tandem.ext.guardian.GuardianException;
import com.tandem.ext.guardian.Receive;
import com.tandem.ext.guardian.ReceiveInfo;
import com.tandem.ext.guardian.ReceiveNoOpeners;
import com.tandem.sqlmx.SQLMXDataSource;

import net.kwami.pathsend.ParameterBuffer;
import net.kwami.pathsend.PathwayClient;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;
import net.kwami.utils.MyProperties;

public class PathwayContainer implements PpfeContainer {
	private static final MyLogger LOGGER = new MyLogger(PathwayContainer.class);

	static class Context {
		String appName;
		long startTime;
		ReceiveInfo receiveInfo;
	}

	static class DataSourceConfig {
		public String blobTableName;
		public String clobTableName;
		public String catalog;
		public String schema;
		public int initialPoolSize;
		public int maxIdleTime;
		public int maxPoolSize;
		public int maxStatements;
		public int minPoolSize;
	}

	@SuppressWarnings("rawtypes")
	private Class appClass = null;;
	private boolean serverTerminating = false;
	private Receive $receive = null;
	private SQLMXDataSource dataSource;
	private Object readLock = new Object();
	private ThreadLocal<List<PathwayClient>> pathwayClientsHolder = new ThreadLocal<>();
	private ThreadLocal<ParameterBuffer> replyBufferHolder = new ThreadLocal<>();
	private ThreadLocal<ParameterBuffer> inputBufferHolder = new ThreadLocal<>();

	public PathwayContainer() throws Exception {
		super();
		DataSourceConfig cfg = Configurator.get(DataSourceConfig.class);
		dataSource = new SQLMXDataSource();
		dataSource.setBlobTableName(cfg.blobTableName);
		dataSource.setCatalog(cfg.catalog);
		dataSource.setSchema(cfg.schema);
		dataSource.setClobTableName(cfg.clobTableName);
		dataSource.setInitialPoolSize(cfg.initialPoolSize);
		dataSource.setMaxIdleTime(cfg.maxIdleTime);
		dataSource.setMaxPoolSize(cfg.maxPoolSize);
		dataSource.setMaxStatements(cfg.maxStatements);
		dataSource.setMinPoolSize(cfg.minPoolSize);
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
				PpfeApplication app = createApplication();
				Thread t = new Thread(app);
				t.setName(app.getClass().getSimpleName() + "-" + String.valueOf(i));
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
		if (appClass == null)
			appClass = Class.forName(thisApp.getClassName());
		PpfeApplication ppfeApp = (PpfeApplication) appClass.newInstance();
		ppfeApp.setContainer(this);
		return ppfeApp;
	}

	@Override
	public PpfeContainer sendRequest(String destinationName, MyProperties requestParameters,
			PpfeResponse ppfeResponse) {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		MyProperties responseParameters = ppfeResponse.getData();
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
			outcome.setMessage(String.format("Destination '%s' was requested but there is no configuration for it",
					destinationName));
			return this;
		}
		PathwayClient pwClient = getPathwayClient(destSelected);
		try {
			LOGGER.trace("sending to: %s", destSelected.getUri());
			pwClient.transceive(0, requestParameters, responseParameters);
			ppfeResponse.setData(responseParameters);
		} catch (Exception e) {
			String err = e.toString();
			if (err.contains("File system error 40"))
				err = "TIMEOUT: " + err;
			LOGGER.error(e, "sending request");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(err);
		}
		return this;
	}

	private PathwayClient getPathwayClient(Destination destSelected) {
		if (pathwayClientsHolder.get() == null)
			pathwayClientsHolder.set(new ArrayList<PathwayClient>());
		for (PathwayClient client : pathwayClientsHolder.get()) {
			if (client.getServerPath().equalsIgnoreCase(destSelected.getName())) {
				return client;
			}
		}
		PathwayClient pwClient = null;
		int timeoutCentiSecs = Integer.parseInt(String.valueOf(destSelected.getClientTimeoutMillis())) / 10;
		pwClient = new PathwayClient(destSelected.getUri(), timeoutCentiSecs, destSelected.getLatencyThresholdMillis());
		pathwayClientsHolder.get().add(pwClient);
		return pwClient;
	}

	@Override
	public boolean getRequest(PpfeRequest ppfeRequest) {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application app = config.getApplications().get(0);
		ReceiveInfo ri = null;
		int bytesReadCount = 0;
		ParameterBuffer buffer = inputBufferHolder.get();
		if (buffer == null) {
			buffer = new ParameterBuffer(0, app.getMaxRequestSize());
			inputBufferHolder.set(buffer);
		}
		byte[] maxMsg = buffer.array();
		short sysNum;

		if (!serverTerminating) {
			try {
				while (bytesReadCount == 0) {
					synchronized (readLock) {
						bytesReadCount = $receive.read(maxMsg, maxMsg.length);
						ri = $receive.getLastMessageInfo();
					}
					LOGGER.trace("receivedBytes:", maxMsg, bytesReadCount);
					if (ri.isSystemMessage()) {
						bytesReadCount = 0;
						sysNum = ri.getSystemMessageNumber(maxMsg);
						if (sysNum == ReceiveInfo.SYSMSG_OPEN) {
							$receive.reply(null, 0, GError.EOK);
							continue;
						} else
							continue;
					} else {
						ppfeRequest.clear();
						buffer.position(bytesReadCount);
						buffer.extractPropertiesInto(ppfeRequest.getData());
						Context ctx = new Context();
						ctx.appName = app.getName();
						ctx.startTime = System.currentTimeMillis();
						ctx.receiveInfo = ri;
						ppfeRequest.setContext(ctx);
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
		return !serverTerminating;
	}

	@Override
	public PpfeContainer sendReply(Object requestContext, MyProperties responseParameters, Outcome outcome) {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application app = config.getApplications().get(0);
		Context ctx = (Context) requestContext;
		long latency = System.currentTimeMillis() - ctx.startTime;
		LOGGER.debug("Application: '%s', latency: %d", ctx.appName, latency);
		outcome.setMessage("replied with %d bytes");
		try {
			ParameterBuffer buffer = replyBufferHolder.get();
			if (buffer == null) {
				buffer = new ParameterBuffer(0, app.getMaxResponseSize());
				replyBufferHolder.set(buffer);
			}
			buffer.initialize(0, responseParameters);
			LOGGER.trace("about to write " + buffer.position() + " chars to $Receive");
			int bytesSent = $receive.reply(buffer.array(), buffer.position(), ctx.receiveInfo, GError.EOK);
			outcome.setReturnCode(ReturnCode.SUCCESS);
			outcome.setMessage(String.format(outcome.getMessage(), bytesSent));
		} catch (Exception ex) {
			LOGGER.error(ex, "sending Reply");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(ex.toString());
		}
		return this;
	}

	@Override
	public Connection getDatabaseConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			LOGGER.error(e, "getting a connection");
		}
		return null;
	}
}
