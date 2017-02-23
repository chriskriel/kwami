package net.kwami.tcp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class CachedObject {

	long expireTime;
	Object object;

	CachedObject(long refreshInterval, Object object) {
		this.expireTime = System.currentTimeMillis() + refreshInterval;
		this.object = object;
	}

	boolean hasExpired() {
		return expireTime < System.currentTimeMillis();
	}
}

public abstract class Configurator {

	private static final Logger logger = Logger.getLogger(Configurator.class);
	private static final Map<String, CachedObject> cache = new ConcurrentHashMap<String, CachedObject>();
	private static final long refreshInterval;

	static {
		refreshInterval = Long.parseLong(System.getProperty("config.caching.mins", "2")) * 60000;
	}
	
	public static <T> T get(Class<T> classT) {
		String resourceName = String.format("/%s.js", classT.getSimpleName());
		return get(classT, resourceName);
	}

	public static <T> T get(Class<T> classT, String resourceName) {
		logger.debug("resource=" + resourceName);
		URL url = classT.getResource(resourceName);
		T t = null;
		if ((t = getCachedObject(resourceName, url)) != null)
			return t;
		synchronized (classT) {
			if ((t = getCachedObject(resourceName, url)) != null)
				return t;
			InputStream is = null;
			try {
				is = classT.getResourceAsStream(resourceName);
				if (is == null) {
					logger.error("could not create an InputStream on " + resourceName);
					return null;
				}
				InputStreamReader rdr = new InputStreamReader(is);
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				t = gson.fromJson(rdr, classT);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
					}
			}
			if (t != null)
				cache.put(resourceName, new CachedObject(refreshInterval, t));
			else
				logger.error("could not create " + classT.getSimpleName() + " from " + resourceName);
			return t;
		}
	}
	
	public static String toJson(Object obj) {
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(obj);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getCachedObject(String simpleName, URL url) {
		CachedObject cachedEntity = cache.get(simpleName);
		if (cachedEntity == null) {
			return null;
		}
		if (!cachedEntity.hasExpired()) {
			return (T) cachedEntity.object;
		}
		File f = new File(url.getFile());
		if (f.lastModified() < cachedEntity.expireTime - refreshInterval) {
			cache.put(simpleName, new CachedObject(refreshInterval, cachedEntity.object));
			return (T) cachedEntity.object;
		}
		return null;
	}
}