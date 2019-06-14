package dragon.ml.seqmodel.data;

import java.util.Vector;

/**
 * <p>Basic data structure of a set of sequence data</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicDataset implements Dataset{
    private Vector vector;
    private int originalLabelNum, labelNum, markovOrder;
    private int curPos;

    public BasicDataset(int originalLabelNum, int markovOrder) {
        vector=new Vector();
        this.originalLabelNum=originalLabelNum;
        this.markovOrder=markovOrder;
        labelNum=1;
        for(int i=0;i<markovOrder; i++) labelNum*=originalLabelNum;
    }

    public Dataset copy(){
        BasicDataset dataset;

        dataset=new BasicDataset(originalLabelNum,markovOrder);
        startScan();
        while(hasNext()){
            dataset.add(next().copy());
        }
        return dataset;
    }

    public int size(){
        return vector.size();
    }

    public void startScan(){
        curPos=0;
    }

    public boolean hasNext(){
        return curPos<vector.size();
    }

    public DataSequence next(){
        curPos++;
        return (DataSequence)vector.get(curPos-1);
    }

    public boolean add(DataSequence seq){
        seq.setParent(this);
        vector.add(seq);
        return true;
    }

    public int getLabelNum(){
        return labelNum;
    }

    public int getOriginalLabelNum() {
        return originalLabelNum;
    }

    public int getMarkovOrder() {
        return markovOrder;
    }
}
