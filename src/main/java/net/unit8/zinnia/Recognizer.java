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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.comparators.ComparatorChain;

public class Recognizer {
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
//			for(int i=0; i<b.length; i++)
				//System.out.println(p1 +"-" + p2 + ":" + String.format("%02x",b[i]));
			int len;
			for(len = 0;  b[len] != 0 && len < 16; len++) ;
			m.character = new String(b, 0, len, charset);
			m.bias = buffer.getFloat();

			while(true) {
				FeatureNode f = new FeatureNode();
				f.index = buffer.getInt();
				f.value = buffer.getFloat();
				if(f.index == -1)
					break;
				m.x.add(f);
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
		
		Features feature = new Features();
		if(!feature.read(character)) {
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
				return (int)(p2.first - p1.first);
			}
		});
		
		Result result = new Result();
		for (int i=0; i < nbest; ++i) {
			result.add(results.get(i).second, results.get(i).first);
		}
		
		return result;
	}
}
