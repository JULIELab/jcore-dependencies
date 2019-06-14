package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;

/**
 * <p>This feature type prior </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureTypePrior extends AbstractFeatureType {
    private int stateNum;
    private int curState;

    public FeatureTypePrior(int stateNum) {
        super(false);
        this.stateNum=stateNum;
        idPrefix="Bias_";
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        curState= stateNum-1;
        return hasNext();
    }

    public boolean hasNext() {
        return curState>= 0;
    }

    public Feature next() {
        Feature f;
        FeatureIdentifier id;

        id=new FeatureIdentifier(idPrefix+curState,curState,curState);
        f=new BasicFeature(id,curState,1);
        curState--;
        return f;
    }
}
