package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;

/**
 * This FeatureType can not be used independently because the label is not set.
 * This feature type should be wrapped by FeatureTypeStateLoop.
 */

public class FeatureTypeSegmentLengthPoly extends AbstractFeatureType {
    private double lenSq;
    private short callNo;
    private int maxSegLen;

    public FeatureTypeSegmentLengthPoly(int maxSegmentLength) {
        super(false);
        this.maxSegLen = maxSegmentLength;
    }

    public boolean startScanFeaturesAt(DataSequence data, int pos){
        return startScanFeaturesAt(data, pos, pos);
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        lenSq = (endPos+1-startPos)/maxSegLen;
        callNo = 0;
        return true;
    }

    public boolean hasNext() {
        return callNo < 2;
    }

    public Feature next() {
        Feature f;
        String name;
        FeatureIdentifier id;
        int curState;

        curState=-1;
        name = (callNo==0)?"LENGTH^1":"LENGTH^2";
        if (callNo == 0) {
            id=new FeatureIdentifier(name,0,curState);
            f=new BasicFeature(id,curState,lenSq);
        } else {
            id=new FeatureIdentifier(name,1,curState);
            f=new BasicFeature(id,curState,lenSq*lenSq);
        }
        callNo++;
        return f;
    }
}
