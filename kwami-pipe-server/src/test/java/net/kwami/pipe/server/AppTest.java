package net.kwami.pipe.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.kwami.pipe.server.Command;

public class AppTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCommandObj() throws Exception {
		Command c = new Command(Command.Cmd.QUERY);
		c.addParameter("key1", "value1");
		c.addParameter("key2", "value2");
		System.out.println(c.toString());
		// assertEquals("{\"command\":\"QUERY\",\"parameters\":{\"key1\":\"value1\",\"key2\":\"value2\"}}", c.toString());
	}

	@Test
	public void generateConfig() {
		System.out.println(new ServerConfig().toString());
	}
}
