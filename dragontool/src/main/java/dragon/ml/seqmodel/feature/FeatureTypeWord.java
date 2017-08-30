package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;

/**
 *
 * <p>World feature type </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 *
 * If feature dictionary is given, no training is needed and all states are
 * simply emitted for each word. Otherwise training is needed.
 */
public class FeatureTypeWord extends AbstractFeatureType {
    public static int RARE_THRESHOLD=0;
    private FeatureDictionary dict;
    private String wordFile;
    private int stateNum;
    private int curState;
    private String token;
    private int tokenId;
    private boolean caseSensitive;

    public FeatureTypeWord(String wordFile, int stateNum){
        this(wordFile,stateNum, false);
    }

    public FeatureTypeWord(String wordFile, int stateNum, boolean caseSensitive) {
        super(true);
        this.caseSensitive =caseSensitive;
        this.stateNum =stateNum;
        this.wordFile =wordFile;
        dict=new FeatureDictionaryChar(stateNum,500);
        idPrefix="W_";
    }

    public FeatureTypeWord(FeatureDictionary dict, int stateNum){
        this(dict,stateNum,false);
    }

    public FeatureTypeWord(FeatureDictionary dict, int stateNum, boolean caseSensitive) {
        super(false);
        this.caseSensitive =caseSensitive;
        this.stateNum =stateNum;
        this.wordFile =null;
        this.dict=dict;
        idPrefix="W_";
    }

    public FeatureDictionary getWordDictionary(){
        return dict;
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos){

        curState = -1;
        if(startPos!=endPos){
            System.out.println("The starting position and the ending position should be the same for word features");
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

        if (dict.getCount(tokenId) > RARE_THRESHOLD) {
            getNextState();
            return true;
        }
        else
            return false;
    }

    public boolean hasNext() {
        return (curState>=0 && curState<stateNum);
    }

    public Feature next() {
        BasicFeature f;
        FeatureIdentifier id;

        id=new FeatureIdentifier(idPrefix+token, tokenId*stateNum+curState,curState);
        f=new BasicFeature(id,curState,1);
        getNextState();
        return f;
    }

    public boolean train(Dataset trainData) {
        DataSequence seq;
        BasicToken token;
        int pos, index;

        for (trainData.startScan(); trainData.hasNext(); ) {
            seq = trainData.next();
            for (pos = 0; pos < seq.length(); pos++) {
                if(pos>=0 && pos<seq.length()){
                    token = seq.getToken(pos);
                    if(caseSensitive)
                        index=dict.addFeature(token.getContent(), seq.getLabel(pos));
                    else
                        index=dict.addFeature(token.getContent().toLowerCase(), seq.getLabel(pos));
                    token.setIndex(index);
                }
            }
        }
        dict.finalize();
        return true;
    }

    public boolean readTrainingResult(){
        return dict.read(wordFile);
    }

    public boolean saveTrainingResult(){
        return dict.write(wordFile);
    }

    private void getNextState() {
        if(needTraining())
            curState = dict.getNextStateWithFeature(tokenId, curState);
        else
            curState++;
    }

    public boolean supportSegment(){
        return false;
    }
};


