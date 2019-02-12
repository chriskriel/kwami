package kwami.test;

import static org.junit.Assert.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.apache.logging.log4j.message.ThreadDumpMessage;
import org.junit.Test;

import net.kwami.utils.MemoryDumpMessage;
import net.kwami.utils.MyLogger;

public class Log4j2Tester {
	Logger logger = LogManager.getLogger();

	@Test
	public void testBasic() {
		logger.info("message");
		x(1, 2);
		MyLogger logger2 = new MyLogger(Log4j2Tester.class);
		logger2.info("who is logging here");
		logger2.info("%s", "formatted message");
		logger2.info("%s", "formatted message");
	}

	private String x(int one, int two) {
		logger.traceEntry("{}, {}", one, two);
		logger.printf(Level.DEBUG, "%d + %d = %d", one, two, one + two);
		logger.debug(new ParameterizedMessage("one={},two={}", one, two));
		logger.debug(new StringFormattedMessage("one=%d,two=%d", one, two));
		logger.trace(new ThreadDumpMessage("Here"));
		String garbage = "";
		for (int i = 0; i < 20; i++) {
			garbage += this.getClass().getName();
		}
		logger.debug(new MemoryDumpMessage("Class Name", garbage.getBytes()));
		assertEquals(true, true);
		return logger.traceExit("return value");
	}

}
