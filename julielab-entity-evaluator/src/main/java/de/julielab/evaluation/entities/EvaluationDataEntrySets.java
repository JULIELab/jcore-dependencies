package de.julielab.evaluation.entities;

import java.util.Set;

import com.google.common.collect.Sets.SetView;

public class EvaluationDataEntrySets {
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
}
