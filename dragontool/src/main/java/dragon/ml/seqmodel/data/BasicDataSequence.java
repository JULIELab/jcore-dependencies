package dragon.ml.seqmodel.data;

import java.util.Vector;

/**
 * <p>Basic data structure for sequence data</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicDataSequence implements DataSequence{
    private Vector vector;
    private Dataset parent;

    public BasicDataSequence() {
       this(50);
    }

    public BasicDataSequence(int length) {
        parent=null;
        vector=new Vector(length,50);
    }

    public DataSequence copy(){
        BasicDataSequence seq;
        int i, len;

        len=length();
        seq=new BasicDataSequence(length());
        for(i=0;i<len;i++)
            seq.add(getToken(i).copy());
        return seq;
    }

    public Dataset getParent(){
        return parent;
    }

    public void setParent(Dataset parent){
        this.parent =parent;
    }

    public int length(){
        return vector.size();
    }

    public int getLabel(int pos){
        BasicToken token;
        int markovOrder, label, i;

        markovOrder=parent.getMarkovOrder();
        if(markovOrder<=1){
            token=(BasicToken)vector.get(pos);
            return token.getLabel();
        }
        else{
            if(pos>=markovOrder-1){
                label=0;
                for(i=pos+1-markovOrder;i<=pos;i++){
                    token=(BasicToken)vector.get(i);
                    label=label*parent.getOriginalLabelNum()+token.getLabel();
                }
                return label;
            }
            else
                return -1;
        }
    }

    public int getOriginalLabel(int pos){
        return ((BasicToken)vector.get(pos)).getLabel();
    }

    public BasicToken getToken(int pos){
        return (BasicToken)vector.get(pos);
    }

    public void setLabel(int pos, int label){
        BasicToken token;
        int markovOrder;

        token=(BasicToken)vector.get(pos);
        markovOrder=parent.getMarkovOrder();
        if(markovOrder>1){
            token.setLabel(label % parent.getOriginalLabelNum());
            //set the labels of the node before the current pos
            if(pos==markovOrder-1){
                label=label/parent.getOriginalLabelNum();
                while(pos>0){
                    pos=pos-1;
                    label=label/parent.getOriginalLabelNum();
                    token=(BasicToken)vector.get(pos);
                    token.setLabel(label%parent.getOriginalLabelNum());
                }
            }
        }
        else
            token.setLabel(label);
    }

    public void add(BasicToken token){
        vector.add(token);
    }

    public int getSegmentEnd(int segmentStart){
        int curPos, len;

        len=length();
        curPos=segmentStart+1;
        while(curPos<len){
            if(getToken(curPos).isSegmentStart())
                break;
            else
                curPos++;
        }
        return curPos-1;
    }

    public void setSegment(int segmentStart, int segmentEnd, int label){
        BasicToken curToken;
        int i;

        if(parent.getMarkovOrder()>1){
            System.out.println("Only first-order markov allowed for segment sequencing!");
            return;
        }

        curToken=getToken(segmentStart);
        curToken.setSegmentMarker(true);
        curToken.setLabel(label);
        for(i=segmentStart+1;i<=segmentEnd;i++){
            curToken=getToken(i);
            curToken.setSegmentMarker(false);
            curToken.setLabel(label);
        }
        if(segmentEnd<length()-1)
            getToken(segmentEnd+1).setSegmentMarker(true);
    }
}
