package net.unit8.zinnia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

public class Recognizer {
	private static final Logger logger = Logger.getLogger(Recognizer.class.getName());
	private static final Charset charset = Charset.forName("UTF-8");
	private static class Model {
		String character;
		double bias;
		List<FeatureNode> x;
	}

	private List<Model> model;

	public Recognizer() {
		model = new ArrayList<Model>();
	}

	public String getValue(int i) {
		return (i >= model.size()) ? null : model.get(0).character;
	}

	public boolean open(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		MappedByteBuffer buffer = stream
			.getChannel()
			.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		buffer.order(ByteOrder.BIG_ENDIAN);
		return open(buffer);
	}

	public boolean open(ByteBuffer buffer) {
		int magic = buffer.getInt();
		int version = buffer.getInt();
		int size= buffer.getInt();

		byte[] b = new byte[16];
		while(buffer.hasRemaining()) {
			Model m = new Model();
			m.x = new ArrayList<FeatureNode>();
			buffer.get(b, 0, 16);
			int len;
			for(len = 0;  b[len] != 0 && len < 16; len++) ;
			m.character = new String(b, 0, len, charset);
			float bias = buffer.getFloat();
			m.bias = bias;

			while(true) {
				FeatureNode f = new FeatureNode();
				f.index = buffer.getInt();
				f.value = buffer.getFloat();
				m.x.add(f);
				if(f.index == -1)
					break;
			}
			model.add(m);
		}

		return true;
	}

	public boolean close() {
		model.clear();
		return true;
	}

	public Result classify(Character character, int nbest) {
		if (model.isEmpty() || nbest <= 0) {
			return null;
		}

		Features feature = Features.read(character);
		if (feature == null) {
			return null;
		}

		List<FeatureNode> x = feature.get();
		List<Pair<Double, String>> results
			= new ArrayList<Pair<Double, String>>(model.size());

		for (int i=0; i < model.size(); ++i) {
			results.add(
				new Pair<Double, String>(
					model.get(i).bias + Character.dot(model.get(i).x, x),
					model.get(i).character));
		}
		nbest = Math.min(nbest, results.size());
		Collections.sort(results, new Comparator<Pair<Double, String>>() {
			public int compare(Pair<Double, String> p1, Pair<Double, String> p2) {
				return (int)Math.signum(p2.first - p1.first);
			}
		});

		Result result = new Result();
		for (int i=0; result.getSize() < nbest; i++) {
			if(StringUtils.containsAny(results.get(i).second, "0123456789"))
				continue;
			result.add(results.get(i).second, results.get(i).first);
			logger.info(results.get(i).second + ":" + results.get(i).first);
		}

		return result;
	}

	public void train(Character character) {
		Features feature = Features.read(character);
		if (feature == null) {
			return;
		}

		List<FeatureNode> x = feature.get();
		Model m = findModel(character);

		List<List<FeatureNode>> xCopy = new ArrayList<List<FeatureNode>>();
		List<Double> y = new ArrayList<Double>();

		makeExample(character.getValue(), y, xCopy);
		int maxDim = getMaxDim();
		double[] w = new double[maxDim + 1];

		if (m == null) {
			m = new Model();
			m.character = character.getValue();
			m.x = new ArrayList<FeatureNode>();
			model.add(m);
		} else {
			for(FeatureNode fn  : m.x) {
				if (fn.index < 0) continue;
				w[fn.index] = fn.value;
			}
		}

		SVM.train(y.size(),
			w.length,
			y,
			xCopy,
			1.0,
			w);

		m.bias = w[0];
		m.x.clear();
		for (int j=1; j < w.length; j++) {
			if (Math.abs(w[j]) >= Trainer.THRESHOLD) {
				FeatureNode fn = new FeatureNode();
				fn.index = j;
				fn.value = w[j];
				m.x.add(fn);
			}
		}

	}

	private void makeExample(String key, List<Double> y, List<List<FeatureNode>> copyX) {
		y.clear();
		copyX.clear();
		for (Model m : model) {
			if(key.equals(m.character)) {
				y.add(+1.0);
			} else {
				y.add(-1.0);
			}
			copyX.add(m.x);
		}
	}

	private int getMaxDim() {
		int maxDim = 0;
		for (Model m : model) {
			for(FeatureNode f : m.x) {
				maxDim = Math.max(f.index, maxDim);
			}
		}
		return maxDim;
	}
	private Model findModel(Character ch) {
		for (Model m : model) {
			if (StringUtils.equals(m.character, ch.getValue())) {
				return m;
			}
		}
		return null;
	}
}
