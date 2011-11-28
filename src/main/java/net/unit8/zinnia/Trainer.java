package net.unit8.zinnia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class Trainer {
	private List<Pair<String, List<FeatureNode>>> x;
	public static final int DIC_VERSION = 1;
	public static final double THRESHOLD = 1.0e-3;
	int maxDim;

	List<FeatureNode> copyFeatureNode(List<FeatureNode> fn, int maxDim) {
		return null;
	}

	boolean makeExample(String key, List<Pair<String,List<FeatureNode>>> x, List<Double> y
			, List<List<FeatureNode>> copyX) {
		int posNum = 0;
		int negNum = 0;
		y.clear();
		copyX.clear();
		for (int i=0; i<x.size(); i++) {
			if(key.equals(x.get(i).first)) {
				y.add(+1.0);
				++posNum;
			} else {
				y.add(-1.0);
				++negNum;
			}
			copyX.add(x.get(i).second);
		}
		return (posNum > 0 && negNum > 0);
	}

	public void add(Character c) {
		String y = c.getValue();
		Features features = Features.read(c);
		List<FeatureNode> fn = features.get();
		x.add(new Pair<String, List<FeatureNode>>(y, fn));
	}

	public boolean train(String filename) throws IOException {
		Set<String> dicSet = new HashSet<String>();
		for (int i=0; i < x.size(); ++i)
			dicSet.add(x.get(i).first);

		Double[] w = new Double[maxDim + 1];
		List<Double> y = new ArrayList<Double>();
		List<List<FeatureNode>> xCopy = new ArrayList<List<FeatureNode>>();


		BufferedWriter ofs = null;

		try {
			ofs = new BufferedWriter(new FileWriter(new File(filename + ".txt")));
			List<String> dic = new ArrayList<String>(dicSet);
			for (int i=0; i<dic.size(); ++i) {
				if (makeExample(dic.get(i), x, y, xCopy)) {
					System.err.println("cannot make training data");
				}
				System.out.println("learning: (" + i + "/" + dic.size() + ") " + dic.get(i) + " ");

				SVM.train(y.size(),
						w.length,
						y,
						xCopy,
						1.0,
						w);
				ofs.write(dic.get(i));
				ofs.write(" ");
				ofs.write(w[0].toString());

				for (int j = 1; j < w.length; ++j) {
					if (Math.abs(w[j]) >= THRESHOLD) {
						ofs.write(" ");
						ofs.write(Integer.toString(j));
						ofs.write(":");
						ofs.write(Double.toString(w[j]));
					}
				}
				ofs.write("\n");

			}
		} finally {
			IOUtils.closeQuietly(ofs);
		}
		convert(filename+"txt", filename, 0.0f);
		return false;
	}

	public boolean convert(
			String textFilename,
			String binaryFilename,
			double compressionThreshold) throws IOException {
		BufferedReader ifs = new BufferedReader(new FileReader(textFilename));
		DataOutputStream bofs = new DataOutputStream(new FileOutputStream(binaryFilename));


		int magic = 0;
		int version = DIC_VERSION;
		int msize = 0;

		bofs.writeInt(magic);
		bofs.writeInt(version);
		bofs.writeInt(msize);

		String line;
		while((line = ifs.readLine()) != null) {
			String[] col = line.split("[ \\t:]");
			if(col.length < 5) return false;
			if(col.length % 2 != 0) return false;

			float bias = Float.parseFloat(col[1]);
			byte[] character = new byte[16];
			System.arraycopy(col[0].getBytes("UTF-8"), 0, character, 0, col[0].getBytes("UTF-8").length);
			bofs.write(character, 0, 16);
			bofs.writeFloat(bias);
			for(int i=2; i < col.length; i +=2) {
				int index = Integer.parseInt(col[i]);
				float value = Float.parseFloat(col[i + 1]);
				if (Math.abs(value) > compressionThreshold) {
					bofs.writeInt(index);
					bofs.writeFloat(value);
				}
			}
			bofs.writeInt(-1);
			bofs.writeFloat(0.0f);
			++msize;
		}
		bofs.flush();
		bofs.close();

		RandomAccessFile raf=null;
		try {
			raf = new RandomAccessFile(binaryFilename, "rw");
			magic = (int)raf.length();
			magic ^= 0xef71821;
			raf.seek(0);
			raf.writeInt(magic);
			raf.writeInt(version);
			raf.writeInt(msize);
		} finally {
			if(raf != null)
				raf.close();
		}
		return true;
	}


}
