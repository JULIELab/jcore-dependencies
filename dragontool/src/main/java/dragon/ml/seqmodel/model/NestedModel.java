package dragon.ml.seqmodel.model;

import dragon.ml.seqmodel.data.DataSequence;

import java.util.StringTokenizer;

/**
 * <p>Nested model graph </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class NestedModel extends AbstractModel {
    private int _numStates;
    private int _numEdges;
    private int nodeOffsets[]; // the number of states in the labels before this.
    private ModelGraph inner[];
    private ModelGraph outer;
    private int startStates[];
    private int endStates[];

    public static void main(String args[]) {
        try {
            System.out.println(args[0]);
            System.out.println(args[1]);
            AbstractModel model = new NestedModel(Integer.parseInt(args[0]), args[1]);
            System.out.println(model.getStateNum());
            System.out.println(model.getEdgeNum());
            System.out.println(model.getStartStateNum());
            System.out.println(model.getEndStateNum());
            EdgeIterator edgeIter = model.getEdgeIterator();
            for (int edgeNum = 0; edgeIter.hasNext(); edgeNum++) {
                boolean edgeIsOuter = edgeIter.nextIsOuter();
                Edge e = edgeIter.next();
                System.out.println(e.getStart() + "("+ model.getLabel(e.getStart()) + ")" + " -> " + e.getEnd() + ":" + edgeIsOuter+ ";");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public NestedModel(int labelNum, String specs) throws Exception {
        super(labelNum, "Nested");

        EdgeIterator outerIter;
        StringTokenizer start;
        String commonStruct, thisStruct;
        int numStart, numEnd, index, i, j;

        nodeOffsets = new int[numLabels];
        inner = new AbstractModel[numLabels];

        start = new StringTokenizer(specs, ",");
        outer =getNewBaseModel(numLabels, (String) start.nextToken());
        commonStruct = null;
        for (i = 0; i < numLabels; i++) {
            thisStruct = commonStruct;
            if (thisStruct == null) {
                thisStruct = start.nextToken();
                if (thisStruct.endsWith("*")) {
                    thisStruct = thisStruct.substring(0, thisStruct.length() - 1);
                    commonStruct = thisStruct;
                }
            }
            inner[i] = new GenericModel(thisStruct, i);
        }

        _numEdges = 0;
        _numStates = 0;
        for (int l = 0; l < numLabels; l++) {
            nodeOffsets[l] += _numStates;
            _numStates += inner[l].getStateNum();
            _numEdges += inner[l].getEdgeNum();
        }

        outerIter = outer.getEdgeIterator();
        while (outerIter.hasNext()) {
            Edge e = outerIter.next();
            _numEdges += inner[e.getEnd()].getStartStateNum() * inner[e.getStart()].getEndStateNum();
        }

        numStart = 0;
        for (i = 0; i < outer.getStartStateNum(); i++) {
            numStart += inner[outer.getStartState(i)].getStartStateNum();
        }
        startStates = new int[numStart];
        index = 0;
        for (i = 0; i < outer.getStartStateNum(); i++) {
            for (j = 0; j < inner[outer.getStartState(i)].getStartStateNum(); j++) {
                startStates[index++] = inner[outer.getStartState(i)].getStartState(j) + nodeOffsets[outer.getStartState(i)];
            }
        }

        numEnd = 0;
        for (i = 0; i < outer.getEndStateNum(); i++) {
            numEnd += inner[outer.getEndState(i)].getEndStateNum();
        }
        endStates = new int[numEnd];
        index = 0;
        for (i = 0; i < outer.getEndStateNum(); i++) {
            for (j = 0; j < inner[outer.getEndState(i)].getEndStateNum(); j++) {
                endStates[index++] = inner[outer.getEndState(i)].getEndState(j) + nodeOffsets[outer.getEndState(i)];
            }
        }
    }

    public EdgeIterator getEdgeIterator() {
        return new NestedEdgeIterator(this);
    }

    public EdgeIterator innerEdgeIterator() {
        return new NestedEdgeIterator(this,false);
    }

    public int getStateNum() {
        return _numStates;
    }

    public int getEdgeNum() {
        return _numEdges;
    }

    public int getLabel(int stateNum) {
        // TODO -- convert to binary scan.
        for (int i = 0; i < nodeOffsets.length; i++) {
            if (stateNum < nodeOffsets[i])
                return i - 1;
        }
        return nodeOffsets.length - 1;
    }

    public int getStartStateNum() {
        return startStates.length;
    }

    public int getEndStateNum() {
        return endStates.length;
    }

    public int getStartState(int i) {
        return ( (i < getStartStateNum()) ? startStates[i] : -1);
    }

    public int getEndState(int i) {
        return ( (i < getEndStateNum()) ? endStates[i] : -1); // endStates[i];
    }

    public boolean isEndState(int i) {
        // TODO -- convert this to binary search
        for (int k = 0; k < endStates.length; k++)
            if (endStates[k] == i)
                return true;
        return false;
    }

    public boolean isStartState(int i) {
        // TODO -- convert this to binary search
        for (int k = 0; k < startStates.length; k++)
            if (startStates[k] == i)
                return true;
        return false;
    }

    public boolean mapStateToLabel(DataSequence dataSeq) {
        int dataLen, segStart, segEnd;

        dataLen = dataSeq.length();
        if (dataLen == 0)
            return true;

        for (segStart = 0, segEnd = 0; segStart < dataLen; segStart = segEnd + 1) {
            for (segEnd = segStart; segEnd < dataLen; segEnd++) {
                if (getLabel(dataSeq.getLabel(segStart)) != getLabel(dataSeq.getLabel(segEnd))) {
                    segEnd -= 1;
                    System.out.println("WARNING: label ending in a state not marked as a End-state");
                    break;
                }
                if (isEndState(dataSeq.getLabel(segEnd))) {
                    break;
                }
            }
            if (segEnd == dataLen) {
                System.out.println("WARNING: End state not found until the last position");
                System.out.println(dataSeq);
                segEnd = dataLen - 1;
            }
            dataSeq.setSegment(segStart, segEnd, getLabel(dataSeq.getLabel(segStart)));
        }
        return true;
    }


    public boolean mapLabelToState(DataSequence data){
        int lstart, lend, label, k;

        if (data.length() == 0)
            return true;
        for (lstart = 0; lstart < data.length(); ) {
            lend = data.getSegmentEnd(lstart) + 1;
            if (lend == 0)
                return false;
            label = data.getLabel(lstart);
            inner[label].mapLabelToState(data, lend - lstart, lstart);
            for (k = lstart; k < lend; k++) {
                data.setLabel(k, nodeOffsets[label] + data.getLabel(k));
            }
            lstart = lend;
        }
        return true;
    }


    private ModelGraph getNewBaseModel(int numLabels, String modelSpecs) throws Exception {
        if (modelSpecs.equalsIgnoreCase("naive") || (modelSpecs.equalsIgnoreCase("semi-markov"))) {
            return new CompleteModel(numLabels);
        } else if (modelSpecs.equalsIgnoreCase("noEdge")) {
            return new NoEdgeModel(numLabels);
        }
        throw new Exception("Base model can be one of {naive, noEdge, semi-Markov}");
    }

    private class NestedEdgeIterator implements EdgeIterator {
        private NestedModel model;
        private int label;
        private Edge edge;
        private EdgeIterator edgeIter[], outerEdgeIter;
        private Edge outerEdge;
        private boolean outerEdgesSent;
        private int index1, index2;
        private boolean sendOuter;

        public NestedEdgeIterator(NestedModel m) {
            this(m, true);
        }

        public NestedEdgeIterator(NestedModel m, boolean sendOuter) {
            model = m;
            edge = new Edge();
            edgeIter = new EdgeIterator[model.numLabels];
            for (int l = 0; l < model.numLabels; l++) {
                edgeIter[l] = model.inner[l].getEdgeIterator();
            }
            outerEdgeIter = model.outer.getEdgeIterator();
            this.sendOuter = sendOuter;
            start();
        }

        public void start() {
            label = 0;
            for (int l = 0; l < model.numLabels; l++) {
                edgeIter[l].start();
            }
            outerEdgeIter.start();
            outerEdge = outerEdgeIter.next();

            //check for the null edge
            if ( (outerEdge == null) || !sendOuter)
                outerEdgesSent = true;
            else
                outerEdgesSent = false;
            index1 = index2 = 0;
        }

        public boolean hasNext() {
            return (label < model.numLabels) || !outerEdgesSent;
        }

        public Edge nextOuterEdge() {
            edge.setStart(model.inner[outerEdge.getStart()].getEndState(index1) + model.nodeOffsets[outerEdge.getStart()]);
            edge.setEnd(model.inner[outerEdge.getEnd()].getStartState(index2) + model.nodeOffsets[outerEdge.getEnd()]);
            index2++;
            if (index2 == model.inner[outerEdge.getEnd()].getStartStateNum()) {
                index2 = 0;
                index1++;
                if (index1 == model.inner[outerEdge.getStart()].getEndStateNum()) {
                    if (outerEdgeIter.hasNext()) {
                        outerEdge = outerEdgeIter.next();
                        index1 = index2 = 0;
                    } else {
                        outerEdgesSent = true;
                    }
                }
            }
            return edge;
        }

        public Edge nextInnerEdge() {
            Edge edgeToRet = edgeIter[label].next();
            edge.setStart(edgeToRet.getStart());
            edge.setEnd(edgeToRet.getEnd());
            edge.setStart(edge.getStart() + model.nodeOffsets[label]);
            edge.setEnd(edge.getEnd() + model.nodeOffsets[label]);
            if (!edgeIter[label].hasNext())
                label++;
            return edge;
        }

        public Edge next() {
            if (!nextIsOuter()) {
                return nextInnerEdge();
            } else {
                return nextOuterEdge();
            }
        }

        public boolean nextIsOuter() {
            return (label >= model.numLabels);
        }
    }
}

