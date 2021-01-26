package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.model.ModelGraph;


/**
 * <p>Feature type for end </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureTypeEnd  extends AbstractFeatureType {
    private ModelGraph model;
    private int curState;
    private int index;

    public FeatureTypeEnd(ModelGraph model) {
        super(false);
        idPrefix="END_";
        this.model =model;
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        if (endPos<data.length()-1) {
            curState = -1;
            return false;
        }
        else {
            index = 0;
            curState = model.getEndState(index);
            return true;
        }
    }

    public boolean hasNext() {
        return (curState >= 0);
    }

    public Feature next() {
        BasicFeature f;
        FeatureIdentifier id;
        id=new FeatureIdentifier(idPrefix, curState,curState);
        f=new BasicFeature(id,curState,1);
        index++;
        curState=model.getEndState(index);
        return f;
    }

};
