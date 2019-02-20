package kwami.test;

import static org.junit.Assert.*;

import org.junit.Test;

import net.kwami.utils.JsonMessage;

public class JsonMessageTester {
	static class TestObj {
		int i = 0;
		String x = "String X";
		public final int getI() {
			return i;
		}
		public final void setI(int i) {
			this.i = i;
		}
		public final String getX() {
			return x;
		}
		public final void setX(String x) {
			this.x = x;
		}
	}

	@Test
	public void test() {
		TestObj obj = new TestObj();
		String json1 = new JsonMessage(obj).getFormattedMessage();
		JsonMessage jsonMessage = new JsonMessage(json1, TestObj.class);
		obj = jsonMessage.getObject(TestObj.class);
		String json2 = new JsonMessage(obj).getFormattedMessage();
		assertEquals("failed-1", json1,  json2);
		obj = JsonMessage.getObj(json2, TestObj.class);
		String json3 = JsonMessage.getJson(obj);
		assertEquals("failed-2", json1,  json3);
}

}
