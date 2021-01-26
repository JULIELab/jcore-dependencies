package de.julielab.evaluation.entities;

public class FMeasure {
	public static double getFMeasure(int tp, int fp, int fn) {
		double R = getRecall(tp, fp, fn);
		double P = getPrecision(tp, fp, fn);
		if (R + P == 0)
			return 0;
		return (2 * P * R) / (P + R);
	}

	public static double getRecall(int tp, int fp, int fn) {
		if (tp + fn == 0)
			return 0;
		return (double) tp / (tp + fn);
	}

	public static double getPrecision(int tp, int fp, int fn) {
		if (tp + fp == 0)
			return 0;
		return (double) tp / (tp + fp);
	}
}
