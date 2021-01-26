package dragon.ml.seqmodel.model;

import dragon.ml.seqmodel.data.DataSequence;

import java.util.StringTokenizer;

/**
 * <p>Generic model graph </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class GenericModel extends AbstractModel {
    private int _numStates;
    private Edge _edges[];  // edges have to be sorted by their starting node id.
    private int edgeStart[]; // the index in the edges array where edges out of node i start.
    private int startStates[];
    private int endStates[];
    private int myLabel;

    public GenericModel(String spec, int thisLabel) throws Exception {
        super(1,spec);
        myLabel = thisLabel;

        if (spec.endsWith("-chain") || spec.endsWith("-long")) {
            StringTokenizer tok = new StringTokenizer(spec,"-");
            int len = Integer.parseInt(tok.nextToken());
            _numStates = len;
            startStates = new int[1];
            startStates[0] = 0;
            edgeStart = new int[_numStates];
            if (len == 1) {
                _edges = new Edge[1];
                _edges[0] = new Edge(0,0);
                endStates = new int[1];
                endStates[0] = 0;
                edgeStart[0] = 0;
            } else {
                _edges = new Edge[2*(len-1)];
                for (int i = 0; i < len-1; i++) {
                    _edges[2*i] = new Edge(i,i+1);
                    _edges[2*i+1] = new Edge(i,len-1);
                    edgeStart[i] = 2*i;
                }
                _edges[_edges.length-1] = new Edge(len-2,len-2);
                endStates = new int[2];
                endStates[0] = 0; // to allow one word entities.
                endStates[1] = len-1;
            }
        }
        else if (spec.endsWith("parallel")) {
            StringTokenizer tok = new StringTokenizer(spec,"-");
            int len = Integer.parseInt(tok.nextToken());
            _numStates = len*(len+1)/2;
            _edges = new Edge[len*(len-1)/2 + 1];
            edgeStart = new int[_numStates];
            startStates = new int[len];
            endStates = new int[len];
            int node = 0;
            int e = 0;
            for (int i = 0; i < len; i++) {
                node += i;
                for (int j = 0; j < i; j++) {
                    _edges[e++] = new Edge(node+j,node+j+1);
                    edgeStart[node+j] = e-1;
                }
                startStates[i] = node;
                endStates[i] = node + i;
            }
            node += len;
            _edges[e++] = new Edge(_numStates-2, _numStates-2);
        }
        else if (spec.equals("boundary")) {
            // this implements a model where each label is either of a
            // Unique word (state 0) or broken into a Start state
            // (state 1) with a single token, Continuation state
            // (state 2) with multiple tokens (only state with
            // self-loop) and end state (state 3) with a single token.
            // The number of states is thus 4, and number of edges 4
            _numStates = 4;
            _edges = new Edge[4];
            _edges[0] = new Edge(1,2);
            _edges[1] = new Edge(1,3);
            _edges[2] = new Edge(2,2);
            _edges[3] = new Edge(2,3);
            startStates = new int[2];
            startStates[0] = 0;
            startStates[1] = 1;
            endStates = new int[2];
            endStates[0] = 0;
            endStates[1] = 3;
            edgeStart = new int[_numStates];
            edgeStart[0] = 4;
            edgeStart[1] = 0;
            edgeStart[2] = 2;
            edgeStart[3] = 4;
        }
        else {
            throw new Exception("Unknown graph type: " + spec);
        }
    }

    public int getLabel(int s) {
        return (myLabel == -1)?s:myLabel;
    }

    public GenericModel(int numNodes, int numEdges) throws Exception {
        super(numNodes,"");
        _numStates = numNodes;
        _edges = new Edge[numEdges];
    }

    public int getStateNum() {
        return _numStates;
    }

    public int getEdgeNum() {
        return _edges.length;
    }

    public int getStartStateNum() {
        return startStates.length;
    }

    public int getStartState(int i) {
        return (i < getStartStateNum())?startStates[i]:-1;
    }

    public int getEndStateNum() {
        return endStates.length;
    }

    public int getEndState(int i) {
        return (i < getEndStateNum())?endStates[i]:-1;
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

    public EdgeIterator getEdgeIterator() {
        return new GenericEdgeIterator(_edges);
    }

    public boolean mapLabelToState(DataSequence data, int len, int start){
        for (int i = 0; i < getStartStateNum(); i++) {
            if (pathToEnd(data,getStartState(i),len-1,start+1)) {
                data.setLabel(start,getStartState(i));
                return true;
            }
        }
        return false;
    }

    private boolean pathToEnd(DataSequence data, int s, int lenLeft, int start) {
        int e, child;

        if (lenLeft == 0) {
            return isEndState(s);
        }
        for (e = edgeStart[s]; (e < getEdgeNum()) && (_edges[e].getStart() == s); e++) {
            child = _edges[e].getEnd();
            if (pathToEnd(data,child,lenLeft-1,start+1)) {
                data.setLabel(start,child);
                return true;
            }
        }
        return false;
    }

    private class GenericEdgeIterator implements EdgeIterator {
        private int edgeNum;
        private Edge edges[];

        public GenericEdgeIterator(Edge[] e) {
            edges = e;
            start();
        }

        public void start() {
            edgeNum = 0;
        }

        public boolean hasNext() {
            return (edgeNum < edges.length);
        }

        public Edge next() {
            edgeNum++;
            return edges[edgeNum-1];
        }

        public boolean nextIsOuter() {
            return true;
        }
    }
}

