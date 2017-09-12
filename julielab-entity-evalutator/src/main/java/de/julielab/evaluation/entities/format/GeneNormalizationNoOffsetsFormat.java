package de.julielab.evaluation.entities.format;

import static de.julielab.evaluation.entities.format.EvaluationDataColumn.CONFIDENCE;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.DOC_ID;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.ENTITY_ID;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.ENTITY_STRING;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.TAGGER;

public class GeneNormalizationNoOffsetsFormat extends EvaluationDataFormat {
	public GeneNormalizationNoOffsetsFormat() {
		super(DOC_ID, ENTITY_ID, ENTITY_STRING, TAGGER, CONFIDENCE);
	}
}
