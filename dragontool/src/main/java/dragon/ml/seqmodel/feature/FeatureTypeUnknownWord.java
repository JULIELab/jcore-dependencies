package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;

/**
 * <p>Feature type for unknown world </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureTypeUnknownWord extends AbstractFeatureType {
    private int curState;
    private FeatureDictionary dict;
    private int stateNum;
    private boolean caseSensitive;

    public FeatureTypeUnknownWord(FeatureDictionary d){
        this(d,false);
    }

    public FeatureTypeUnknownWord(FeatureDictionary d, boolean caseSensitive) {
        super(false);
        this.caseSensitive =caseSensitive;
        dict = d;
        idPrefix="UW";
        stateNum=d.getStateNum();
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        String token;
        int count;

        curState =stateNum;
        if(startPos!=endPos){
            System.out.println("The starting position and the ending position should be the same for unknown word features");
            return false;
        }

        if(data.getToken(endPos).getIndex()>=0)
            count=dict.getCount(data.getToken(endPos).getIndex());
        else{
            token = data.getToken(endPos).getContent();
            if (!caseSensitive)
                token = token.toLowerCase();
            count=dict.getCount(token);
        }
        if (count> FeatureTypeWord.RARE_THRESHOLD + 1) {
            return false;
        } else {
            curState = 0;
            return true;
        }
    }

    public boolean hasNext() {
        return (curState <stateNum);
    }

    public Feature next() {
        Feature f;
        FeatureIdentifier id;

        id=new FeatureIdentifier(idPrefix, curState, curState);
        f=new BasicFeature(id,curState,1);
        curState++;
        return f;
    }

    public boolean supportSegment(){
        return false;
    }
};
