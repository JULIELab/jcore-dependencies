package de.julielab.xml;

public class SofaVTDGraphNode extends JeDISVTDGraphNode {
    private String sofaID;

    public SofaVTDGraphNode(Integer oldXmiId) {
        super(oldXmiId);
    }

    public String getSofaID() {
        return sofaID;
    }

    @Override
    public String toString() {
        return "SofaVTDGraphNode{" +
                ", oldXmiId=" + oldXmiId +
                ", annotationModuleLabels=" + annotationModuleLabels +
                ", typeName='" + typeName + '\'' +
                "sofaID='" + sofaID + '\'' +
                '}';
    }

    public void setSofaID(String sofaID) {
        this.sofaID = sofaID;
    }
}
