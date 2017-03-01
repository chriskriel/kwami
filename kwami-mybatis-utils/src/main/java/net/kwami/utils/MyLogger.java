package net.kwami.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A logger that replaces the log4j Logger class in order to format messages using String.format before they are logged.
 * It also checks whether the logging priority is enabled before formatting any message.
 *
 */
public class MyLogger {

	private static final String FQCN = MyLogger.class.getName();
	private final Logger logger;

	public MyLogger(Class<?> clazz) {
		logger = Logger.getLogger(clazz);
	}

	public MyLogger(String name) {
		logger = Logger.getLogger(name);
	}

	public void info(final String message) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.INFO, message, null);
	}

	public void info(final Throwable t, final String message) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.INFO, message, t);
	}

	public void info(final String format, final Object... args) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.INFO, String.format(format, args), null);
	}

	public void info(final Throwable t, final String format, final Object... args) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.INFO, String.format(format, args), t);
	}

	public void debug(final String message) {
		if (logger.isDebugEnabled())
			logger.log(FQCN, Level.DEBUG, message, null);
	}

	public void debug(final Throwable t, final String message) {
		if (logger.isDebugEnabled())
			logger.log(FQCN, Level.DEBUG, message, t);
	}

	public void debug(final String format, final Object... args) {
		if (logger.isDebugEnabled())
			logger.log(FQCN, Level.DEBUG, String.format(format, args), null);
	}

	public void debug(final Throwable t, final String format, final Object... args) {
		if (logger.isDebugEnabled())
			logger.log(FQCN, Level.DEBUG, String.format(format, args), t);
	}

	public void trace(final String message) {
		if (logger.isTraceEnabled())
			logger.log(FQCN, Level.TRACE, message, null);
	}

	public void trace(final Throwable t, final String message) {
		if (logger.isTraceEnabled())
			logger.log(FQCN, Level.TRACE, message, t);
	}

	public void trace(final String format, final Object... args) {
		if (logger.isTraceEnabled())
			logger.log(FQCN, Level.TRACE, String.format(format, args), null);
	}

	public void trace(final Throwable t, final String format, final Object... args) {
		if (logger.isTraceEnabled())
			logger.log(FQCN, Level.TRACE, String.format(format, args), t);
	}

	public void error(final String message) {
		logger.log(FQCN, Level.ERROR, message, null);
	}

	public void error(final Throwable t, final String message) {
		logger.log(FQCN, Level.ERROR, message, t);
	}

	public void error(final String format, final Object... args) {
		logger.log(FQCN, Level.ERROR, String.format(format, args), null);
	}

	public void log(final Throwable t, final String format, final Object... args) {
		logger.log(FQCN, Level.ERROR, String.format(format, args), t);
	}
}
