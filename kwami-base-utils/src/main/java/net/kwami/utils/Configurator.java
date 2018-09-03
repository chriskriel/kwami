package net.kwami.utils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class CachedObject {

	long cachedTime;
	Object object;

	CachedObject(long refreshInterval, Object object) {
		cachedTime = System.currentTimeMillis();
		this.object = object;
	}

	final boolean hasExpired(long resfreshIntervalMs) {
		long expireTime = cachedTime + resfreshIntervalMs;
		if (expireTime < System.currentTimeMillis())
			return true;
		return false;
	}
	
	final boolean needsRefresh(long lastModified) {
		return lastModified > cachedTime;
	}
}

/*
 * The Configurator uses Google's GSON library to instantiate objects from JSON
 * formatted files. It is mostly used to read files with configuration
 * properties into configuration objects. The JSON serialized file is called the
 * "resource file". The Configurator can only locate resource files that appear
 * on the java classpath. By default the resource file name is the class name of
 * the object to be instantiated with a *.js extension.
 */
public abstract class Configurator {

	private static final MyLogger LOGGER = new MyLogger(Configurator.class);
	private static final Map<String, CachedObject> CACHE = new ConcurrentHashMap<String, CachedObject>();
	private static final long LIFETIME_MILLIS;
	private static final String CONFIG_FILE_TYPE;

	static {
		LIFETIME_MILLIS = Long.parseLong(System.getProperty("config.caching.mins", "2")) * 60000;
		CONFIG_FILE_TYPE = System.getProperty("config.default.file.type", "js");
	}

	/*
	 * Create an object of the specified class from a JSON file. If it has
	 * previously been cached return the cached object. Only a single object based
	 * on the JSON file will be created if only this method is used.
	 * 
	 * @param classT the Class of the object to be created. The simple name of this
	 * class with a suffix of *.js is used as the filename of the JSON file. The
	 * file must be available on the classpath.
	 * 
	 * @return an object of the specified class is returned
	 */
	public final static <T> T get(Class<T> classT) {
		return get(classT, true);
	}

	/*
	 * Create an object of the specified class from a JSON file. If it has
	 * previously been cached return the cached object. Only a single object based
	 * on the JSON file will be created if only this method is used.
	 * 
	 * @param classT the Class of the object to be created.
	 * 
	 * @param resourceName The name of the JSON file from which the object will get
	 * its values. The file must be available on the classpath.
	 * 
	 * @return an object of the specified class is returned
	 */
	public final static <T> T get(Class<T> classT, String resourceName) {
		return get(classT, resourceName, true);
	}

	/*
	 * Create an object of the specified class from a JSON file. If caching is
	 * required and the object has already been cached, return it from the cache.
	 * 
	 * @param classT the Class of the object to be created. The simple name of this
	 * class with a suffix of .js is used as the filename of the JSON file. The file
	 * must be available on the classpath.
	 * 
	 * @param useCache If false a new object is created and not cached. The default
	 * is true.
	 * 
	 * @return an object of the specified class is returned
	 */
	public final static <T> T get(Class<T> classT, boolean useCache) {
		String resourceName = String.format("/%s.%s", classT.getSimpleName(), CONFIG_FILE_TYPE);
		return get(classT, resourceName, useCache);
	}

	/*
	 * Create an object of the specified class from a JSON file. If caching is
	 * required and the object has already been cached, return it from the cache.
	 * 
	 * @param classT the Class of the object to be created. The simple name of this
	 * class with a suffix of .js is used as the filename of the JSON file. The file
	 * must be available on the classpath.
	 * 
	 * @param resourceName The name of the JSON file from which the object will get
	 * its values. The file must be available on the classpath.
	 * 
	 * @param useCache If false a new object is created and not cached. The default
	 * is true.
	 * 
	 * @return an object of the specified class is returned
	 */
	public final static <T> T get(Class<T> classT, String resourceName, boolean useCache) {
		T t = getCachedObject(useCache, classT, resourceName);
		if (t != null) {
			return t;
		}
		synchronized (classT) {
			t = getCachedObject(useCache, classT, resourceName);
			if (t != null)
				return t;
			LOGGER.info("Loading %s from resource %s", classT.getName(), resourceName);
			try (InputStream is = classT.getResourceAsStream(resourceName)) {
				InputStreamReader rdr = new InputStreamReader(is);
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				t = gson.fromJson(rdr, classT);
			} catch (Throwable e) {
				String err = String.format("%s: error processing %s", e.toString(), resourceName);
				LOGGER.error(err);
				System.err.println(err);
				throw new RuntimeException(e);
			}
			if (t != null) {
				if (useCache) {
					CACHE.put(classT.getName(), new CachedObject(LIFETIME_MILLIS, t));
				}
			} else {
				String err = String.format("could not create %s from %s", classT.getName(), resourceName);
				LOGGER.error(err);
				System.err.println(err);
				throw new RuntimeException(err);
			}
			return t;
		}
	}

	public final static String toJson(Object obj) {
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(obj);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getCachedObject(boolean useCache, Class<T> classT, String resourceName) {
		if (!useCache)
			return null;
		String key = classT.getName();
		CachedObject cachedObject = CACHE.get(key);
		if (cachedObject == null) {
			return null;
		}
		if (!cachedObject.hasExpired(LIFETIME_MILLIS)) {
			return (T) cachedObject.object;
		}
		// CachedObject has expired, check if it changed on File System
		URL url = classT.getResource(resourceName);
		File f = new File(url.getFile());
		if (cachedObject.needsRefresh(f.lastModified()))
			return null;
		// re-cache the object with a new lease on life
		CACHE.put(key, new CachedObject(LIFETIME_MILLIS, cachedObject.object));
		return (T) cachedObject.object;
	}
}
