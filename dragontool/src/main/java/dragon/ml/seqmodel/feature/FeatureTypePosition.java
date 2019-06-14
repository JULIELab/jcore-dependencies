package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;

/**
 * This can be used as a wrapper around a FeatureType class that wants to
 * generate features which take into account the normalized distance of the token to
 * the start of a segement.
 */

public class FeatureTypePosition extends AbstractFeatureTypeWrapper {
    private Feature savedFeature;
    private boolean squareSent;
    private int segStart;
    private int segEnd;
    private int currPos;
    private transient DataSequence dataSeq;

    public FeatureTypePosition(FeatureType ftype) {
        super(ftype);
    }

    private void advance() {
        while (true) {
            if (ftype.hasNext())
                return;
            currPos++;
            if (currPos > segEnd)
                return;
            ftype.startScanFeaturesAt(dataSeq,currPos, currPos);
        }
    }

    public  boolean startScanFeaturesAt(DataSequence data, int pos) {
        return startScanFeaturesAt(data,pos,pos);
    };

    public  boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        segStart = startPos;
        segEnd = endPos;
        currPos = startPos;
        squareSent=true;
        dataSeq = data;
        ftype.startScanFeaturesAt(data,startPos, startPos);
        advance();
        return ftype.hasNext();
    }

    public boolean hasNext() {
        return !squareSent || ((currPos <= segEnd) && ftype.hasNext());
    }

    public Feature next() {
        FeatureIdentifier id;
        Feature f;

        if (!squareSent) {
            squareSent = true;
            // saved feature with value change to square.
            savedFeature.setValue(savedFeature.getValue()*savedFeature.getValue());
            id=savedFeature.getID();
            id.setName("POS^2" + id.getName());
            id.setId(id.getId()*2+1);
            advance();
            return savedFeature;
        }
        else {
            f=ftype.next();
            f.setValue((currPos-segStart+1)/(segEnd-segStart+1)); //dataLen;
            savedFeature=f.copy();
            squareSent = false;
            id=f.getID();
            id.setName("POS_" + id.getName());
            id.setId(id.getId()*2);
            return f;
        }
    }
}
