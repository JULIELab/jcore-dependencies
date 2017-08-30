package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;
import dragon.ml.seqmodel.model.*;

/**
 * <p>The feature type for start </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureTypeStart extends AbstractFeatureType {
    private ModelGraph model;
    private int curState;
    private int index;

    public FeatureTypeStart(ModelGraph model) {
        super(false);
        idPrefix="S_";
        this.model =model;
    }

    public boolean startScanFeaturesAt(DataSequence data, int pos){
        return startScanFeaturesAt(data,pos,pos);
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        if (startPos>0) {
            curState = -1;
            return false;
        }
        else {
            index = 0;
            curState = model.getStartState(index);
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
        curState=model.getStartState(index);
        return f;
    }
};
