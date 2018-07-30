package de.julielab.xml;

import java.util.*;

public class JeDISVTDGraphNode {
    protected int vtdIndex;
    protected long elementFragment;
    protected Integer oldXmiId;
    protected Integer newXmiId;
    protected Set<String> annotationModuleLabels;
    protected int sofaXmiId;
    protected String typeName;
    protected List<JeDISVTDGraphNode> predecessors;
    private Map<String, List<Integer>> referencedXmiIds;


    public JeDISVTDGraphNode(Integer oldXmiId) {
        this();
        this.oldXmiId = oldXmiId;
    }

    public JeDISVTDGraphNode() {
        annotationModuleLabels = Collections.emptySet();
        predecessors = Collections.emptyList();
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
    }

    public int getByteOffset() {
        return (int) elementFragment;
    }

    public int getByteLength() {
        return (int) (elementFragment >> 32);
    }

    public int getVtdIndex() {
        return vtdIndex;
    }

    public void setVtdIndex(int vtdIndex) {
        this.vtdIndex = vtdIndex;
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
}
