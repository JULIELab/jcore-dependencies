package de.julielab.evaluation.entities;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Optional;

/**
 * This class is just a map from string (should be the entity type for a result)
 * to entity evaluation result. It defines one additional method,
 * {@link #getSingle()} to quickly get the single evaluation result, if there is
 * only one.
 * 
 * @author faessler
 *
 */
public class EntityEvaluationResults extends HashMap<String, EntityEvaluationResult> {

	public static final String OVERALL = "OVERALL";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7101952595130357247L;

	public EntityEvaluationResult getSingle() {
		if (size() > 1)
			throw new IllegalStateException("There is more than a single evaluation result in this list.");
		Optional<EntityEvaluationResult> optional = values().stream().findFirst();
		if (!optional.isPresent())
			throw new IllegalStateException("The evaluation results list is empty.");
		return optional.get();
	}
	
	public EntityEvaluationResult getOverallResult() {
		 EntityEvaluationResult overallResult = get(OVERALL);
		 if (overallResult == null && size() == 1)
			 overallResult = getSingle();
		 return overallResult;
	}
	
	public String getEvaluationReportShort() {
		StringBuilder sb = new StringBuilder();
		for (EntityEvaluationResult res : values()) {
			sb.append(res.getEvaluationReportShort());
			sb.append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));
		}
		if (size() > 1) {
			sb.append("Evaluation results across all classes:");
			sb.append("Document Level:");
			Triple<Double, Double, Double> microStatsDocWise = getMicroStatsDocWise();
			Triple<Double, Double, Double> macroStatsDocWise = getMacroStatsDocWise();
			sb.append("  Recall (micro):    " + microStatsDocWise.getLeft());
			sb.append("  Precision (micro): " + microStatsDocWise.getLeft());
			sb.append("  F-Score (micro):   " + microStatsDocWise.getLeft());
			sb.append("  Recall (macro):    " + macroStatsDocWise.getLeft());
			sb.append("  Precision (macro): " + macroStatsDocWise.getLeft());
			sb.append("  F-Score (macro):   " + macroStatsDocWise.getLeft());
			sb.append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));

			if (getEvaluationMode() == EvaluationMode.MENTION) {
				sb.append("Document Level:");
				Triple<Double, Double, Double> microStatsMentionWise = getMicroStatsMentionWise();
				Triple<Double, Double, Double> macroStatsMentionWise = getMacroStatsMentionWise();
				sb.append("  Recall (micro):    " + microStatsMentionWise.getLeft());
				sb.append("  Precision (micro): " + microStatsMentionWise.getLeft());
				sb.append("  F-Score (micro):   " + microStatsMentionWise.getLeft());
				sb.append("  Recall (macro):    " + macroStatsMentionWise.getLeft());
				sb.append("  Precision (macro): " + macroStatsMentionWise.getLeft());
				sb.append("  F-Score (macro):   " + macroStatsMentionWise.getLeft());
				sb.append(System.getProperty("line.separator"));
				sb.append(System.getProperty("line.separator"));
			}
		}
		return sb.toString();
	}

	public EvaluationMode getEvaluationMode() {
		return values().stream().anyMatch(r -> r.getEvaluationMode() == EvaluationMode.MENTION) ? EvaluationMode.MENTION : EvaluationMode.DOCUMENT;
	}
	
	public String getEvaluationReportLong() {
		StringBuilder sb = new StringBuilder();
		for (EntityEvaluationResult res : values()) {
			sb.append(res.getEvaluationReportLong());
			sb.append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}

	public Triple<Double, Double, Double> getMacroStatsDocWise() {
		double macroRecall = values().stream().mapToDouble(EntityEvaluationResult::getMicroRecallDocWise).average().orElse(0);
		double macroPrecision = values().stream().mapToDouble(EntityEvaluationResult::getMicroPrecisionDocWise).average().orElse(0);
		double macroF = values().stream().mapToDouble(EntityEvaluationResult::getMicroFMeasureDocWise).average().orElse(0);
		return new ImmutableTriple<>(macroRecall, macroPrecision, macroF);
	}

	public Triple<Double, Double, Double> getMicroStatsDocWise() {
		int sumTps = values().stream().mapToInt(r -> r.getSumTpDocWise()).sum();
		int sumFps = values().stream().mapToInt(r -> r.getSumFpDocWise()).sum();
		int sumFns = values().stream().mapToInt(r -> r.getSumFnDocWise()).sum();
		return new ImmutableTriple<>(FMeasure.getRecall(sumTps, sumFps, sumFns), FMeasure.getPrecision(sumTps, sumFps, sumFns), FMeasure.getFMeasure(sumTps, sumFps, sumFns));
	}

	public Triple<Double, Double, Double> getMacroStatsMentionWise() {
		double macroRecall = values().stream().mapToDouble(EntityEvaluationResult::getMicroRecallMentionWise).average().orElse(0);
		double macroPrecision = values().stream().mapToDouble(EntityEvaluationResult::getMicroPrecisionMentionWise).average().orElse(0);
		double macroF = values().stream().mapToDouble(EntityEvaluationResult::getMicroFMeasureMentionWise).average().orElse(0);
		return new ImmutableTriple<>(macroRecall, macroPrecision, macroF);
	}

	public Triple<Double, Double, Double> getMicroStatsMentionWise() {
		int sumTps = values().stream().mapToInt(r -> r.getSumTpMentionWise()).sum();
		int sumFps = values().stream().mapToInt(r -> r.getSumFpMentionWise()).sum();
		int sumFns = values().stream().mapToInt(r -> r.getSumFnMentionWise()).sum();
		return new ImmutableTriple<>(FMeasure.getRecall(sumTps, sumFps, sumFns), FMeasure.getPrecision(sumTps, sumFps, sumFns), FMeasure.getFMeasure(sumTps, sumFps, sumFns));
	}
}
