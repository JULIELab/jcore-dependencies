package dragon.ml.seqmodel.model;

/**
 * <p>Complete model graph </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class CompleteModel extends AbstractModel {
    private int markovOrder;
    private int originalLabelNum;
    public CompleteModel(int labelNum) {
        this(labelNum, 1);
    }

    public CompleteModel(int labelNum, int markovOrder) {
        super(labelNum, "Complete");
        super.numLabels=computeLabelNum(labelNum,markovOrder);
        this.markovOrder =markovOrder;
        this.originalLabelNum =labelNum;
    }

    public int getOriginalLabelNum(){
        return originalLabelNum;
    }

    public int getMarkovOrder() {
        return markovOrder;
    }

    public int getLabel(int state) {
        return state;
    }

    public int getEdgeNum() {
        return numLabels * numLabels;
    }

    public int getStartStateNum() {
        return numLabels;
    }

    public int getEndStateNum() {
        return numLabels;
    }

    public int getStartState(int i) {
        if (i < getStartStateNum()) {
            return i;
        }
        return -1;
    }

    public int getEndState(int i) {
        if (i < getEndStateNum()) {
            return i;
        }
        return -1;
    }

    public boolean isEndState(int i) {
        return true;
    }

    public boolean isStartState(int i) {
        return true;
    }

    public EdgeIterator getEdgeIterator() {
        return new SingleEdgeIterator(getLabelNum());
    }

    private int computeLabelNum(int originalLabelNum, int markovOrder){
        int labelNum, i;

        labelNum=originalLabelNum;
        i=1;
        while(i<markovOrder){
            labelNum*=originalLabelNum;
            i++;
        }
        return labelNum;
    }

    private class SingleEdgeIterator implements EdgeIterator {
        private int labelNum;
        private Edge edge;
        private Edge edgeToReturn;

        public SingleEdgeIterator(int labelNum) {
            this.labelNum= labelNum;
            edge = new Edge();
            edgeToReturn = new Edge();
            start();
        }

        public void start() {
            edge.setStart(0);
            edge.setEnd(0);
        }

        public boolean hasNext() {
            return (edge.getStart()< labelNum);
        }

        public Edge next() {
            edgeToReturn.setStart(edge.getStart());
            edgeToReturn.setEnd(edge.getEnd());
            edge.setEnd(edge.getEnd()+1);
            if (edge.getEnd() ==labelNum) {
                edge.setEnd(0);
                edge.setStart(edge.getStart()+1);
            }
            return edgeToReturn;
        }

        public boolean nextIsOuter() {
            return true;
        }
    }
}
