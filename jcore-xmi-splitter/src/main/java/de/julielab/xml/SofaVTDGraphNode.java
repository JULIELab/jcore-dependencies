package de.julielab.xml;

public class SofaVTDGraphNode extends JeDISVTDGraphNode {
    private String sofaID;

    public SofaVTDGraphNode(Integer oldXmiId) {
        super(oldXmiId);
    }

    public String getSofaID() {
        return sofaID;
    }

    public void setSofaID(String sofaID) {
        this.sofaID = sofaID;
    }
}
