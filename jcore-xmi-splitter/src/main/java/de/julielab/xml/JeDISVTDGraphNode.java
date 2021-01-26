package de.julielab.xml;

import de.julielab.xml.binary.AttributeParser;
import de.julielab.xml.binary.XmlStartTag;

import java.util.*;

public class JeDISVTDGraphNode {

    public static final JeDISVTDGraphNode CAS_NULL = new JeDISVTDGraphNode(0, 0);
    protected long elementFragment;
    protected Integer oldXmiId;
    protected Integer newXmiId;
    protected Set<String> annotationModuleLabels;
    protected int sofaXmiId;
    protected String typeName;
    protected List<JeDISVTDGraphNode> predecessors;
    private Map<String, List<Integer>> referencedXmiIds;
    private int byteOffset;
    private int byteLength;
    private String moduleXmlData;
    private XmlStartTag byteSegmentedStartTag;

    /**
     * For {@link #CAS_NULL}. So both IDs will be 0.
     * @param oldXmiId The original XMI ID
     * @param newXmiId The new XMI ID
     */
    private JeDISVTDGraphNode(Integer oldXmiId, Integer newXmiId) {
        this();
        this.oldXmiId = oldXmiId;
        this.newXmiId = newXmiId;
    }

    public JeDISVTDGraphNode(Integer oldXmiId) {
        this();
        this.oldXmiId = oldXmiId;
    }

    public JeDISVTDGraphNode() {
        annotationModuleLabels = Collections.emptySet();
        predecessors = Collections.emptyList();
        referencedXmiIds = Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "JeDISVTDGraphNode{" +
                "oldXmiId=" + oldXmiId +
                ", annotationModuleLabels=" + annotationModuleLabels +
                ", typeName='" + typeName + '\'' +
                '}';
    }

    public long getElementFragment() {
        return elementFragment;
    }

    public void setElementFragment(long elementFragment) {
        this.elementFragment = elementFragment;
        byteOffset = (int) elementFragment;
        byteLength = (int) (elementFragment >> 32);
    }

    public int getByteLength() {
        return byteLength;

    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public Integer getOldXmiId() {
        return oldXmiId;
    }

    public void setOldXmiId(Integer oldXmiId) {
        this.oldXmiId = oldXmiId;
    }

    public Integer getNewXmiId() {
        return newXmiId;
    }

    public void setNewXmiId(Integer newXmiId) {
        this.newXmiId = newXmiId;
    }

    public Set<String> getAnnotationModuleLabels() {
        return annotationModuleLabels;
    }

    public void setAnnotationModuleLabels(Set<String> annotationModuleLabels) {
        this.annotationModuleLabels = annotationModuleLabels;
    }

    public int getSofaXmiId() {
        return sofaXmiId;
    }

    public void setSofaXmiId(int sofaXmiId) {
        this.sofaXmiId = sofaXmiId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<JeDISVTDGraphNode> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(List<JeDISVTDGraphNode> predecessors) {
        this.predecessors = predecessors;
    }

    public void addPredecessor(JeDISVTDGraphNode referencingNode) {
        if (predecessors.isEmpty())
            predecessors = new ArrayList<>();
        predecessors.add(referencingNode);
    }

    public void addAnnotationModuleLabel(String typeName) {
        if (annotationModuleLabels.isEmpty())
            annotationModuleLabels = new HashSet<>();
        annotationModuleLabels.add(typeName);
    }

    public void addAnnotationModuleLabels(Set<String> labels) {
        labels.forEach(this::addAnnotationModuleLabel);
    }

    public Map<String, List<Integer>> getReferencedXmiIds() {
        return referencedXmiIds;
    }

    public void setReferencedXmiIds(Map<String, List<Integer>> referencedXmiIds) {
        this.referencedXmiIds = referencedXmiIds;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    /**
     * <p>
     * Returns the final result of the splitting process for this node. This includes an updated XMI ID, Sofa ID and reference XMI IDs.
     * </p>
     *
     * @return The final XML data to be stored in the annotation module for this node.
     */
    public String getModuleXmlData() {
        return moduleXmlData;
    }

    public void setModuleData(String moduleXmlData) {
        this.moduleXmlData = moduleXmlData;
    }

    public XmlStartTag getByteSegmentedStartTag(byte[] elementData) {
        if (byteSegmentedStartTag == null) {
            final AttributeParser attributeParser = new AttributeParser();
            byteSegmentedStartTag = attributeParser.parse(elementData);
        }
        return byteSegmentedStartTag;
    }

    public void setByteSegmentedStartTag(XmlStartTag byteSegmentedStartTag) {
        this.byteSegmentedStartTag = byteSegmentedStartTag;
    }
}
