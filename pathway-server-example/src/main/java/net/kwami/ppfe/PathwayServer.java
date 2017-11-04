package net.kwami.ppfe;

import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.tandem.ext.guardian.GError;
import com.tandem.ext.guardian.GuardianException;
import com.tandem.ext.guardian.Receive;
import com.tandem.ext.guardian.ReceiveInfo;
import com.tandem.ext.guardian.ReceiveNoOpeners;

import net.kwami.pathsend.PathwayClient;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;
import net.kwami.utils.ParameterBuffer;

public abstract class PathwayServer implements PpfeContainer {
	private MyLogger logger = new MyLogger(PathwayServer.class);
	private boolean serverTerminating = false;
	private Receive $receive = null;
	private DataSource dataSource;

	public PathwayServer() throws Exception {
		super();
		Properties properties = Configurator.get(Properties.class);
		String schemaName = properties.getProperty("schemaName", "Dev.Rms");
		String resourceName = String.format("/%s.js", schemaName);
		logger.info("configuring schema from the file %s", resourceName);
		PoolProperties pp = Configurator.get(PoolProperties.class, resourceName);
		DataSource ds = new DataSource();
		ds.setPoolProperties(pp);
		this.dataSource = ds;
		start();
	}
	
	private void start() {
		try {
			$receive = Receive.getInstance();
			$receive.setSystemMessageMask(Receive.SMM_OPEN);
			$receive.setReceiveDepth(10);
			try {
				$receive.open();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (GuardianException ex) {
			logger.error("Unable to open $RECEIVE", ex);
			return;
		}
		try {
			for (int i = 0; i < $receive.getReceiveDepth(); i++) {
				logger.debug("starting thread " + i);
				Thread t = new Thread(createApplication());
				t.setDaemon(true);
				t.start();
			}
		} catch (InterruptedException e) {
			try {
				logger.error("Server interrupted, going to cancel read from $Receive file");
				serverTerminating = true;
				$receive.cancelRead();
			} catch (Exception ge) {
			}
			logger.error("Terminating normally after interrupt");
			return;
		} catch (Exception e) {
			logger.error("Terminating abnormally because of Exception", e);
			serverTerminating = true;
			return;
		}
	}
	
	@Override
	public PpfeMessage sendRequest(String destination, PpfeMessage message) {
		PpfeMessage response = new PpfeMessage();
		ParameterBuffer data = null;
		try {
			PathwayClient pwClient = new PathwayClient(200, 5000);
			data = pwClient.transceive(destination, message.getData());
			response.setData(data);
		} catch (Exception e) {
		}
		return response;
	}

	@Override
	public synchronized PpfeMessage getRequest(int maxBuf) {
		ReceiveInfo ri = null;
		PpfeMessage message = null;
		int bytesReadCount = 0;
		byte[] maxMsg = new byte[maxBuf];
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
						message = new PpfeMessage();
						message.setData(ParameterBuffer.wrap(maxMsg, 0, bytesReadCount));
						message.setContext(ri);
					}
				}
			} catch (ReceiveNoOpeners ex) {
				try {
					$receive.cancelRead();
				} catch (Exception e) {
				}
				logger.info("Terminating normally (no more work)");
				serverTerminating = true;
				
			} catch (Exception ex) {
				try {
					$receive.cancelRead();
				} catch (Exception e) {
				}
				logger.error(ex, "Terminating after Exception");
				serverTerminating = true;
			}
		}
		return message;
	}

	@Override
	public int sendReply(PpfeMessage message) {
		try {
			byte[] response = message.getData().toByteArray();
			logger.debug("about to write " + response.length + " chars to $Receive");
			return $receive.reply(response, response.length, (ReceiveInfo)message.getContext(), GError.EOK);
		} catch (Exception ex) {
			logger.error(ex, ex.getMessage());
			return 0;
		}
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}
}
