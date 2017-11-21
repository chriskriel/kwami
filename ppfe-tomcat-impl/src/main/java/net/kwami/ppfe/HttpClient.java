package net.kwami.ppfe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class HttpClient {

	private final HttpHost targetHost;
	private CloseableHttpClient client = null;
	private HttpClientContext context = null;
	private int maxPoolSize = 50;
	private int maxInactivityMs = 5000;
	private int soTimeoutMs = 5000;

	public HttpClient(String scheme, String targetHostName, int targetPort) {
		super();
		targetHost = new HttpHost(targetHostName, targetPort, scheme);
	}

	public synchronized void build() {
		if (client != null)
			return;
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(maxPoolSize);
		cm.setMaxTotal(maxPoolSize);
		cm.closeExpiredConnections();
		cm.closeIdleConnections(maxInactivityMs, TimeUnit.MILLISECONDS);
		SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(soTimeoutMs).build();
		cm.setSocketConfig(targetHost, socketConfig);
		client = HttpClients.custom().setConnectionManager(cm).build();
	}

	public void setCredentials(String userName, String password) throws Exception {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(userName, password));
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);
		context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);
	}

	public HttpClient setSoTimeoutMs(int soTimeoutMs) {
		this.soTimeoutMs = soTimeoutMs;
		return this;
	}

	public HttpClient setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	public HttpClient setMaxInactivityMs(int maxInactivityMs) {
		this.maxInactivityMs = maxInactivityMs;
		return this;
	}

	public String post(String relativePath, String postData) throws Exception {
		if (client == null)
			build();
		HttpPost postMethod = null;
		try {
			postMethod = new HttpPost(relativePath);
			StringEntity requestEntity = new StringEntity(postData, Consts.UTF_8);
			postMethod.setEntity(requestEntity);
			postMethod.addHeader("Accept", "application/x-www-form-urlencoded");
			postMethod.addHeader("Content-Type", "application/x-www-form-urlencoded");
			CloseableHttpResponse response = null;
			if (context != null)
				response = client.execute(targetHost, postMethod, context);
			else
				response = client.execute(targetHost, postMethod);
			System.out.println(response.getStatusLine());
			if (response.getStatusLine().getStatusCode() != 200)
				throw new Exception(response.getStatusLine().toString());
			HttpEntity responseEntity = response.getEntity();
			Reader reader = new InputStreamReader(responseEntity.getContent());
			BufferedReader bufferedReader = new BufferedReader(reader);
			String responseData = bufferedReader.readLine();
			EntityUtils.consume(responseEntity);
			return responseData;
		} finally {
			postMethod.releaseConnection();
		}
	}

}
