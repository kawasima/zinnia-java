package net.unit8.zinnia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Features {
	private static final int MAX_CHARACTER_SIZE=50;
	private List<FeatureNode> features;

	public static class NodePair {
		public Node first;
		public Node last;
	}

	public static class NodeSet {
		public NodeSet(Node first, Node last) {
			this.first = first;
			this.last  = last;
		}
		public Node first;
		public Node last;
		public Node best;
	}


	public Features() {
		features = new ArrayList<FeatureNode>();
	}
	private double distance(Node n1, Node n2) {
		double x = n1.x - n2.x;
		double y = n1.y - n2.y;
		return Math.sqrt(x*x + y*y);
	}
	private double distance2(Node n1) {
		double x = n1.x - 0.5;
		double y = n1.y - 0.5;
		return Math.sqrt(x*x + y*y);
	}

	private double minimumDistance(NodeSet nodeSet) {
		if (nodeSet.first == nodeSet.last)
			return 0.0;
		double a = nodeSet.last.x - nodeSet.first.x;
		double b = nodeSet.last.y - nodeSet.first.y;
		double c = nodeSet.last.y * nodeSet.first.x - nodeSet.last.x * nodeSet.first.y;

		double max = -1.0;
		for (Node n = nodeSet.first; n != nodeSet.last; n = n.next) {
			double dist = Math.abs((a * n.y)- (b * n.x)+ c);
			if (dist > max) {
				max = dist;
				nodeSet.best = n;
			}
		}
		return max * max / (a*a + b*b);
	}

	private void makeBasicFeature(int offset, Node first, Node last) {
		// distance
		addFeature(offset + 1, 10 * distance(first, last));

		// degree
		addFeature(offset + 2, Math.atan2(last.y - first.y, last.x - first.x));

		// absolute position
		addFeature(offset + 3, 10 * (first.x - 0.5));
		addFeature(offset + 4, 10 * (first.y - 0.5));
		addFeature(offset + 5, 10 * (last.x  - 0.5));
		addFeature(offset + 6, 10 * (last.y  - 0.5));

		// absolute degree
		addFeature(offset + 7, Math.atan2(first.y - 0.5, first.x - 0.5));
		addFeature(offset + 8, Math.atan2(last.y  - 0.5, last.x  - 0.5));

		// absolute distance
		addFeature(offset + 9,  10 * distance2(first));
		addFeature(offset + 10, 10 * distance2(last));

		//diff
		addFeature(offset + 11, 5 * (last.x - first.x));
		addFeature(offset + 12, 5 * (last.y - first.y));
	}

	private void makeMoveFeature(int sid, Node first, Node last) {
		int offset = 100000 + sid * 1000;
		makeBasicFeature(offset, first, last);
	}

	private void makeVertexFeature(int sid, List<NodePair> nodePairs) {
		for (int i=0; i<nodePairs.size(); ++i) {
			if (i > MAX_CHARACTER_SIZE)
				break;
			Node first = nodePairs.get(i).first;
			Node last  = nodePairs.get(i).last;
			if(first == null)
				continue;
			int offset = sid * 1000 + 20 * i;
			makeBasicFeature(offset, first, last);
		}
	}

	private void getVertex(Node first, Node last, int id, List<NodePair> nodePairs) {
		if(nodePairs.size() <= id) {
			for(int size=nodePairs.size(); size <= id; size++) {
				nodePairs.add(new NodePair());
			}
		}
		NodePair pair = nodePairs.get(id);
		pair.first = first;
		pair.last  = last;

		NodeSet nodeSet = new NodeSet(first, last);
		double dist = minimumDistance(nodeSet);

		if(dist > 0.001/*error factor*/) {
			getVertex(nodeSet.first, nodeSet.best, id * 2 + 1, nodePairs);
			getVertex(nodeSet.best,  nodeSet.last, id * 2 + 2, nodePairs);
		}
	}

	private void addFeature(int index, double value) {
		FeatureNode f = new FeatureNode();
		f.index = index;
		f.value = value;
		features.add(f);

	}

	public static Features read(Character character) {
		Features features = new Features();
		Node prev = null;

		// bias term
		{
			FeatureNode f = new FeatureNode();
			f.index = 0;
			f.value = 1.0;
			features.features.add(f);
		}

		List<List<Node>> nodes = new ArrayList<List<Node>>(character.getStrokesSize());
		for(int i=0; i<character.getStrokesSize(); i++) {
			nodes.add(new ArrayList<Node>());
		}

		{
			int height = character.getHeight();
			int width  = character.getWidth();
			if (height == 0 || width == 0) return null;
			if (character.getStrokesSize() == 0) return null;

			for (int i=0; i < character.getStrokesSize(); i++) {
				int ssize = character.getStrokeSize(i);
				if(ssize == 0) return null;

				for (int j=0; j < ssize; ++j) {
					Node n = new Node(
						1.0 * character.getX(i, j) / width,
						1.0 * character.getY(i, j) / height);
					if(j > 0)
						nodes.get(i).get(j-1).next = n;
					nodes.get(i).add(n);
				}
			}
		}

		for (int sid = 0; sid < nodes.size(); ++sid) {
			List<NodePair> nodePairs = new ArrayList<NodePair>();
			Node first = nodes.get(sid).get(0);
			Node last  = nodes.get(sid).get(nodes.get(sid).size() - 1);
			features.getVertex(first, last, 0, nodePairs);
			features.makeVertexFeature(sid, nodePairs);
			if(prev != null) {
				features.makeMoveFeature(sid, prev, first);
			}
			prev = last;
		}

		features.addFeature(2000000,  nodes.size());
		features.addFeature(2000000 + nodes.size(), 10);

		// sort
		Collections.sort(features.features, new Comparator<FeatureNode>() {
			@Override
			public int compare(FeatureNode f1, FeatureNode f2) {
				return f1.index - f2.index;
			}
		});

		{
			FeatureNode f = new FeatureNode();
			f.index = -1;
			f.value = 0.0;
			features.features.add(f);
		}
		return features;

	}

	public List<FeatureNode> get() {
		return features;
	}
}
