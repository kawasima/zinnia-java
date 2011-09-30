package net.unit8.zinnia;

import java.io.IOException;
import java.text.StringCharacterIterator;

import net.unit8.zinnia.Sexp.Cell;

import org.junit.Test;

public class SexpTest {
	@Test
	public void test() throws IOException {
		StringCharacterIterator iter = new StringCharacterIterator(
		"(character (value あ) (strokes ((1 1)(2 2))))");
		Cell cell = new Sexp().read(iter);
		System.out.println(cell);
	}

}
