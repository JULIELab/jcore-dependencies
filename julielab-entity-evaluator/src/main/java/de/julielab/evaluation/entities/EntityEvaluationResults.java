package de.julielab.evaluation.entities;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Optional;

/**
 * <p>Evaluation result of multiple entity groups (e.g. entity class or label groups).</p>
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
		String ls = System.getProperty("line.separator");
		for (EntityEvaluationResult res : values()) {
			sb.append(res.getEvaluationReportShort());
			sb.append(ls);
			sb.append(ls);
		}
		if (size() > 1) {
			sb.append("Evaluation results across all classes:").append(ls);
			sb.append("Document Level:").append(ls);
			FMetric microStatsDocWise = getMicroStatsDocWise();
			FMetric macroStatsDocWise = getMacroStatsDocWise();
			sb.append("  Recall (micro):    " + microStatsDocWise.getRecall()).append(ls);
			sb.append("  Precision (micro): " + microStatsDocWise.getPrecision()).append(ls);
			sb.append("  F-Score (micro):   " + microStatsDocWise.getF()).append(ls);
			sb.append("  Recall (macro):    " + macroStatsDocWise.getRecall()).append(ls);
			sb.append("  Precision (macro): " + macroStatsDocWise.getPrecision()).append(ls);
			sb.append("  F-Score (macro):   " + macroStatsDocWise.getF()).append(ls);
			sb.append(ls);
			sb.append(ls);

			if (getEvaluationMode() == EvaluationMode.MENTION) {
				sb.append("Document Level:").append(ls);
				FMetric microStatsMentionWise = getMicroStatsMentionWise();
				FMetric macroStatsMentionWise = getMacroStatsMentionWise();
				sb.append("  Recall (micro):    " + microStatsMentionWise.getRecall()).append(ls);
				sb.append("  Precision (micro): " + microStatsMentionWise.getPrecision()).append(ls);
				sb.append("  F-Score (micro):   " + microStatsMentionWise.getF()).append(ls);
				sb.append("  Recall (macro):    " + macroStatsMentionWise.getRecall()).append(ls);
				sb.append("  Precision (macro): " + macroStatsMentionWise.getPrecision()).append(ls);
				sb.append("  F-Score (macro):   " + macroStatsMentionWise.getF()).append(ls);
				sb.append(ls);
				sb.append(ls);
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

	public FMetric getMacroStatsDocWise() {
		double macroRecall = values().stream().mapToDouble(EntityEvaluationResult::getMicroRecallDocWise).average().orElse(0);
		double macroPrecision = values().stream().mapToDouble(EntityEvaluationResult::getMicroPrecisionDocWise).average().orElse(0);
		double macroF = values().stream().mapToDouble(EntityEvaluationResult::getMicroFMeasureDocWise).average().orElse(0);
		return new FMetric(macroRecall, macroPrecision, macroF);
	}

	public FMetric getMicroStatsDocWise() {
		int sumTps = values().stream().mapToInt(r -> r.getSumTpDocWise()).sum();
		int sumFps = values().stream().mapToInt(r -> r.getSumFpDocWise()).sum();
		int sumFns = values().stream().mapToInt(r -> r.getSumFnDocWise()).sum();
		return new FMetric(FMeasure.getRecall(sumTps, sumFps, sumFns), FMeasure.getPrecision(sumTps, sumFps, sumFns), FMeasure.getFMeasure(sumTps, sumFps, sumFns));
	}

	public FMetric getMacroStatsMentionWise() {
		double macroRecall = values().stream().mapToDouble(EntityEvaluationResult::getMicroRecallMentionWise).average().orElse(0);
		double macroPrecision = values().stream().mapToDouble(EntityEvaluationResult::getMicroPrecisionMentionWise).average().orElse(0);
		double macroF = values().stream().mapToDouble(EntityEvaluationResult::getMicroFMeasureMentionWise).average().orElse(0);
		return new FMetric(macroRecall, macroPrecision, macroF);
	}

	public FMetric getMicroStatsMentionWise() {
		int sumTps = values().stream().mapToInt(r -> r.getSumTpMentionWise()).sum();
		int sumFps = values().stream().mapToInt(r -> r.getSumFpMentionWise()).sum();
		int sumFns = values().stream().mapToInt(r -> r.getSumFnMentionWise()).sum();
		return new FMetric(FMeasure.getRecall(sumTps, sumFps, sumFns), FMeasure.getPrecision(sumTps, sumFps, sumFns), FMeasure.getFMeasure(sumTps, sumFps, sumFns));
	}
}
