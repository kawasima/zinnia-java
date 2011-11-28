package net.unit8.zinnia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Learn {
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("handwriting-ja.s")));
		Trainer trainer = new Trainer();
		String line;
		while((line = reader.readLine()) != null) {
			Character c = Character.parse(line);
			if (c == null)
				continue;

			trainer.add(c);
		}
		trainer.train("handwriting-ja.model");
	}
}
