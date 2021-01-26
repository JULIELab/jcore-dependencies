package de.julielab.evaluation.entities;

import java.util.Collections;
import java.util.Set;

public class EvaluationDataEntrySets {
	public static final EvaluationDataEntrySets EMPTY = new EvaluationDataEntrySets(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
	public EvaluationDataEntrySets(Set<EvaluationDataEntry> tpSet, Set<EvaluationDataEntry> fpSet,
			Set<EvaluationDataEntry> fnSet) {
		this.tpSet = tpSet;
		this.fpSet = fpSet;
		this.fnSet = fnSet;
	}

	public Set<EvaluationDataEntry> tpSet;
	public Set<EvaluationDataEntry> fpSet;
	public Set<EvaluationDataEntry> fnSet;

	public Set<EvaluationDataEntry> get(Statistics type) {
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
