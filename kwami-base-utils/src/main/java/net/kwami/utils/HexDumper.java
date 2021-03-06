package net.kwami.utils;

public final class HexDumper {
	private static final String HEX = "0123456789ABCDEF";
	private int hexPerLine = 64;
	
	public HexDumper() {
		super();
	}
	
	public HexDumper(final int hexPerLine) {
		super();
		this.hexPerLine = hexPerLine;
	}

	public final StringBuilder buildHexDump(final byte[] bytes) {
		return buildHexDump(bytes, bytes.length);
	}

	public final StringBuilder buildHexDump(final byte[] inBytes, final int length) {
		byte[] bytes = new byte[length];
		System.arraycopy(inBytes, 0, bytes, 0, length);
		StringBuilder sb = new StringBuilder((length == 0 ? 20 : length) * 2);
		if (length == 0) {
			sb.append("no bytes to display in hex format");
			return sb;
		}
		int i;
		for (i = 0; i < length; i++) {
			if (i > 0) {
				if (i % 4 == 0) {
					sb.append(' ');
				}
				if (i % hexPerLine == 0) {
					sb.append("| ");
					sb.append(removeNonPrintable(bytes, i - hexPerLine, hexPerLine));
					sb.append(" |\n");
				}
			}
			translateToHex(sb, bytes[i]);
		}
		// now complete the final line
		int j = i % hexPerLine;
		if (j == 0) {
			sb.append(" | ");
			sb.append(removeNonPrintable(bytes, i - hexPerLine, hexPerLine));
			sb.append(" |");
		} else {
			for (; i % hexPerLine != 0; i++) {
				if (i % 4 == 0) {
					sb.append(' ');
				}
				sb.append("  ");
			}
			if (j > 0) {
				sb.append(" | ");
				sb.append(removeNonPrintable(bytes, length - j, j));
			}
		}
		return sb;
	}

	private final void translateToHex(final StringBuilder sb, final byte b) {
		int byteValue = b;
		int leftDigit = byteValue & 0x000000F0;
		leftDigit >>= 4;
		int rightDigit = byteValue & 0x0000000F;
		sb.append(HEX.charAt(leftDigit));
		sb.append(HEX.charAt(rightDigit));
	}

	private final String removeNonPrintable(final byte[] bytes, final int offset, final int length) {
		if (bytes.length == 0)
			return "";
		for (int i = offset; i < (offset + length); i++) {
			if (bytes[i] < 32 || bytes[i] > 126)
				bytes[i] = 32;
		}
		return new String(bytes, offset, length);
	}
}
