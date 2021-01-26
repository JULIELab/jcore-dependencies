package de.julielab.evaluation.entities;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Evaluation result of one evaluation group (e.g. entity class).
 */
public class EntityEvaluationResult {
    private Map<String, EvaluationStatistics> statisticsByDocumentMentionWise = Collections.emptyMap();
    private Map<String, EvaluationStatistics> statisticsByDocumentDocWise = Collections.emptyMap();
    private Map<String, EvaluationDataEntrySets> entrySetsByDocumentMentionWise = Collections.emptyMap();
    private Map<String, EvaluationDataEntrySets> entrySetsByDocumentDocWise = Collections.emptyMap();
    private String entityType;
    private EvaluationMode evaluationMode;

    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    public void addStatisticsByDocument(String docId, Set<EvaluationDataEntry> tpSet,
                                        Set<EvaluationDataEntry> fpSet, Set<EvaluationDataEntry> fnSet, EvaluationMode statsMode) {
        switch (statsMode) {
            case DOCUMENT:
                // Do not override mention mode if already set
                if (evaluationMode == null)
                    evaluationMode = statsMode;
                if (statisticsByDocumentDocWise.isEmpty())
                    statisticsByDocumentDocWise = new LinkedHashMap<>();
                statisticsByDocumentDocWise.put(docId, new EvaluationStatistics(tpSet.size(), fpSet.size(), fnSet.size()));
                if (entrySetsByDocumentDocWise.isEmpty())
                    entrySetsByDocumentDocWise = new LinkedHashMap<>();
                entrySetsByDocumentDocWise.put(docId, new EvaluationDataEntrySets(tpSet, fpSet, fnSet));
                break;
            case MENTION:
                evaluationMode = statsMode;
                if (statisticsByDocumentMentionWise.isEmpty())
                    statisticsByDocumentMentionWise = new LinkedHashMap<>();
                statisticsByDocumentMentionWise.put(docId,
                        new EvaluationStatistics(tpSet.size(), fpSet.size(), fnSet.size()));
                if (entrySetsByDocumentMentionWise.isEmpty())
                    entrySetsByDocumentMentionWise = new LinkedHashMap<>();
                entrySetsByDocumentMentionWise.put(docId, new EvaluationDataEntrySets(tpSet, fpSet, fnSet));
                break;
        }
    }

    public double getFMeasure(String documentId, EvaluationMode evaluationMode) {
        return evaluationMode == EvaluationMode.MENTION ? entrySetsByDocumentMentionWise.getOrDefault(documentId, EvaluationDataEntrySets.EMPTY).getFMeasure() : entrySetsByDocumentDocWise.getOrDefault(documentId, EvaluationDataEntrySets.EMPTY).getFMeasure();
    }

    public double getRecall(String documentId, EvaluationMode evaluationMode) {
        return evaluationMode == EvaluationMode.MENTION ? entrySetsByDocumentMentionWise.getOrDefault(documentId, EvaluationDataEntrySets.EMPTY).getRecall() : entrySetsByDocumentDocWise.getOrDefault(documentId, EvaluationDataEntrySets.EMPTY).getRecall();
    }

    public double getPrecision(String documentId, EvaluationMode evaluationMode) {
        return evaluationMode == EvaluationMode.MENTION ? entrySetsByDocumentMentionWise.getOrDefault(documentId, EvaluationDataEntrySets.EMPTY).getPrecision() : entrySetsByDocumentDocWise.getOrDefault(documentId, EvaluationDataEntrySets.EMPTY).getPrecision();
    }

    public double getFMeasure(String documentId) {
        return getFMeasure(documentId, evaluationMode);
    }

    public double getRecall(String documentId) {
        return getRecall(documentId, evaluationMode);
    }

    public double getPrecision(String documentId) {
        return getPrecision(documentId, evaluationMode);
    }

    public Map<String, EvaluationDataEntrySets> getEntrySetsByDocumentMentionWise() {
        return entrySetsByDocumentMentionWise;
    }

    public Map<String, EvaluationDataEntrySets> getEntrySetsByDocumentDocWise() {
        return entrySetsByDocumentDocWise;
    }

    public Stream<EvaluationDataEntry> getTpEvaluationDataEntriesMentionWise() {
        return entrySetsByDocumentMentionWise.values().stream().flatMap(set -> set.tpSet.stream());
    }

    public Stream<EvaluationDataEntry> getFpEvaluationDataEntriesMentionWise() {
        return entrySetsByDocumentMentionWise.values().stream().flatMap(set -> set.fpSet.stream());
    }

    public Stream<EvaluationDataEntry> getFnEvaluationDataEntriesMentionWise() {
        return entrySetsByDocumentMentionWise.values().stream().flatMap(set -> set.fnSet.stream());
    }

    public Stream<EvaluationDataEntry> getTpEvaluationDataEntriesDocWise() {
        return entrySetsByDocumentDocWise.values().stream().flatMap(set -> set.tpSet.stream());
    }

    public Stream<EvaluationDataEntry> getFpEvaluationDataEntriesDocWise() {
        return entrySetsByDocumentDocWise.values().stream().flatMap(set -> set.fpSet.stream());
    }

    public Stream<EvaluationDataEntry> getFnEvaluationDataEntriesDocWise() {
        return entrySetsByDocumentDocWise.values().stream().flatMap(set -> set.fnSet.stream());
    }

    public Map<String, EvaluationStatistics> getStatisticsByDocumentMentionWise() {
        return statisticsByDocumentMentionWise;
    }

    public Map<String, EvaluationStatistics> getStatisticsByDocumentDocWise() {
        return statisticsByDocumentDocWise;
    }

    public double getMacroFMeasureMentionWise() {
        double sum = 0;
        for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentMentionWise.entrySet()) {
            sum += entry.getValue().getFMeasure();
        }
        return sum / statisticsByDocumentMentionWise.size();
    }

    public double getMacroPrecisionMentionWise() {
        double sum = 0;
        for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentMentionWise.entrySet()) {
            sum += entry.getValue().getPrecision();
        }
        return sum / statisticsByDocumentMentionWise.size();
    }

    public double getMacroRecallMentionWise() {
        double sum = 0;
        for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentMentionWise.entrySet()) {
            sum += entry.getValue().getRecall();
        }
        return sum / statisticsByDocumentMentionWise.size();
    }

    public double getMacroFMeasureDocWise() {
        double sum = 0;
        for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentDocWise.entrySet()) {
            sum += entry.getValue().getFMeasure();
        }
        return sum / statisticsByDocumentDocWise.size();
    }

    public double getMicroFMeasureDocWise() {
        return FMeasure.getFMeasure(getSumTpDocWise(), getSumFpDocWise(), getSumFnDocWise());
    }

    public double getMicroRecallDocWise() {
        return FMeasure.getRecall(getSumTpDocWise(), getSumFpDocWise(), getSumFnDocWise());
    }

    public double getMicroPrecisionDocWise() {
        return FMeasure.getPrecision(getSumTpDocWise(), getSumFpDocWise(), getSumFnDocWise());
    }

    public double getMicroFMeasureMentionWise() {
        return FMeasure.getFMeasure(getSumTpMentionWise(), getSumFpMentionWise(), getSumFnMentionWise());
    }

    public double getMicroRecallMentionWise() {
        return FMeasure.getRecall(getSumTpMentionWise(), getSumFpMentionWise(), getSumFnMentionWise());
    }

    public double getMicroPrecisionMentionWise() {
        return FMeasure.getPrecision(getSumTpMentionWise(), getSumFpMentionWise(), getSumFnMentionWise());
    }

    public double getMacroPrecisionDocWise() {
        double sum = 0;
        for (Entry<String, EvaluationStatistics> entry : statisticsByDocumentDocWise.entrySet()) {
            sum += entry.getValue().getPrecision();
        }
        return sum / statisticsByDocumentDocWise.size();
    }

    public double getMacroRecallDocWise() {
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
        sb.append("  Recall (micro):    ").append(getMicroRecallDocWise()).append("\n");
        sb.append("  Precision (micro): ").append(getMicroPrecisionDocWise()).append("\n");
        sb.append("  F-Score (micro):   ").append(getMicroFMeasureDocWise()).append("\n");
        sb.append("  Recall (macro):    ").append(getMacroRecallDocWise()).append("\n");
        sb.append("  Precision (macro): ").append(getMacroPrecisionDocWise()).append("\n");
        sb.append("  F-Score (macro):   ").append(getMacroFMeasureDocWise()).append("\n");

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
            sb.append("  Recall (micro):    ").append(getMicroRecallMentionWise()).append("\n");
            sb.append("  Precision (micro): ").append(getMicroPrecisionMentionWise()).append("\n");
            sb.append("  F-Score (micro):   ").append(getMicroFMeasureMentionWise()).append("\n");
            sb.append("  Recall (macro):    ").append(getMacroRecallMentionWise()).append("\n");
            sb.append("  Precision (macro): ").append(getMacroPrecisionMentionWise()).append("\n");
            sb.append("  F-Score (macro):   ").append(getMacroFMeasureMentionWise()).append("\n");
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
        sb.append("  Recall (micro):    ").append(getMicroRecallDocWise()).append("\n");
        sb.append("  Precision (micro): ").append(getMicroPrecisionDocWise()).append("\n");
        sb.append("  F-Score (micro):   ").append(getMicroFMeasureDocWise()).append("\n");

        sb.append("Mention Level:\n");
        if (null != statisticsByDocumentMentionWise) {
            sb.append("  TP: ").append(getSumTpMentionWise()).append("\n");
            sb.append("  FP: ").append(getSumFpMentionWise()).append("\n");
            sb.append("  FN: ").append(getSumFnMentionWise()).append("\n");
            sb.append("  Recall (micro):    ").append(getMicroRecallMentionWise()).append("\n");
            sb.append("  Precision (micro): ").append(getMicroPrecisionMentionWise()).append("\n");
            sb.append("  F-Score (micro):   ").append(getMicroFMeasureMentionWise()).append("\n");
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

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

}
