package kwami.test;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.apache.logging.log4j.message.ThreadDumpMessage;
import org.junit.Test;

import net.kwami.utils.JsonMessage;
import net.kwami.utils.MemoryDumpMessage;

public class Log4j2Tester {
	Logger logger = LogManager.getLogger(Log4j2Tester.class);

	@Test
	public void testBasic() {
		logger.info("message");
		x(1, 2);
	}

	private String x(int one, int two) {
		logger.traceEntry("{}, {}", one, two);
		logger.printf(Level.DEBUG, "%d + %d = %d", one, two, one + two);
		logger.debug(new ParameterizedMessage("ParameterizedMessage: one={},two={}", one, two));
		logger.debug(new StringFormattedMessage("StringFormattedMessage: one=%d,two=%d", one, two));
		logger.trace(MarkerManager.getMarker("STACK"), new ThreadDumpMessage("ThreadDumpMessage"));
		String garbage = "";
		for (int i = 0; i < 20; i++) {
			garbage += this.getClass().getName();
		}
		logger.debug(MarkerManager.getMarker("DUMP"), new MemoryDumpMessage("MemoryDumpMessage", garbage.getBytes()));
		logger.debug(MarkerManager.getMarker("JSON"), new JsonMessage(garbage.getBytes()));
		assertEquals(true, true);
		return logger.traceExit("Good bye!");
	}

}
