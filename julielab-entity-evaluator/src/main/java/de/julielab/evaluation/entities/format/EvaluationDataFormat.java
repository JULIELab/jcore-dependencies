package de.julielab.evaluation.entities.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EvaluationDataFormat  {
	private Map<EvaluationDataColumn, Integer> colMap = new HashMap<>();
	
	public EvaluationDataFormat(EvaluationDataColumn...columns) {
		colMap = new HashMap<>();
		for (int i = 0; i < columns.length; i++) {
			EvaluationDataColumn col = columns[i];
			if (colMap.put(col, i) != null)
				throw new IllegalArgumentException("Column type " + col + " was specified multiple times.");
		}
	}
	
	public int get(EvaluationDataColumn col) {
		return colMap.get(col);
	}

	public Set<EvaluationDataColumn> getColumns() {
		return colMap.keySet();
	}
}
