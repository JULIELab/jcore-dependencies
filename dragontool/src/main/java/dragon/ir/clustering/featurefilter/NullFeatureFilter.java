package dragon.ir.clustering.featurefilter;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;

/**
 * <p>A Null Feature Selector </p>
 * <p>No feature reduction is done actually</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class NullFeatureFilter extends AbstractFeatureFilter{

    protected int[] getSelectedFeatures(IndexReader indexReader, IRDoc[] docSet){
        int[] featureMap;

        featureMap=new int[indexReader.getCollection().getTermNum()];
        for(int i=0;i<featureMap.length;i++)
            featureMap[i]=i;
        return featureMap;
    }
}
