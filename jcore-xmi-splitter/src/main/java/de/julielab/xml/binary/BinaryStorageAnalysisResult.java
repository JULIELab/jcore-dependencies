package de.julielab.xml.binary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BinaryStorageAnalysisResult {
    private Set<String> valuesToMap;
    private Map<String, Boolean> featuresToMap;
    private Map<String, Integer> missingItemsMapping;


    public BinaryStorageAnalysisResult(Set<String> valuesToMap, Map<String, Boolean> featuresToMap, int nextAvailableId) {
        this.valuesToMap = valuesToMap;
        this.featuresToMap = featuresToMap;
        missingItemsMapping = new HashMap<>();
        for (String value : valuesToMap)
            missingItemsMapping.put(value, nextAvailableId++);
    }

    /**
     * The 'values' don't just mean attribute values but all strings that should be replaced by an integer ID.
     *
     * @return The strings to include in the mapping.
     */
    public Set<String> getMissingValuesToMap() {
        return valuesToMap;
    }


    public Map<String, Boolean> getMissingFeaturesToMap() {
        return featuresToMap;
    }

    public Map<String, Integer> getMissingItemsMapping() {
        return missingItemsMapping;
    }
}
