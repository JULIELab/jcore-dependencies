package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;
import dragon.ml.seqmodel.model.*;


/**
 * <p>Feature type edge </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureTypeEdge extends AbstractFeatureType {
    private ModelGraph model;
	private EdgeIterator edgeIter;
	private int curEdgeIndex;

	public FeatureTypeEdge(ModelGraph model) {
		super(false);
        this.model=model;
        this.edgeIter=null;
	}

	public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
		if (startPos <= 0) {
			curEdgeIndex = model.getEdgeNum();
			return false;
		}
        else {
			curEdgeIndex = 0;
            edgeIter=model.getEdgeIterator();
			if (edgeIter != null)
			    edgeIter.start();
			return hasNext();
		}
	}

	public boolean hasNext() {
		return (edgeIter != null) && (curEdgeIndex< model.getEdgeNum());
	}

	public Feature next() {
        Feature f;
        Edge e;
        FeatureIdentifier id;
        String name;
        boolean edgeIsOuter;

		edgeIsOuter = edgeIter.nextIsOuter();
		e = edgeIter.next();
        name = "E."+model.getLabel(e.getStart());
		if (edgeIsOuter)
			id=new FeatureIdentifier(name,model.getLabel(e.getStart())*model.getLabelNum()+model.getLabel(e.getEnd()) + model.getEdgeNum(), model.getLabel(e.getEnd()));
		else
			id=new FeatureIdentifier(name,curEdgeIndex,e.getEnd());
        f=new BasicFeature(id,e.getStart(),e.getEnd(),1);
		curEdgeIndex++;
        return f;
	}
};
