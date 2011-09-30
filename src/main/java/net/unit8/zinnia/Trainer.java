package net.unit8.zinnia;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Trainer {
	private List<Pair<String, FeatureNode>> x;
	public static final int DIC_VERSION = 1;
	int maxDim;

	boolean makeExample(String key, List<Pair<String,FeatureNode>> x, List<Double> y
			, List<FeatureNode> copyX) {
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

	public boolean train(String filename) throws IOException {
		FileOutputStream ofs = new FileOutputStream(filename + ".txt");
		Set<String> dicSet = new HashSet<String>();
		for (int i=0; i < x.size(); ++i)
			dicSet.add(x.get(i).first);

		Double[] w = new Double[maxDim + 1];
		List<Double> y = new ArrayList<Double>();
		List<FeatureNode> xCopy = new ArrayList<FeatureNode>();


		List<String> dic = new ArrayList<String>(dicSet);
		for (int i=0; i<dic.size(); ++i) {
			if (makeExample(dic.get(i), x, y, xCopy)) {
				System.err.println("cannot make training data");
			}
			System.out.println("learning: (" + i + "/" + dic.size() + ") " + dic.get(i) + " ");

			List<List<FeatureNode>> xCopyList = new ArrayList<List<FeatureNode>>();
			xCopyList.add(xCopy);

			SVM.train(y.size(),
					w.length,
					y,
					xCopyList,
					1.0,
					w);
		}
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
