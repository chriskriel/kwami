package kwami.test;

import static org.junit.Assert.*;

import java.util.Properties;

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
	public void testJson() {
		MyProperties testProps = new MyProperties();
		Properties props = new Properties();
		props.setProperty("x1", "yyyy");
		props.setProperty("x2", "zzzz");
		testProps.setJsonProperty("json", props);
		Properties props2 = testProps.getJsonProperty("json", props);
		assertEquals(props.getProperty("x1"), props2.getProperty("x1"));
		assertEquals(props.getProperty("x2"), props2.getProperty("x2"));
		assertEquals(2, props2.keySet().size());
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
		testProps.setProperty("with.sys.var", "this is my home: '${user.home}' and that is that.");
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
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
		assertEquals("this is my home: '/home/chris' and that is that.", testProps.getProperty("with.sys.var", "bad value"));
		assertEquals("this is my home: '/home/chris' and that is that.", testProps.getProperty("with.sys.var"));
	}
}
