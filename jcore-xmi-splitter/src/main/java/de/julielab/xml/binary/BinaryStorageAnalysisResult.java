package de.julielab.xml.binary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinaryStorageAnalysisResult {
    private List<String> valuesToMap;
    private Map<String, Boolean> featuresToMap;
    private int nextAvailableId;
    private Map<String, Integer> missingItemsMapping;


    public BinaryStorageAnalysisResult(List<String> valuesToMap, Map<String, Boolean> featuresToMap, int nextAvailableId) {
        this.valuesToMap = valuesToMap;
        this.featuresToMap = featuresToMap;
        this.nextAvailableId = nextAvailableId;
        missingItemsMapping = new HashMap<>();
        for (String value : valuesToMap)
            missingItemsMapping.put(value, nextAvailableId++);
    }

    /**
     * The 'values' don't just mean attribute values but all strings that should be replaced by an integer ID.
     *
     * @return The strings to include in the mapping.
     */
    public List<String> getMissingValuesToMap() {
        return valuesToMap;
    }


    public Map<String, Boolean> getMissingFeaturesToMap() {
        return featuresToMap;
    }

    public Map<String, Integer> getMissingItemsMapping() {
        return missingItemsMapping;
    }
}
