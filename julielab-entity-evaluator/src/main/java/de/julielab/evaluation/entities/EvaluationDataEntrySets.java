package de.julielab.evaluation.entities;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.util.Collections;
import java.util.Set;

public class EvaluationDataEntrySets {
	public static final EvaluationDataEntrySets EMPTY = new EvaluationDataEntrySets(ImmutableMultiset.of(), ImmutableMultiset.of(), ImmutableMultiset.of());
	public EvaluationDataEntrySets(Multiset<EvaluationDataEntry> tpSet, Multiset<EvaluationDataEntry> fpSet,
								   Multiset<EvaluationDataEntry> fnSet) {
		this.tpSet = tpSet;
		this.fpSet = fpSet;
		this.fnSet = fnSet;
	}

	public Multiset<EvaluationDataEntry> tpSet;
	public Multiset<EvaluationDataEntry> fpSet;
	public Multiset<EvaluationDataEntry> fnSet;

	public Multiset<EvaluationDataEntry> get(Statistics type) {
		switch (type) {
		case TP:
			return tpSet;
		case FP:
			return fpSet;
		case FN:
			return fnSet;
		}
		return null;
	}

	public double getFMeasure() {
		return FMeasure.getFMeasure(tpSet.size(), fpSet.size(), fnSet.size());
	}

	public double getRecall() {
		return FMeasure.getRecall(tpSet.size(), fpSet.size(), fnSet.size());
	}

	public double getPrecision() {
		return FMeasure.getPrecision(tpSet.size(), fpSet.size(), fnSet.size());
	}
}
