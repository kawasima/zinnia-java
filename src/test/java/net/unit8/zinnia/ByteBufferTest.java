package net.unit8.zinnia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;


public class ByteBufferTest {
	@Test
	public void test() throws IOException {
		File file = new File(".project");
		FileInputStream is = new FileInputStream(file);
		ByteBuffer buf = is.getChannel().map(MapMode.READ_ONLY, 0, file.length());
		byte[] b = new byte[16];
		for(int i=0; i<=buf.limit(); i+=16) {
			int len= (buf.remaining() < 16) ? buf.remaining() : 16;
			buf.get(b, 0, len);
			System.out.print(new String(b, 0, len));
		}
		System.out.println();
	}

	@Test
	public void randomShuffle() {
		String[] abc = {"a", "b", "c"};
		Collections.shuffle(Arrays.asList(abc));

		for(String s : abc)
			System.out.print(s);
	}
}
