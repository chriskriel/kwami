package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kwami.utils.MyProperties;

public class MyPropertiesTester {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		MyProperties testProps = new MyProperties();
		testProps.setIntProperty("int.property.max", Integer.MAX_VALUE);
		testProps.setIntProperty("int.property.min", Integer.MIN_VALUE);
		testProps.setLongProperty("long.property.max", Long.MAX_VALUE);
		testProps.setLongProperty("long.property.min", Long.MIN_VALUE);
		testProps.setShortProperty("short.property.max", Short.MAX_VALUE);
		testProps.setShortProperty("short.property.min", Short.MIN_VALUE);
		Gson gson = new GsonBuilder().create();
		String serialized = gson.toJson(testProps);
		System.out.println(serialized);
		testProps = gson.fromJson(serialized, MyProperties.class);
		assertEquals(Short.MAX_VALUE, testProps.getShortProperty("short.property.max", (short) 0));
		assertEquals(Integer.MAX_VALUE, testProps.getIntProperty("int.property.max", 0));
		assertEquals(Long.MAX_VALUE, testProps.getLongProperty("long.property.max", 0L));
		assertEquals(Short.MIN_VALUE, testProps.getShortProperty("short.property.min", (short) 0));
		assertEquals(Integer.MIN_VALUE, testProps.getIntProperty("int.property.min", 0));
		assertEquals(Long.MIN_VALUE, testProps.getLongProperty("long.property.min", 0L));
		assertEquals(Short.MAX_VALUE, testProps.getShortProperty("int.property.max", Short.MAX_VALUE));
	}
}
