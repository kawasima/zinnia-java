package net.unit8.zinnia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Character {
	public static class Dot {
		public Dot(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public int x;
		public int y;
	}

	public static double dot(List<FeatureNode> xList1, List<FeatureNode> xList2) {
		double sum = 0.0;

		Iterator<FeatureNode> xIter1 = xList1.iterator();
		Iterator<FeatureNode> xIter2 = xList2.iterator();

		FeatureNode x1 = xIter1.next();
		FeatureNode x2 = xIter2.next();

		while (x1.index >= 0 && x2.index >=0) {
			if (x1.index == x2.index) {
				sum += (x1.value * x2.value);
				x1 = xIter1.next();
				x2 = xIter2.next();
			} else if (x1.index < x2.index) {
				x1 = xIter1.next();
			} else {
				x2 = xIter2.next();
			}
		}

		return sum;
	}
	private int width;
	private int height;
	private List<List<Dot>> strokes;
	private String value;
	private Sexp sexp;

	public Character(int width, int height) {
		this.width = width;
		this.height= height;
		strokes = new ArrayList<List<Dot>>();
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void clear() {
		strokes.clear();
	}

	public int getStrokesSize() {
		return strokes.size();
	}

	public int getStrokeSize(int id) {
		return (strokes.size() <= id) ? -1 : strokes.get(id).size();
	}

	public int getX(int id, int i) {
		return (id >= strokes.size() || i >= strokes.get(id).size()) ?
				-1 : strokes.get(id).get(i).x;
	}

	public int getY(int id, int i) {
		return (id >= strokes.size() || i >= strokes.get(id).size()) ?
				-1 : strokes.get(id).get(i).y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean add(int id,int x, int y) {
		Dot d = new Dot(x, y);
		for(int size = strokes.size(); size <= id; size++) {
			strokes.add(new ArrayList<Dot>());
		}
		strokes.get(id).add(d);
		return true;
	}

	public boolean parse(String str, int length) {
		clear();

		return true;
	}
}
