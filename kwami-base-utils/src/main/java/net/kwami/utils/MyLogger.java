package net.kwami.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A logger that wraps the log4j Logger class in order to format messages
 * using String.format() before they are logged. It also checks whether the
 * logging priority is enabled before formatting any message.
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

	public final void info(final Object message) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.INFO, message.toString(), null);
	}

	public final void info(final Throwable t, final Object message) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.INFO, message.toString(), t);
	}

	public final void info(final String format, final Object... args) {
		if (!logger.isInfoEnabled())
			return;
		if (handledSwappedArgs(Level.INFO, format, args))
			return;
		logger.log(FQCN, Level.INFO, String.format(format, args), null);
	}

	private boolean handledSwappedArgs(final Level level, final String format, final Object... args) {
		if (!(args[args.length - 1] instanceof Throwable))
			return false;
		if (args.length == 1) {
			logger.log(FQCN, level, format, (Throwable) args[args.length - 1]);
			return true;
		}
		Object[] newArgs = new Object[args.length - 1];
		System.arraycopy(args, 0, newArgs, 0, args.length - 1);
		logger.log(FQCN, level, String.format(format, newArgs), (Throwable) args[args.length - 1]);
		return true;
	}

	public final void info(final Throwable t, final String format, final Object... args) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.INFO, String.format(format, args), t);
	}

	public final void warn(final Object message) {
		if (logger.isInfoEnabled())
			logger.log(FQCN, Level.WARN, message.toString(), null);
	}

	public final void warn(final String format, final Object... args) {
		if (!logger.isInfoEnabled())
			return;
		if (handledSwappedArgs(Level.WARN, format, args))
			return;
		logger.log(FQCN, Level.WARN, String.format(format, args), null);
	}

	public final void debug(final Object message) {
		if (logger.isDebugEnabled())
			logger.log(FQCN, Level.DEBUG, message.toString(), null);
	}

	public final void debug(final Throwable t, final Object message) {
		if (logger.isDebugEnabled())
			logger.log(FQCN, Level.DEBUG, message.toString(), t);
	}

	public final void debug(final String format, final Object... args) {
		if (!logger.isDebugEnabled())
			return;
		if (handledSwappedArgs(Level.DEBUG, format, args))
			return;
		logger.log(FQCN, Level.DEBUG, String.format(format, args), null);
	}

	public final void debug(final Throwable t, final String format, final Object... args) {
		if (logger.isDebugEnabled())
			logger.log(FQCN, Level.DEBUG, String.format(format, args), t);
	}

	public final void trace(final String heading, final byte[] data, final int length) {
		if (!logger.isTraceEnabled())
			return;
		HexDumper hexDumper = new HexDumper();
		String message = String.format("%s:(length=%d)\n%s", heading, length, hexDumper.buildHexDump(data, length));
		logger.log(FQCN, Level.TRACE, message, null);
	}

	public final void trace(final Object message) {
		if (logger.isTraceEnabled())
			logger.log(FQCN, Level.TRACE, message.toString(), null);
	}

	public final void trace(final Throwable t, final Object message) {
		if (logger.isTraceEnabled())
			logger.log(FQCN, Level.TRACE, message.toString(), t);
	}

	public final void trace(final String format, final Object... args) {
		if (!logger.isTraceEnabled())
			return;
		if (handledSwappedArgs(Level.TRACE, format, args))
			return;
		logger.log(FQCN, Level.TRACE, String.format(format, args), null);
	}

	public final void trace(final Throwable t, final String format, final Object... args) {
		if (logger.isTraceEnabled())
			logger.log(FQCN, Level.TRACE, String.format(format, args), t);
	}

	public final void error(final Object message) {
		logger.log(FQCN, Level.ERROR, message.toString(), null);
	}

	public final void error(final Throwable t) {
		error(t, t.toString());
	}

	public final void error(final Throwable t, final Object message) {
		logger.log(FQCN, Level.ERROR, message.toString(), t);
	}

	public final void error(final String format, final Object... args) {
		if (handledSwappedArgs(Level.ERROR, format, args))
			return;
		logger.log(FQCN, Level.ERROR, String.format(format, args), null);
	}

	public final void error(final Throwable t, final String format, final Object... args) {
		logger.log(FQCN, Level.ERROR, String.format(format, args), t);
	}

	public final void log(final Throwable t, final String format, final Object... args) {
		logger.log(FQCN, Level.ERROR, String.format(format, args), t);
	}

	public final Logger getLogger() {
		return logger;
	}
}
