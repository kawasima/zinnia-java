package net.unit8.zinnia;

import java.io.File;
import java.io.IOException;

import org.junit.Test;


public class RecognizerTest {
	@Test
	public void testClassify() throws IOException {
		Recognizer recognizer = new Recognizer();
		recognizer.open(new File("/home/kawasima/Downloads/zinnia-tomoe-0.6.0-20080911/handwriting-ja.model"));
		Character c = new Character(300, 300);
		c.add(0, 51, 29);
		c.add(0, 117, 41);
		c.add(1, 99, 65);
		c.add(1, 219, 77);
		c.add(2, 27, 131);
		c.add(2, 261, 131);
		c.add(3, 129, 17);
		c.add(3, 57, 203);
		c.add(4, 111, 71);
		c.add(4, 219, 173);

		Result result = recognizer.classify(c, 10);
		if(result == null) {
			throw new IOException("NULL result!");
		}
		for(Pair<Double, String> pair: result.results) {
			System.out.println("[" + pair.second + ":" + pair.first +"]");
		}
	}
}
