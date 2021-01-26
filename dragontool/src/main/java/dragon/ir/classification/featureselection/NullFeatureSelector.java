package dragon.ir.classification.featureselection;

import dragon.ir.classification.DocClassSet;
import dragon.ir.index.IndexReader;
import dragon.matrix.SparseMatrix;
/**
 * <p>A Null Feature Selector </p>
 * <p>No feature reduction is done actually</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class NullFeatureSelector extends AbstractFeatureSelector implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	protected int[] getSelectedFeatures(IndexReader indexReader, DocClassSet trainingSet){
        int[] featureMap;

        featureMap=new int[indexReader.getCollection().getTermNum()];
        for(int i=0;i<featureMap.length;i++)
            featureMap[i]=i;
        return featureMap;
    }

    protected int[] getSelectedFeatures(SparseMatrix doctermMatrix, DocClassSet trainingSet){
        int[] featureMap;

        featureMap=new int[doctermMatrix.columns()];
        for(int i=0;i<featureMap.length;i++)
            featureMap[i]=i;
        return featureMap;
    }

}