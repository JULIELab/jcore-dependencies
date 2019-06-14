package de.julielab.evaluation.entities.format;

import static de.julielab.evaluation.entities.format.EvaluationDataColumn.*;

public class GeneNormalizationFormat extends EvaluationDataFormat {
	public GeneNormalizationFormat() {
		super(DOC_ID, ENTITY_ID, BEGIN, END, ENTITY_STRING, TAGGER, CONFIDENCE);
	}
}
