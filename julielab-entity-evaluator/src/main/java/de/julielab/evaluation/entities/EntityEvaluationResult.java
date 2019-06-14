package de.julielab.evaluation.entities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

public class EntityEvaluationResult {
	private LinkedHashMap<String, EvaluationStatistics> statisticsByDocumentMentionWise;
	private LinkedHashMap<String, EvaluationStatistics> statisticsByDocumentDocWise;
	private LinkedHashMap<String, EvaluationDataEntrySets> entrySetsByDocumentMentionWise;
	private LinkedHashMap<String, EvaluationDataEntrySets> entrySetsByDocumentDocWise;
	private String entityType;

	public void addStatisticsByDocument(String docId, Set<EvaluationDataEntry> tpSet,
			Set<EvaluationDataEntry> fpSet, Set<EvaluationDataEntry> fnSet, EvaluationMode statsMode) {
		switch (statsMode) {
		case DOCUMENT:
			if (null == statisticsByDocumentDocWise)
				statisticsByDocumentDocWise = new LinkedHashMap<>();
			statisticsByDocumentDocWise.put(docId, new EvaluationStatistics(tpSet.size(), fpSet.size(), fnSet.size()));
			if (null == entrySetsByDocumentDocWise)
				entrySetsByDocumentDocWise = new LinkedHashMap<>();
			entrySetsByDocumentDocWise.put(docId, new EvaluationDataEntrySets(tpSet, fpSet, fnSet));
			break;
		case MENTION:
			if (null == statisticsByDocumentMentionWise)
				statisticsByDocumentMentionWise = new LinkedHashMap<>();
			statisticsByDocumentMentionWise.put(docId,
					new EvaluationStatistics(tpSet.size(), fpSet.size(), fnSet.size()));
			if (null == entrySetsByDocumentMentionWise)
				entrySetsByDocumentMentionWise = new LinkedHashMap<>();
			entrySetsByDocumentMentionWise.put(docId, new EvaluationDataEntrySets(tpSet, fpSet, fnSet));
			break;
		}
	}

	public Map<String, EvaluationDataEntrySets> getEntrySetsByDocumentMentionWise() {
		return entrySetsByDocumentMentionWise;
	}

	public Map<String, EvaluationDataEntrySets> getEntrySetsByDocumentDocWise() {
		return entrySetsByDocumentDocWise;
	}
	
	public Stream<EvaluationDataEntry> getTpEvaluationDataEntriesMentionWise(){
		return entrySetsByDocumentMentionWise.values().stream().flatMap(set -> set.tpSet.stream());
	}
	
	public Stream<EvaluationDataEntry> getFpEvaluationDataEntriesMentionWise(){
		return entrySetsByDocumentMentionWise.values().stream().flatMap(set -> set.fpSet.stream());
	}
	
	public Stream<EvaluationDataEntry> getFnEvaluationDataEntriesMentionWise(){
		return entrySetsByDocumentMentionWise.values().stream().flatMap(set -> set.fnSet.stream());
	}
	
	public Stream<EvaluationDataEntry> getTpEvaluationDataEntriesDocWise(){
		return entrySetsByDocumentDocWise.values().stream().flatMap(set -> set.tpSet.stream());
	}
	
	public Stream<EvaluationDataEntry> getFpEvaluationDataEntriesDocWise(){
		return entrySetsByDocumentDocWise.values().stream().flatMap(set -> set.fpSet.stream());
	}
	
	public Stream<EvaluationDataEntry> getFnEvaluationDataEntriesDocWise(){
		return entrySetsByDocumentDocWise.values().stream().flatMap(set -> set.fnSet.stream());
	}

	public Map<String, EvaluationStatistics> getStatisticsByDocumentMentionWise() {
		return statisticsByDocumentMentionWise;
	}

	public Map<String, EvaluationStatistics> getStatisticsByDocumentDocWise() {
		return statisticsByDocumentDocWise;
	}

	public double getAvgFMeasureMentionWise() {
		double sum = 0;
		for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentMentionWise.entrySet()) {
			sum += entry.getValue().getFMeasure();
		}
		return sum / statisticsByDocumentMentionWise.size();
	}

	public double getAvgPrecisionMentionWise() {
		double sum = 0;
		for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentMentionWise.entrySet()) {
			sum += entry.getValue().getPrecision();
		}
		return sum / statisticsByDocumentMentionWise.size();
	}

	public double getAvgRecallMentionWise() {
		double sum = 0;
		for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentMentionWise.entrySet()) {
			sum += entry.getValue().getRecall();
		}
		return sum / statisticsByDocumentMentionWise.size();
	}

	public double getAvgFMeasureDocWise() {
		double sum = 0;
		for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentDocWise.entrySet()) {
			sum += entry.getValue().getFMeasure();
		}
		return sum / statisticsByDocumentDocWise.size();
	}

	public double getOverallFMeasureDocWise() {
		return FMeasure.getFMeasure(getSumTpDocWise(), getSumFpDocWise(), getSumFnDocWise());
	}

	public double getOverallRecallDocWise() {
		return FMeasure.getRecall(getSumTpDocWise(), getSumFpDocWise(), getSumFnDocWise());
	}

	public double getOverallPrecisionDocWise() {
		return FMeasure.getPrecision(getSumTpDocWise(), getSumFpDocWise(), getSumFnDocWise());
	}

	public double getOverallFMeasureMentionWise() {
		return FMeasure.getFMeasure(getSumTpMentionWise(), getSumFpMentionWise(), getSumFnMentionWise());
	}

	public double getOverallRecallMentionWise() {
		return FMeasure.getRecall(getSumTpMentionWise(), getSumFpMentionWise(), getSumFnMentionWise());
	}

	public double getOverallPrecisionMentionWise() {
		return FMeasure.getPrecision(getSumTpMentionWise(), getSumFpMentionWise(), getSumFnMentionWise());
	}

	public double getAvgPrecisionDocWise() {
		double sum = 0;
		for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentDocWise.entrySet()) {
			sum += entry.getValue().getPrecision();
		}
		return sum / statisticsByDocumentDocWise.size();
	}

	public double getAvgRecallDocWise() {
		double sum = 0;
		for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentDocWise.entrySet()) {
			sum += entry.getValue().getRecall();
		}
		return sum / statisticsByDocumentDocWise.size();
	}

	public int getSumFpDocWise() {
		int sum = 0;
		for (EvaluationStatistics stats : statisticsByDocumentDocWise.values())
			sum += stats.fp;

		return sum;
	}

	public int getSumFnDocWise() {
		int sum = 0;
		for (EvaluationStatistics stats : statisticsByDocumentDocWise.values())
			sum += stats.fn;

		return sum;
	}

	public int getSumTpDocWise() {
		int sum = 0;
		for (EvaluationStatistics stats : statisticsByDocumentDocWise.values())
			sum += stats.tp;

		return sum;
	}

	public int getSumFpMentionWise() {
		int sum = 0;
		for (EvaluationStatistics stats : statisticsByDocumentMentionWise.values())
			sum += stats.fp;

		return sum;
	}

	public int getSumFnMentionWise() {
		int sum = 0;
		for (EvaluationStatistics stats : statisticsByDocumentMentionWise.values())
			sum += stats.fn;

		return sum;
	}

	public int getSumTpMentionWise() {
		int sum = 0;
		for (EvaluationStatistics stats : statisticsByDocumentMentionWise.values())
			sum += stats.tp;

		return sum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((statisticsByDocumentDocWise == null) ? 0 : statisticsByDocumentDocWise.hashCode());
		result = prime * result
				+ ((statisticsByDocumentMentionWise == null) ? 0 : statisticsByDocumentMentionWise.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityEvaluationResult other = (EntityEvaluationResult) obj;
		if (statisticsByDocumentDocWise == null) {
			if (other.statisticsByDocumentDocWise != null)
				return false;
		} else if (!statisticsByDocumentDocWise.equals(other.statisticsByDocumentDocWise))
			return false;
		if (statisticsByDocumentMentionWise == null) {
			if (other.statisticsByDocumentMentionWise != null)
				return false;
		} else if (!statisticsByDocumentMentionWise.equals(other.statisticsByDocumentMentionWise))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityEvaluationResult [statisticsByDocumentMentionWise=" + statisticsByDocumentMentionWise
				+ ", statisticsByDocumentDocWise=" + statisticsByDocumentDocWise + "]";
	}

	public String getEvaluationReportLong() {
		StringBuilder sb = new StringBuilder();
		sb.append("Evaluation results for entity type \"" + entityType + "\":\n");
		sb.append("Document Level:\n");
		for (Entry<String, EvaluationStatistics> entry : this.getStatisticsByDocumentDocWise().entrySet()) {
			String docId = entry.getKey();
			EvaluationStatistics stats = entry.getValue();
			sb.append(docId + ": F: " + stats.getFMeasure() + ", R: " + stats.getRecall() + ", P: "
					+ stats.getPrecision() + " (tp: " + stats.tp + ", fp: " + stats.fp + ", fn: " + stats.fn + ")");
			sb.append("\n");
		}
		sb.append("  TP: ").append(getSumTpDocWise()).append("\n");
		sb.append("  FP: ").append(getSumFpDocWise()).append("\n");
		sb.append("  FN: ").append(getSumFnDocWise()).append("\n");
		sb.append("  Recall:    ").append(getOverallRecallDocWise()).append("\n");
		sb.append("  Precision: ").append(getOverallPrecisionDocWise()).append("\n");
		sb.append("  F-Score:   ").append(getOverallFMeasureDocWise()).append("\n");
		sb.append("  Average Recall:    ").append(getAvgRecallDocWise()).append("\n");
		sb.append("  Average Precision: ").append(getAvgPrecisionDocWise()).append("\n");
		sb.append("  Average F-Score:   ").append(getAvgFMeasureDocWise()).append("\n");

		sb.append("Mention Level:\n");
		if (null != statisticsByDocumentMentionWise) {
			for (Entry<String, EvaluationStatistics> entry : this.getStatisticsByDocumentMentionWise().entrySet()) {
				String docId = entry.getKey();
				EvaluationStatistics stats = entry.getValue();
				sb.append(docId + ": F: " + stats.getFMeasure() + ", R: " + stats.getRecall() + ", P: "
						+ stats.getPrecision() + " (tp: " + stats.tp + ", fp: " + stats.fp + ", fn: " + stats.fn + ")");
				sb.append("\n");
			}
			sb.append("  TP: ").append(getSumTpMentionWise()).append("\n");
			sb.append("  FP: ").append(getSumFpMentionWise()).append("\n");
			sb.append("  FN: ").append(getSumFnMentionWise()).append("\n");
			sb.append("  Recall:    ").append(getOverallRecallMentionWise()).append("\n");
			sb.append("  Precision: ").append(getOverallPrecisionMentionWise()).append("\n");
			sb.append("  F-Score:   ").append(getOverallFMeasureMentionWise()).append("\n");
			sb.append("  Average Recall:    ").append(getAvgRecallMentionWise()).append("\n");
			sb.append("  Average Precision: ").append(getAvgPrecisionMentionWise()).append("\n");
			sb.append("  Average F-Score:   ").append(getAvgFMeasureMentionWise()).append("\n");
		} else {
			sb.append("No mention information available.");
		}
		sb.append("\n");
		return sb.toString();
	}

	public String getEvaluationReportShort() {
		StringBuilder sb = new StringBuilder();
		sb.append("Evaluation results for entity type \"" + entityType + "\":\n");
		sb.append("Document Level:\n");
		sb.append("  TP: ").append(getSumTpDocWise()).append("\n");
		sb.append("  FP: ").append(getSumFpDocWise()).append("\n");
		sb.append("  FN: ").append(getSumFnDocWise()).append("\n");
		sb.append("  Recall:    ").append(getOverallRecallDocWise()).append("\n");
		sb.append("  Precision: ").append(getOverallPrecisionDocWise()).append("\n");
		sb.append("  F-Score:   ").append(getOverallFMeasureDocWise()).append("\n");

		sb.append("Mention Level:\n");
		if (null != statisticsByDocumentMentionWise) {
			sb.append("  TP: ").append(getSumTpMentionWise()).append("\n");
			sb.append("  FP: ").append(getSumFpMentionWise()).append("\n");
			sb.append("  FN: ").append(getSumFnMentionWise()).append("\n");
			sb.append("  Recall:    ").append(getOverallRecallMentionWise()).append("\n");
			sb.append("  Precision: ").append(getOverallPrecisionMentionWise()).append("\n");
			sb.append("  F-Score:   ").append(getOverallFMeasureMentionWise()).append("\n");
		} else {
			sb.append("No mention information available.");
		}
		return sb.toString();
	}

	public String getEvaluationReportAllTp(EvaluationMode mode) {
		return getEvaluationReportAllEntries(mode, Statistics.TP);
	}
	
	public String getEvaluationReportAllFp(EvaluationMode mode) {
		return getEvaluationReportAllEntries(mode, Statistics.FP);
	}
	
	public String getEvaluationReportAllFn(EvaluationMode mode) {
		return getEvaluationReportAllEntries(mode, Statistics.FN);
	}
	
	public String getEvaluationReportAllEntries(EvaluationMode mode, Statistics type) {
		StringBuilder sb = new StringBuilder();
		switch (mode) {
		case DOCUMENT:
			for (Entry<String, EvaluationDataEntrySets> entry : entrySetsByDocumentDocWise.entrySet()) {
				for (EvaluationDataEntry dataEntry : entry.getValue().get(type)) {
					sb.append(entry.getKey());
					sb.append(":");
					sb.append(dataEntry.getEntityId());
					sb.append("\n");
				}
			}
			break;
		case MENTION:
			for (Entry<String, EvaluationDataEntrySets> entry : entrySetsByDocumentMentionWise.entrySet()) {
				for (EvaluationDataEntry dataEntry : entry.getValue().get(type)) {
					sb.append(entry.getKey());
					sb.append(":");
					sb.append(dataEntry.getEntityId());
					sb.append("\n");
				}
			}
			break;
		}
		return sb.toString();
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getEntityType() {
		return entityType;
	}

}
