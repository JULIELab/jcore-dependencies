package de.julielab.xml;

import java.util.List;
import java.util.Set;

public class BinaryStorageAnalysisResult {
    private List<String> valuesToMap;
    private Set<String> featuresToMap;

    public BinaryStorageAnalysisResult(List<String> valuesToMap, Set<String> featuresToMap) {
        this.valuesToMap = valuesToMap;
        this.featuresToMap = featuresToMap;
    }

    /**
     * The 'values' don't just mean attribute values but all strings that should be replaced by an integer ID.
     * @return The strings to include in the mapping.

     */
    public List<String> getValuesToMap() {
        return valuesToMap;
    }

    public void setValuesToMap(List<String> valuesToMap) {
        this.valuesToMap = valuesToMap;
    }

    public Set<String> getFeaturesToMap() {
        return featuresToMap;
    }

    public void setFeaturesToMap(Set<String> featuresToMap) {
        this.featuresToMap = featuresToMap;
    }
}
