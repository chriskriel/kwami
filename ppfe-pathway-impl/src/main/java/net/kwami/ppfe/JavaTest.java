package net.kwami.ppfe;

import java.nio.ByteBuffer;

public class JavaTest {

	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.allocate(4096);
		System.out.println(bb.hasArray() ? "True" : "False");
	}

}
