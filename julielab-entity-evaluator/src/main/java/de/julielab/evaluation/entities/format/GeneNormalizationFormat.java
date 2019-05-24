package de.julielab.evaluation.entities.format;

import static de.julielab.evaluation.entities.format.EvaluationDataColumn.BEGIN;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.CONFIDENCE;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.DOC_ID;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.END;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.ENTITY_ID;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.ENTITY_STRING;
import static de.julielab.evaluation.entities.format.EvaluationDataColumn.TAGGER;

public class GeneNormalizationFormat extends EvaluationDataFormat {
	public GeneNormalizationFormat() {
		super(DOC_ID, ENTITY_ID, BEGIN, END, ENTITY_STRING, TAGGER, CONFIDENCE);
	}
}
