package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;

/**
 * This FeatureType can not be used independently because the label is not set.
 * This feature type should be wrapped by FeatureTypeStateLoop.
 */

public class  FeatureTypeSegmentLength extends AbstractFeatureType {
    protected int segLen;
    protected int maxLen;

    public FeatureTypeSegmentLength() {
        super(false);
        maxLen = Integer.MAX_VALUE;
    }

    public FeatureTypeSegmentLength(int maxSegmentLength) {
        super(false);
        maxLen = maxSegmentLength;
    }

    public  boolean startScanFeaturesAt(DataSequence data, int pos) {
        return startScanFeaturesAt(data,pos,pos);
    }

    public  boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        segLen = Math.min(endPos+1-startPos,maxLen);
        return true;
    }

    public boolean hasNext() {
        return segLen > 0;
    }

    public  Feature next() {
        Feature f;
        String name;
        FeatureIdentifier id;
        int curState;

        curState=-1;
        name = "Length" + ((segLen==maxLen)?">=":"=") + segLen;
        id=new FeatureIdentifier(name,segLen,curState);
        f=new BasicFeature(id,curState,1);
        segLen = 0;
        return f;
    }
};

