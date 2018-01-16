package de.julielab.evaluation.entities;

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
		return sb.toString();
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
}
