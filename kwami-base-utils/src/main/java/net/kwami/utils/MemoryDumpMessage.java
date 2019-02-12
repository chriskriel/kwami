package net.kwami.utils;

import org.apache.logging.log4j.message.Message;

public class MemoryDumpMessage implements Message {

	private static final long serialVersionUID = 1L;

	private final String heading;
	private final byte[] data;
	private final int length;
	private final int hexPerLine;

	public MemoryDumpMessage(final String heading, final byte[] data) {
		this(heading, data, data.length);
	}

	public MemoryDumpMessage(final String heading, final byte[] data, final int length) {
		this(heading, data, length, 52);
	}

	public MemoryDumpMessage(final String heading, final byte[] data, final int length, final int hexPerLine) {
		super();
		this.heading = heading;
		this.data = data;
		this.length = length;
		this.hexPerLine = hexPerLine;
	}
	@Override
	public String getFormat() {
		return "";
	}

	@Override
	public String getFormattedMessage() {
		HexDumper hexDumper = new HexDumper(hexPerLine);
		String message = String.format("%s:(length=%d)\n%s", heading, length, hexDumper.buildHexDump(data, length));
		return message;
	}

	@Override
	public Object[] getParameters() {
		return new Object[] { new Integer(length) };
	}

	@Override
	public Throwable getThrowable() {
		return null;
	}

}
