package de.julielab.evaluation.entities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Sets.SetView;

public class EntityEvaluationResult {
	private LinkedHashMap<String, EvaluationStatistics> statisticsByDocumentMentionWise;
	private LinkedHashMap<String, EvaluationStatistics> statisticsByDocumentDocWise;
	private LinkedHashMap<String, EvaluationDataEntrySets> entrySetsByDocumentMentionWise;
	private LinkedHashMap<String, EvaluationDataEntrySets> entrySetsByDocumentDocWise;

	public void addStatisticsByDocument(String docId, SetView<EvaluationDataEntry> tpSet,
			SetView<EvaluationDataEntry> fpSet, SetView<EvaluationDataEntry> fnSet, EvaluationMode statsMode) {
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

	public double getAvgPMeasureMentionWise() {
		double sum = 0;
		for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentMentionWise.entrySet()) {
			sum += entry.getValue().getPrecision();
		}
		return sum / statisticsByDocumentMentionWise.size();
	}

	public double getAvgRMeasureMentionWise() {
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
		sb.append("Results doc-wise by document:\n");
		for (Entry<String, EvaluationStatistics> entry : this.getStatisticsByDocumentDocWise().entrySet()) {
			String docId = entry.getKey();
			EvaluationStatistics stats = entry.getValue();
			sb.append(docId + ": F: " + stats.getFMeasure() + ", R: " + stats.getRecall() + ", P: "
					+ stats.getPrecision() + " (tp: " + stats.tp + ", fp: " + stats.fp + ", fn: " + stats.fn + ")");
			sb.append("\n");
		}
		sb.append("Mean: F: " + this.getAvgFMeasureDocWise() + ", R: " + this.getAvgRecallDocWise() + ", P: "
				+ this.getAvgPrecisionDocWise() + " (tp: " + this.getSumTpDocWise() + ", fp: " + this.getSumFpDocWise()
				+ ", fn: " + this.getSumFnDocWise() + ")");
		sb.append("\n");
		sb.append("Overall: F: " + this.getOverallFMeasureDocWise() + ", R: " + this.getOverallRecallDocWise()
				+ ", P: " + this.getOverallPrecisionDocWise() + " (tp: " + this.getSumTpDocWise() + ", fp: "
				+ this.getSumFpDocWise() + ", fn: " + this.getSumFnDocWise() + ")");
		sb.append("\n\n");

		sb.append("Results mention-wise by document:\n");
		if (null != statisticsByDocumentMentionWise) {
			for (Entry<String, EvaluationStatistics> entry : this.getStatisticsByDocumentMentionWise().entrySet()) {
				String docId = entry.getKey();
				EvaluationStatistics stats = entry.getValue();
				sb.append(docId + ": F: " + stats.getFMeasure() + ", R: " + stats.getRecall() + ", P: "
						+ stats.getPrecision() + " (tp: " + stats.tp + ", fp: " + stats.fp + ", fn: " + stats.fn + ")");
				sb.append("\n");
			}
			sb.append("Mean: F: " + this.getAvgFMeasureMentionWise() + ", R: " + this.getAvgRMeasureMentionWise()
					+ ", P: " + this.getAvgPMeasureMentionWise() + " (tp: " + this.getSumTpMentionWise() + ", fp: "
					+ this.getSumFpMentionWise() + ", fn: " + this.getSumFnMentionWise() + ")");
			sb.append("\n");
			sb.append("Overall: F: " + this.getOverallFMeasureMentionWise() + ", R: "
					+ this.getOverallRecallMentionWise() + ", P: " + this.getOverallPrecisionMentionWise() + " (tp: "
					+ this.getSumTpMentionWise() + ", fp: " + this.getSumFpMentionWise() + ", fn: "
					+ this.getSumFnMentionWise() + ")");
		} else {
			sb.append("No mention information available.");
		}
		return sb.toString();
	}

	public String getEvaluationReportShort() {
		StringBuilder sb = new StringBuilder();
		sb.append("Results doc-wise by document:\n");
		sb.append("Mean: F: " + this.getAvgFMeasureDocWise() + ", R: " + this.getAvgRecallDocWise() + ", P: "
				+ this.getAvgPrecisionDocWise() + " (tp: " + this.getSumTpDocWise() + ", fp: " + this.getSumFpDocWise()
				+ ", fn: " + this.getSumFnDocWise() + ")");
		sb.append("\n");
		sb.append("Overall: F: " + this.getOverallFMeasureDocWise() + ", R: " + this.getOverallRecallDocWise()
				+ ", P: " + this.getOverallPrecisionDocWise() + " (tp: " + this.getSumTpDocWise() + ", fp: "
				+ this.getSumFpDocWise() + ", fn: " + this.getSumFnDocWise() + ")");
		sb.append("\n\n");

		sb.append("Results mention-wise by document:\n");
		if (null != statisticsByDocumentMentionWise) {
			sb.append("Mean: F: " + this.getAvgFMeasureMentionWise() + ", R: " + this.getAvgRMeasureMentionWise()
					+ ", P: " + this.getAvgPMeasureMentionWise() + " (tp: " + this.getSumTpMentionWise() + ", fp: "
					+ this.getSumFpMentionWise() + ", fn: " + this.getSumFnMentionWise() + ")");
			sb.append("\n");
			sb.append("Overall: F: " + this.getOverallFMeasureMentionWise() + ", R: "
					+ this.getOverallRecallMentionWise() + ", P: " + this.getOverallPrecisionMentionWise() + " (tp: "
					+ this.getSumTpMentionWise() + ", fp: " + this.getSumFpMentionWise() + ", fn: "
					+ this.getSumFnMentionWise() + ")");
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

}
