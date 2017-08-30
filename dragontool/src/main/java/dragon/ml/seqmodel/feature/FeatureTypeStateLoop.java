package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;

/**
 * This can be used as a wrapper around a FeatureType class that wants to
 * generate a feature for each state.
 */

public class FeatureTypeStateLoop extends AbstractFeatureTypeWrapper {
    private Feature nextFeature;
	int stateNum;
	int curState;
	boolean optimize = false;

	public FeatureTypeStateLoop(FeatureType ftype, int stateNum) {
		super(ftype);
		this.stateNum = stateNum;
	}

	boolean advance() {
		curState++;
		if (curState <stateNum)
			return true;
		if (ftype.hasNext()) {
		    nextFeature=ftype.next();
			curState = 0;
		}
		return curState < stateNum;
	}

	public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
		curState =stateNum;
		ftype.startScanFeaturesAt(data,startPos, endPos);
		return advance();
	}

	public boolean hasNext() {
		return (curState <stateNum);
	}

	public Feature next() {
        FeatureIdentifier id;
        Feature curFeature;

        curFeature=nextFeature.copy();
		curFeature.setLabel(curState);
        id=curFeature.getID();
        id.setState(curState);
        id.setId(id.getId()*stateNum+curState);

		advance();
        return curFeature;
	}
};

