package de.julielab.xml;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XmiSplitterResult {

    /**
     * Keys: Fully qualified Java-style type names. For the base document data, the document table name (given as a
     * parameter to the splitter).
     * Values: XML data of the respective annotation (and document data) modules
     */
    public LinkedHashMap<String, ByteArrayOutputStream> xmiData;
    public int maxXmiId;
    public Map<String, String> namespaces;
    public Map<Integer, String> currentSofaIdMap;
    public List<JeDISVTDGraphNode> jedisNodesInAnnotationModules;

    public XmiSplitterResult(LinkedHashMap<String, ByteArrayOutputStream> xmiData, Integer nextId,
                             Map<String, String> namespaces, Map<Integer, String> currentSofaIdMap, List<JeDISVTDGraphNode> jedisNodesInAnnotationModules) {
        this.xmiData = xmiData;
        maxXmiId = nextId;
        this.namespaces = namespaces;
        this.currentSofaIdMap = currentSofaIdMap;
        this.jedisNodesInAnnotationModules = jedisNodesInAnnotationModules;
    }
}
