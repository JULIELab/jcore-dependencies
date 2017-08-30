package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;

/**
 * This can be used as a wrapper around a FeatureType class that wants to
 * generate features for each token in a segment.
 */

public class FeatureTypeTokenLoop extends AbstractFeatureTypeWrapper {
    private int currPos;
    private int segEnd;
    private transient DataSequence dataSeq;

    public FeatureTypeTokenLoop(FeatureType s) {
        super(s);
    }

    private void advance() {
        while (true) {
            if (ftype.hasNext())
                return;
            currPos++;
            if (currPos > segEnd)
                return;
            ftype.startScanFeaturesAt(dataSeq,currPos,currPos);
        }
    }

    public  boolean startScanFeaturesAt(DataSequence data, int pos){
        return startScanFeaturesAt(data,pos,pos);
    }

    public  boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        currPos = startPos;
        segEnd = endPos;
        dataSeq = data;
        ftype.startScanFeaturesAt(data,startPos,startPos);
        advance();
        return ftype.hasNext();
    }

    public boolean hasNext() {
        return (currPos <= segEnd) && ftype.hasNext();
    }

    public Feature next() {
        Feature f;

        f=ftype.next();
        advance();
        return f;
    }
};


