package net.kwami.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;

public abstract class Codec {

	public static String compressAndEncode64(String input) {
		Deflater deflater = new Deflater();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
		deflater.setInput(input.getBytes());
		deflater.finish();
		byte[] buf = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buf);
			bos.write(buf, 0, count);
		}
		return Base64.encodeBase64String(bos.toByteArray());
	}

	public static String decode64AndUncompress(String input) throws Exception {
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
		return bos.toString();
	}

}
