package net.kwami.pathsend;

import net.kwami.utils.HexDumper;

public final class Tester {

	public static void main(String[] args) throws Exception {
		HexDumper dumper = new HexDumper();
		ParameterBuffer parmBuf = new ParameterBuffer(99);
		parmBuf.addParameter("byte-max", Byte.MAX_VALUE);
		parmBuf.addParameter("short-max", Short.MAX_VALUE);
		parmBuf.addParameter("int-max", Integer.MAX_VALUE);
		parmBuf.addParameter("long-max", Long.MAX_VALUE);
		parmBuf.addParameter("byte-min", Byte.MIN_VALUE);
		parmBuf.addParameter("short-min", Short.MIN_VALUE);
		parmBuf.addParameter("int-min", Integer.MIN_VALUE);
		parmBuf.addParameter("long-min", Long.MIN_VALUE);
		parmBuf.addParameter("bytes", "bbbbbbbbbbbbBBBBBBBccccccvalue".getBytes());
		parmBuf.getStringValue("bytes");
		parmBuf.addParameter("String", "bbbbbbbbbbbbBBBBBBBccccccvalue", false);
		parmBuf.addParameter("string0", "bbbbbbbbbbbbBBBBBBBccccccvalue", true);
		parmBuf.getStringValue("string0");
		parmBuf.addParameter("string1", "bbbbbbbbbbbbBBBBBBBccccccvalue", true);
		parmBuf.addParameter("string2", "bbbbbbbbbbbbBBBBBBBccccccvalue", true);
		parmBuf.addParameter("string3", "bbbbbbbbbbbbBBBBBBBccccccvalue", true);
		parmBuf.addParameter("string5", "bbbbbbbbbbbbBBBBBBBccccccvalue", true);
		parmBuf.addParameter("string6", "bbbbbbbbbbbbBBBBBBBccccccvalue", true);
		parmBuf.addParameter("USSD_STRING", "*909#", true);
		System.out.println(parmBuf.getStringValue("USSD_STRING"));
		byte[] payload = parmBuf.array();
		StringBuilder sb = dumper.buildHexDump(payload);
		System.out.println(sb.toString());
		parmBuf = ParameterBuffer.wrap(payload);
		sb = dumper.buildHexDump(payload);
		System.out.println(sb.toString());
		for (String key : parmBuf.keySet()) {
			if (key.equals("bytes") || key.equals("String") || key.equalsIgnoreCase("string0")) {
				System.out.printf("key=%s,value=%s|\n", key, parmBuf.getStringValue(key));				
			} else if (key.startsWith("byte")) {
				System.out.printf("key=%s,value=%d\n", key, parmBuf.getByteValue(key));				
			} else if (key.startsWith("short")) {
				System.out.printf("key=%s,value=%d\n", key, parmBuf.getShortValue(key));				
			} else if (key.startsWith("int")) {
				System.out.printf("key=%s,value=%d\n", key, parmBuf.getIntValue(key));				
			} else if (key.startsWith("long")) {
				System.out.printf("key=%s,value=%d\n", key, parmBuf.getLongValue(key));				
			}
		}
	}
}
