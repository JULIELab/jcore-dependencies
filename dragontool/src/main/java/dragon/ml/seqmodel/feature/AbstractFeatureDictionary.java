package dragon.ml.seqmodel.feature;

/**
 * <p>Abstract class for feature dictionary </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractFeatureDictionary  {
    protected int stateNum;
    protected int cntsArray[][];
    protected int cntsOverAllState[];
    protected int cntsOverAllFeature[];
    protected int allTotal;
    protected boolean finalized;

    public abstract int getIndex(Object feature);

    public AbstractFeatureDictionary(int stateNum) {
        this.stateNum =stateNum;
    }

    public int getStateNum(){
        return stateNum;
    }

    public int getCount(Object feature) {
        int index;

        index=getIndex(feature);
        return ( (index>=0) ? cntsOverAllState[index] : 0);
    }

    public int getCount(int featureIndex, int label) {
        return cntsArray[featureIndex][label];
    }

    public int getCount(int featureIndex){
        return cntsOverAllState[featureIndex];
    }

    public int getStateCount(int state) {
        return cntsOverAllFeature[state];
    }

    public int getTotalCount() {
        return allTotal;
    }

    public int getNextStateWithFeature(int index, int prevLabel) {
        int k;

        if (prevLabel >= 0) {
            k = prevLabel+ 1;
        }
        else
            k=0;
        for (; k <cntsArray[index].length; k++) {
            if (cntsArray[index][k] > 0) {
                return k;
            }
        }
        return -1;
    }
}