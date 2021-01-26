package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.data.POSToken;
/**
 * <p>This feature type will create features about part of speech patterns for segments
 * This feature type should be wrapped by FeatureTypeStateLoop. </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */
public class FeatureTypePOSPattern  extends AbstractFeatureType {
    private int patternID;
    private int tagNum;
    private int maxSegmentLength;

    public FeatureTypePOSPattern(int stateNum, int tagNum) {
        this(stateNum,tagNum,1);
    }

    public FeatureTypePOSPattern(int stateNum, int tagNum, int maxSegmentLength) {
        super(false);
        idPrefix="PC_";
        this.maxSegmentLength =maxSegmentLength;
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        if(endPos-startPos+1>maxSegmentLength){
            patternID = -1;
            return false;
        }
        else{
            patternID = getMultiTag(data, startPos, endPos);
            return true;
        }
    }

    public boolean hasNext() {
        return (patternID>=0);
    }

    public Feature next() {
        BasicFeature f;
        FeatureIdentifier id;
        int curState;
        curState=-1;
        id=new FeatureIdentifier(idPrefix+String.valueOf(patternID), patternID,curState);
        f=new BasicFeature(id,curState,1);
        patternID=-1;
        return f;
    }

    private int getMultiTag(DataSequence data, int startPos, int endPos){
        int curMultiTag, curTag, i;

        curMultiTag=((POSToken)data.getToken(startPos)).getPOSTag();
        for(i=startPos+1;i<=endPos;i++){
            curTag=((POSToken)data.getToken(i)).getPOSTag();
            if(curTag<0)
                return -1;
            else
                curMultiTag=curMultiTag*tagNum+curTag;
        }
        return curMultiTag;
    }
};
