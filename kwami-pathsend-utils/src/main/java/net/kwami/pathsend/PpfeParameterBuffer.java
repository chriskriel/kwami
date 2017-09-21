package net.kwami.pathsend;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.kwami.utils.HexDumper;

public final class PpfeParameterBuffer {

	private static final int LEN1_OFFSET = 4;
	private static final int LEN2_OFFSET = 6;
	private static final int MSG_ID_OFFSET = 8;
	private static final short HDR_LEN = 10;
	private static final byte TERMINATOR = 0;
	private String charSetName = "ISO-8859-1";
	private int size = Short.MAX_VALUE;
	private ByteBuffer bb = null;
	private Map<String, Integer> keys = null;

	public static PpfeParameterBuffer wrap(byte[] bytes) {
		return wrap(bytes, 0, bytes.length);
	}

	public static PpfeParameterBuffer wrap(byte[] bytes, int offset, int length) {
		PpfeParameterBuffer obj = new PpfeParameterBuffer();
		obj.bb = ByteBuffer.wrap(bytes, offset, length);
		obj.keys = obj.keyMap();
		return obj;
	}

	private PpfeParameterBuffer() {
	}

	public PpfeParameterBuffer(short msgId) {
		bb = ByteBuffer.allocate(size);
		initialize(msgId);
	}

	public PpfeParameterBuffer(short msgId, int size) {
		this.size = size;
		bb = ByteBuffer.allocate(size);
		initialize(msgId);
	}

	public PpfeParameterBuffer initialize(short newMsgId) {
		keys = new HashMap<String, Integer>();
		bb.clear();
		bb.putInt(0);
		bb.position(MSG_ID_OFFSET);
		bb.putShort(newMsgId);
		return this;
	}

	public byte[] toByteArray() {
		if (bb == null)
			return null;
		int dataLen = bb.position() - HDR_LEN;
		bb.putShort(LEN1_OFFSET, (short) dataLen);
		bb.putShort(LEN2_OFFSET, (short) dataLen);
		byte[] payloadBytes = new byte[bb.position()];
		bb.position(0);
		bb.get(payloadBytes);
		return payloadBytes;
	}

	public short getMsgId() {
		if (bb == null)
			return (short) 0;
		return bb.getShort(8);
	}

	public PpfeParameterBuffer addParameter(String name, byte value) throws UnsupportedEncodingException {
		setParameterName(name);
		bb.putShort((short) (Byte.SIZE / Byte.SIZE));
		bb.put(value);
		return this;
	}

	public PpfeParameterBuffer addParameter(String name, short value) throws UnsupportedEncodingException {
		setParameterName(name);
		bb.putShort((short) (Short.SIZE / Byte.SIZE));
		bb.putShort(value);
		return this;
	}

	public PpfeParameterBuffer addParameter(String name, int value) throws UnsupportedEncodingException {
		bb.put(name.getBytes(charSetName));
		bb.put(TERMINATOR);
		bb.putShort((short) (Integer.SIZE / Byte.SIZE));
		bb.putInt(value);
		return this;
	}

	public PpfeParameterBuffer addParameter(String name, long value) throws UnsupportedEncodingException {
		setParameterName(name);
		bb.putShort((short) (Long.SIZE / Byte.SIZE));
		bb.putLong(value);
		return this;
	}

	public PpfeParameterBuffer addParameter(String name, byte[] value) throws UnsupportedEncodingException {
		return addParameter(name, value, 0, (short) value.length);
	}

	public PpfeParameterBuffer addParameter(String name, byte[] value, int offset, short length)
			throws UnsupportedEncodingException {
		setParameterName(name);
		bb.putShort(length);
		bb.put(value, offset, length);
		return this;
	}

	public PpfeParameterBuffer addParameter(String name, String value, boolean addNullTerminator)
			throws UnsupportedEncodingException {
		if (value == null)
			return this;
		setParameterName(name);
		bb.putShort((short) (value.length() + (addNullTerminator ? 1 : 0)));
		if (value.length() > 0) {
			bb.put(value.getBytes(charSetName));
		}
		if (addNullTerminator)
			bb.put(TERMINATOR);
		return this;
	}
	
	public Set<String> keySet() {
		return keys.keySet();
	}

	public byte getByteValue(String name) {
		if (keys.get(name) == null)
			return 0;
		return bb.get(keys.get(name) + 2);
	}

	public short getShortValue(String name) {
		if (keys.get(name) == null)
			return 0;
		return bb.getShort(keys.get(name) + 2);
	}

	public int getIntValue(String name) {
		if (keys.get(name) == null)
			return 0;
		return bb.getInt(keys.get(name) + 2);
	}

	public long getLongValue(String name) {
		if (keys.get(name) == null)
			return 0;
		return bb.getLong(keys.get(name) + 2);
	}

	public byte[] getByteArrayValue(String name) {
		if (keys.get(name) == null)
			return null;
		byte[] valueBytes = null;
		int valuePosition = keys.get(name);
		int putPosition = bb.position();
		bb.position(valuePosition);
		int valueLen = bb.getShort();
		if (valueLen > 0) {
			valueBytes = new byte[valueLen];
			bb.get(valueBytes);
		}
		bb.position(putPosition);
		return valueBytes;
	}

	public String getStringValue(String name) throws UnsupportedEncodingException {
		byte[] valueBytes = getByteArrayValue(name);
		if (valueBytes == null)
			return "";
		return (new String(valueBytes, charSetName)).trim();
	}

	public String getCharSetName() {
		return charSetName;
	}

	public void setCharSetName(String charSetName) {
		this.charSetName = charSetName;
	}

	private void setParameterName(String name) throws UnsupportedEncodingException {
		bb.put(name.getBytes(charSetName));
		bb.put(TERMINATOR);
		keys.put(name, bb.position());
	}

	private Map<String, Integer> keyMap() {
		Map<String, Integer> keys = new HashMap<String, Integer>();
		int length = bb.getShort(LEN1_OFFSET) + HDR_LEN;
		byte[] nameBytes = new byte[64];
		int j = 0, i = HDR_LEN;
		for (; i < length; i++) {
			byte b = bb.get(i);
			if (b != TERMINATOR) {
				nameBytes[j++] = b;
				continue;
			}
			keys.put((new String(nameBytes)).trim(), ++i);
			nameBytes = new byte[64];
			j = 0;
			i += bb.getShort(i) + 1;
		}
		return keys;
	}
}
