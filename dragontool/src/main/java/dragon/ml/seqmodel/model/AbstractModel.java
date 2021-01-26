package dragon.ml.seqmodel.model;

import dragon.ml.seqmodel.data.DataSequence;

/**
 * <p>Abstract class for model graph </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public abstract class AbstractModel implements ModelGraph {
    protected int numLabels;
    protected String name;

    public AbstractModel(int labelNum, String name) {
        numLabels = labelNum;
        this.name = name;
    }

    public int getLabelNum() {
        return numLabels;
    }

    public int getStateNum() {
        return numLabels;
    }

    public int getOriginalLabelNum() {
        return numLabels;
    }

    public int getMarkovOrder() {
        return 1;
    }

    public boolean mapStateToLabel(DataSequence seq) {
        //do nothing unless nested model
        return true;
    }

    public boolean mapLabelToState(DataSequence seq) {
        //do nothing unless nested model
        return true;
    }

    public boolean mapLabelToState(DataSequence data, int len, int start) {
        //used for inner model only
        return true;
    }

    public void printGraph() {
        System.out.println("Numnodes = " + getStateNum() + " NumEdges " + getEdgeNum());
        EdgeIterator iter = getEdgeIterator();
        for (iter.start(); iter.hasNext(); ) {
            Edge edge = iter.next();
            System.out.println(edge.getStart() + "-->" + edge.getEnd());
        }
        System.out.print("Start states");
        for (int i = 0; i < getStartStateNum(); i++) {
            System.out.print(" " + getStartState(i));
        }
        System.out.println("");

        System.out.print("End states");
        for (int i = 0; i < getEndStateNum(); i++) {
            System.out.print(" " + getEndState(i));
        }
        System.out.println("");
    }

    public static ModelGraph getNewModelGraph(int numLabels, String modelSpecs) {
        int markovOrder;

        try {
            modelSpecs = modelSpecs.toLowerCase().trim();
            if (modelSpecs.equalsIgnoreCase("noEdge")) {
                return new NoEdgeModel(numLabels);
            } else if (modelSpecs.equalsIgnoreCase("naive") || modelSpecs.equalsIgnoreCase("semi-markov")) {
                return new CompleteModel(numLabels);
            } else if (modelSpecs.startsWith("naive") && modelSpecs.indexOf(',') < 0) {
                markovOrder=Integer.parseInt(modelSpecs.substring(modelSpecs.indexOf(' ')+1));
                return new CompleteModel(numLabels, markovOrder);
            } else {
                return new NestedModel(numLabels, modelSpecs);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
