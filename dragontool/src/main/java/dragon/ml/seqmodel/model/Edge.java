package dragon.ml.seqmodel.model;

/**
 * <p>Edge of model graph </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class Edge implements Comparable{
    private int start;
    private int end;

    public Edge() {
        start=-1;
        end=-1;
    }

    public Edge(int s, int e) {
        start = s;
        end = e;
    }

    public int getStart(){
        return start;
    }

    public void setStart(int start){
        this.start =start;
    }

    public int getEnd(){
        return end;
    }

    public void setEnd(int end){
        this.end =end;
    }

    String tostring() {
        return (start + " -> " + end);
    }

    public int compareTo(Object o) {
        Edge e = (Edge) o;
        return ( (start != e.start) ? (start - e.start) : (end - e.end));
    }
};
