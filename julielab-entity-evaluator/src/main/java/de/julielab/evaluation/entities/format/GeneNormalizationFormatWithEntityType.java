package de.julielab.evaluation.entities.format;

import static de.julielab.evaluation.entities.format.EvaluationDataColumn.*;

public class GeneNormalizationFormatWithEntityType extends EvaluationDataFormat {
	public GeneNormalizationFormatWithEntityType() {
		super(DOC_ID, ENTITY_ID, BEGIN, END, ENTITY_STRING, TYPE);
	}
}
