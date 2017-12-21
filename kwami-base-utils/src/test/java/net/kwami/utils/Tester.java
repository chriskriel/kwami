package net.kwami.utils;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tester {


	public static void main(String[] args) {
		objCopy();
	}

	private static void objCopy() {
		String key = "camelCaseObjectSomething";
		String replaceAll = key.replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
		System.out.println(replaceAll);
		TestClass from = new TestClass();
		from.setDate(new Date());
		from.setB((byte)1);
		from.setBb(new byte[] { 1, 2, 3 });
		from.setBool(true);
		from.setBools(new boolean[] { true, false });
		from.setC('x');
		from.setCc(new char[] { 'x', 'y', 'z' });
		from.setD(10.5);
		from.setDd(new double[] { 10.5, 10.6, 10.7 });
		from.setF((float) 10.5);
		from.setFf(new float[] { (float) 10.5, (float) 10.6, (float) 10.7 });
		from.setH((short) 1);
		from.setHh(new short[] { (short) 1, (short) 2, (short) 3 });
		TestClass to = new TestClass();
		to.copyFields(from);
		Gson gson = new GsonBuilder().create();
		System.out.println(gson.toJson(to));
		System.out.println(gson.toJson(from));
	}
}
