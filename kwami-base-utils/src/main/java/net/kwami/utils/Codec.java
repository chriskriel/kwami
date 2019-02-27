package net.kwami.utils;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;

public abstract class Codec {

	public static String compressAndEncode64(final String input) {
		ByteBuffer buf = ByteBuffer.allocate(input.length());
		buf.put(input.getBytes());
		return deflateAndEncode64(buf);
	}

	public static String encode64(final ByteBuffer input) {
		byte[] inputBytes = new byte[input.remaining()];
		int pos = input.position();
		int remaining = input.remaining();
		for (int i = 0; i < remaining; i++)
			inputBytes[i] = input.get();
		input.position(pos);
		return Base64.encodeBase64String(inputBytes);
	}

	public static ByteBuffer decode64(final String input) {
		return ByteBuffer.wrap(Base64.decodeBase64(input.getBytes()));
	}

	public static String deflateAndEncode64(final ByteBuffer input) {
		Deflater deflater = new Deflater();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
		deflater.setInput(input.array(), input.position(), input.limit());
		deflater.finish();
		byte[] buf = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buf);
			bos.write(buf, 0, count);
		}
		return Base64.encodeBase64String(bos.toByteArray());
	}

	public static ByteBuffer decode64AndInflateToBuffer(final String input) throws Exception {
		ByteArrayOutputStream bos = decode64AndInflateToStream(input);
		ByteBuffer buf = ByteBuffer.wrap(bos.toByteArray());
		return buf;
	}

	public static String decode64AndUncompress(final String input) throws Exception {
		return decode64AndInflateToStream(input).toString();
	}

	private static ByteArrayOutputStream decode64AndInflateToStream(final String input) throws Exception {
		byte[] decoded = Base64.decodeBase64(input.getBytes());
		Inflater decompressor = new Inflater();
		decompressor.setInput(decoded);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(decoded.length);
		byte[] buf = new byte[1024];
		while (!decompressor.finished()) {
			int count = decompressor.inflate(buf);
			bos.write(buf, 0, count);
		}
		bos.close();
		return bos;
	}

}
