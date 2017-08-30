package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;

/**
 * <p>Abstract class for feature type </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


 public abstract class AbstractFeatureType implements FeatureType {
    protected String idPrefix;
    private boolean needTraining;
    private int typeID;

    public AbstractFeatureType(boolean needTraining) {
        this.needTraining =needTraining;
        typeID=-1;
    }

    public boolean needTraining(){
        return needTraining;
    }

    public boolean train(Dataset data) {
        return true;
    }

    public boolean saveTrainingResult(){
        return true;
    }

    public boolean readTrainingResult(){
        return true;
    }

    public int getTypeID(){
        return typeID;
    }

    public void setTypeID(int typeID){
        this.typeID =typeID;
    }

    public boolean startScanFeaturesAt(DataSequence data, int pos){
        return startScanFeaturesAt(data, pos, pos);
    }

    public boolean supportSegment(){
        return true;
    }
};

