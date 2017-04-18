package net.kwami.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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


/*
The Configurator uses Google's GSON library to instantiate objects from JSON formatted
files. It is mostly used to read files with configuration properties into configuration objects.
The JSON serialized file is called the "resource file". The Configurator can only locate resource
files that appear on the java classpath. By default the resource file name is the class name of
the object to be instantiated with a *.js extension.
*/
public abstract class Configurator {

	private static final MyLogger logger = new MyLogger(Configurator.class);
	private static final Map<String, CachedObject> cache = new ConcurrentHashMap<String, CachedObject>();
	private static final long refreshInterval;

	static {
		refreshInterval = Long.parseLong(System.getProperty("config.caching.mins", "2")) * 60000;
	}
	
	/*
	Instantiate an object of the specified class from a JSON file.
	@param classT 
		the Class of the object to be instantiated. The simple name
		of this class with a suffix of *.js is used as the filename of the JSON
		file. The file must be available on the classpath.
	@return
		an object of the specified class is returned
	*/
	public static <T> T get(Class<T> classT) {
		String resourceName = String.format("/%s.js", classT.getSimpleName());
		return get(classT, resourceName);
	}

	public static <T> T get(Class<T> classT, String resourceName) {
		logger.debug("resource=%s", resourceName);
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
					logger.error("could not create an InputStream on %s", resourceName);
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
				logger.error("could not create %s from %s", classT.getSimpleName(), resourceName);
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
