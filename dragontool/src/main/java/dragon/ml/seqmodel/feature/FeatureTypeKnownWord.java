package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;


/**
 * <p>Feature type of known word</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureTypeKnownWord extends AbstractFeatureType {
    private FeatureDictionary dict;
    private int stateNum;
    private int curState;
    private double wordFreq;
    private int wordIndex;
    private boolean caseSensitive;

    public FeatureTypeKnownWord(FeatureDictionary wordDict){
        this(wordDict,false);
    }

    public FeatureTypeKnownWord(FeatureDictionary wordDict, boolean caseSensitive) {
        super(false);
        dict =wordDict;
        stateNum=dict.getStateNum();
        idPrefix="K_";
    }

    private void getNextState() {
        for (curState++; curState <stateNum; curState++) {
            if (dict.getCount(wordIndex, curState) == 0) {
                return;
            }
        }
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        String token;

        curState=-1;
        if(startPos!=endPos){
            System.out.println("The starting position and the ending position should be the same for word features");
            return false;
        }

        wordIndex=data.getToken(endPos).getIndex();
        if(wordIndex<0){
            token = data.getToken(endPos).getContent();
            if (!caseSensitive)
                token = token.toLowerCase();
            wordIndex = dict.getIndex(token);
        }
        if(wordIndex<0)
            return false;

        if (dict.getCount(wordIndex) <= FeatureTypeWord.RARE_THRESHOLD + 1)
            return false;

        getNextState();
        wordFreq = Math.log( (double) dict.getCount(wordIndex)/ dict.getTotalCount());
        return true;
    }

    public boolean hasNext() {
        return (curState<stateNum && curState>=0);
    }

    public Feature next() {
        BasicFeature f;
        FeatureIdentifier id;

        id=new FeatureIdentifier(idPrefix, curState, curState);
        f=new BasicFeature(id,curState,wordFreq);
        getNextState();
        return f;
    }
};
