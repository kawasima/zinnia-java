package net.unit8.zinnia;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SVM {
	private static final double EPS = 0.1;
	private static final double INF = 1e+37;
	private static final int MAX_ITERATION = 2000;

	public static boolean train(
			int l,
			int n,
			List<Double> y,
			List<List<FeatureNode>> x,
			double C,
			double[] w) {
		int activeSize = l;
		double PGmaxOld = INF;
		double PGminOld = -INF;
		double[] qd = new double[l];
		int[] index = new int[l];
		double[] alpha = new double[l];

		for(int i=0; i<l ; i++) {
			index[i]=i;
			qd[i] = 0.0;

			List<FeatureNode> featureNodes = x.get(i);
			for (FeatureNode f : featureNodes) {
				if (f.index < 0)
					break;
				qd[i] += (f.value * f.value);
			}
		}

		for(int iter=0; iter<MAX_ITERATION; ++iter) {
			double PGmaxNew = -INF;
			double PGminNew = INF;
			Collections.shuffle(Arrays.asList(index));
			for (int s = 0; s < activeSize; ++s) {
				int i = index[s];
				double G = 0.0;

				List<FeatureNode> featureNodes = x.get(i);
				for (FeatureNode f : featureNodes ) {
					if(f.index < 0) break;
					G += w[f.index] * f.value;
				}
				G = G * y.get(i) - 1;
				double PG = 0.0;

				if (alpha[i] == 0.0) {
					if (G > PGmaxOld) {
						activeSize--;
						int tmp = index[s];
						index[s] = index[activeSize];
						index[activeSize] = tmp;
						s--;
						continue;
					} else if (G < 0.0) {
						PG = G;
					}
				} else if (alpha[i] == C) {
					if (G < PGminOld) {
						activeSize--;
						int tmp = index[s];
						index[s] = index[activeSize];
						index[activeSize] = tmp;
						s--;
						continue;
					} else if (G > 0.0) {
						PG = G;
					}
				} else {
					PG = G;
				}

				PGmaxNew = Math.max(PGmaxNew, PG);
				PGminNew = Math.min(PGminNew, PG);

				if (Math.abs(PG) > 1.0e-12) {
					double alphaOld = alpha[i];
					alpha[i] = Math.min(Math.max(alpha[i] - G/qd[i], 0.0), C);
					double d = (alpha[i] - alphaOld) * y.get(i);
					for (FeatureNode f : featureNodes ) {
						if(f.index < 0) break;
						w[f.index]+= d * f.value;
					}
				}
			}

			if (iter % 4 == 0) {
				System.out.print(".");
			}

			if ((PGmaxNew - PGminNew) < EPS) {
				if (activeSize == l) {
					break;
				} else {
					activeSize = l;
					PGmaxOld = INF;
					PGminOld = -INF;
					continue;
				}
			}

			PGmaxOld = PGmaxNew;
			PGminOld = PGminNew;
			if (PGmaxOld <= 0) {
				PGmaxOld = INF;
			}
			if (PGminOld <= 0) {
				PGminOld = -INF;
			}
		}
		System.out.println();
		return true;

	}
}
