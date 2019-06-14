package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.data.Dataset;


/**
 * <p>Abstract class for feature type wrapper </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public abstract class AbstractFeatureTypeWrapper implements FeatureType{
	protected FeatureType ftype;
    private int typeID;

	public AbstractFeatureTypeWrapper(FeatureType ftype) {
		this.ftype = ftype;
        typeID=-1;
	}

	public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
		return ftype.startScanFeaturesAt(data, startPos, endPos);
	}

	public boolean hasNext() {
		return ftype.hasNext();
	}

	public Feature next() {
		return ftype.next();
	}

    public boolean needTraining() {
        return ftype.needTraining();
    }

    public boolean train(Dataset data) {
        return ftype.train(data);
    }

    public boolean readTrainingResult() {
        return ftype.readTrainingResult();
    }

    public boolean saveTrainingResult() {
        return ftype.saveTrainingResult();
    }

    public int getTypeID(){
        return typeID;
    }

    public void setTypeID(int typeID){
        this.typeID =typeID;
    }

    public boolean supportSegment(){
        return true;
    }
}
