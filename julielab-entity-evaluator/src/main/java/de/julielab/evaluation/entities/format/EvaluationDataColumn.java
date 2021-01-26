package de.julielab.evaluation.entities.format;

import de.julielab.evaluation.entities.EvaluationDataEntry;

import java.util.function.Consumer;

public enum EvaluationDataColumn {
	DOC_ID {
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			set(e, f, dataRecord, (String x) -> e.setDocId(x));
		}
	},
	SENTENCE_ID {
		@Override
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			// Nothing, we currently don't use the sentence ID within the Entity
			// Evaluator. It is only used by the BANNER tagger.
		}
	},
	ENTITY_ID {
		@Override
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			set(e, f, dataRecord, (String x) -> e.setEntityId(x));

		}
	},
	BEGIN {
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			set(e, f, dataRecord, (String x) -> e.setBegin(Integer.parseInt(x)));
		}
	},
	END {
		@Override
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			set(e, f, dataRecord, (String x) -> e.setEnd(Integer.parseInt(x)));

		}
	},
	ENTITY_STRING {
		@Override
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			set(e, f, dataRecord, (String x) -> e.setEntityString(x));

		}
	},
	TAGGER {
		@Override
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			set(e, f, dataRecord, (String x) -> e.setRecognitionSystem(x));

		}
	},
	CONFIDENCE {
		@Override
		public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord) {
			set(e, f, dataRecord, (String x) -> e.setConfidence(x));
		}
	};
	public abstract void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord);

	/**
	 * Sets the value corresponding to the column on which the method is invoked
	 * from fields to e.
	 * 
	 * @param e
	 *            The evaluation data entry to set a value to.
	 * @param f
	 *            The evaluation data format that determines the set of columns
	 *            in the input format and thus, dataRecord.
	 * @param dataRecord
	 *            The input fields that should be read into evaluation data
	 *            entry instances.
	 */
	public void set(EvaluationDataEntry e, EvaluationDataFormat f, String[] dataRecord, Consumer<String> setter) {
		int columnIndex = f.get(this);
		if (columnIndex < dataRecord.length)
			setter.accept(dataRecord[columnIndex]);
	}
}
