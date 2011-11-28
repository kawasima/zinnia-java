package net.unit8.zinnia;

import java.io.IOException;

public class TrainerTest {
	public void convert() throws IOException {
		Trainer trainer = new Trainer();
		trainer.convert("handwriting-ja.model.txt"
				, "handwriting-ja.model", 0.001f);
	}

	public static void main(String args[]) throws IOException {
		new TrainerTest().convert();
	}
}
