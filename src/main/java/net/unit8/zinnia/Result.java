package net.unit8.zinnia;

import java.util.ArrayList;
import java.util.List;

public class Result {
	List<Pair<Double, String>> results;

	public Result() {
		results = new ArrayList<Pair<Double, String>>();
	}

	public void add(String character, double score) {
		results.add(new Pair<Double, String>(score, character));
	}

	public void clear() {
		results.clear();
	}

	public String getValue(int i) {
		return (i >= results.size()) ? null : results.get(i).second;
	}

	public double getScore(int i) {
		return (i >= results.size()) ? -1 : results.get(0).first;
	}

	public int getSize() {
		return results.size();
	}

	public List<Pair<Double, String>> getResults() {
		return results;
	}
}
