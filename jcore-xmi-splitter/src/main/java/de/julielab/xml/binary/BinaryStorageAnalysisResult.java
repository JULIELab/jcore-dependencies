package de.julielab.xml.binary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BinaryStorageAnalysisResult {
    private List<String> valuesToMap;
    private Set<String> featuresToMap;
    private int nextAvailableId;

    public BinaryStorageAnalysisResult(List<String> valuesToMap, Set<String> featuresToMap, int nextAvailableId) {
        this.valuesToMap = valuesToMap;
        this.featuresToMap = featuresToMap;
        this.nextAvailableId = nextAvailableId;
    }

    /**
     * The 'values' don't just mean attribute values but all strings that should be replaced by an integer ID.
     *
     * @return The strings to include in the mapping.
     */
    public List<String> getValuesToMap() {
        return valuesToMap;
    }


    public Set<String> getFeaturesToMap() {
        return featuresToMap;
    }

    public Map<String, Integer> getMissingItemsMapping() {
        Map<String, Integer> missingMapping = new HashMap<>();
        for (String value : valuesToMap)
            missingMapping.put(value, nextAvailableId++);
        return missingMapping;
    }
}
