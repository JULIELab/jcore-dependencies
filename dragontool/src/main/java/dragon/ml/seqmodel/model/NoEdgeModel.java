package dragon.ml.seqmodel.model;

/**
 * <p>Model graph without edge </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class NoEdgeModel extends AbstractModel {
    private EmptyEdgeIter emptyIter;

    public NoEdgeModel(int nlabels) {
        super(nlabels, "NoEdge");
        emptyIter = new EmptyEdgeIter();
    }

    public int getEdgeNum() {
        return 0;
    }

    public int getLabel(int state) {
        return state;
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
        return emptyIter;
    }

    private class EmptyEdgeIter implements EdgeIterator {
        public void start() {}

        public boolean hasNext() {
            return false;
        }

        public Edge next() {
            return null;
        }

        public boolean nextIsOuter() {
            return false;
        }
    }
};
