package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;

/**
 * These return one feature per state.  The value of the feature is the
 * fraction of training instances passing through this state that contain
 * the word
 *
 * @author Sunita Sarawagi
 */

public class FeatureTypeWordScore extends AbstractFeatureType {
    private FeatureDictionary dict;
    private int curState;
    private int tokenId;
    private int stateNum;
    private boolean caseSensitive;

    public FeatureTypeWordScore(FeatureDictionary d) {
        this(d,false);
    }

    public FeatureTypeWordScore(FeatureDictionary d, boolean caseSensitive) {
        super(false);
        this.caseSensitive =caseSensitive;
        dict = d;
        stateNum=d.getStateNum();
        idPrefix="WS_";
    }

    private void getNextLabel() {
        curState = dict.getNextStateWithFeature(tokenId, curState);
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        String token;

        curState = -1;
        if(startPos!=endPos){
            System.out.println("The starting position and the ending position should be the same for word score features");
            return false;
        }

        tokenId=data.getToken(endPos).getIndex();
        if(tokenId<0){
            token = data.getToken(endPos).getContent();
            if (!caseSensitive)
                token = token.toLowerCase();
            tokenId = dict.getIndex(token);
        }
        if(tokenId<0)
            return false;

        if (dict.getCount(tokenId) > FeatureTypeWord.RARE_THRESHOLD) {
            getNextLabel();
            return true;
        }
        else
            return false;
    }

    public boolean hasNext() {
        return (curState < stateNum) && (curState>= 0);
    }

    public Feature next() {
        BasicFeature f;
        FeatureIdentifier id;
        double val;

        id=new FeatureIdentifier(idPrefix, curState,curState);
        val=Math.log( ( (double) dict.getCount(tokenId, curState)) / dict.getStateCount(curState));
        f=new BasicFeature(id,curState,val);
        getNextLabel();
        return f;
    }

    public boolean supportSegment(){
        return false;
    }
};


