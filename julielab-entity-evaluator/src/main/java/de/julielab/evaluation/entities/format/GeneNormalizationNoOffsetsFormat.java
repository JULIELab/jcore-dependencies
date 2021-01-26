package de.julielab.evaluation.entities.format;

import static de.julielab.evaluation.entities.format.EvaluationDataColumn.*;

public class GeneNormalizationNoOffsetsFormat extends EvaluationDataFormat {
	public GeneNormalizationNoOffsetsFormat() {
		super(DOC_ID, ENTITY_ID, ENTITY_STRING, TAGGER, CONFIDENCE);
	}
}
