package dragon.ir.clustering.docdistance;

import dragon.ir.clustering.featurefilter.FeatureFilter;
import dragon.matrix.SparseMatrix;

/**
 * <p>Abstract Documennt Distance Measure</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractDocDistance implements DocDistance{
    protected FeatureFilter featureFilter;
    protected SparseMatrix matrix;

    public AbstractDocDistance(SparseMatrix matrix){
        this.matrix =matrix;
    }

    public void setFeatureFilter(FeatureFilter selector){
        this.featureFilter=selector;
    }

    public FeatureFilter getFeatureFilter(){
        return featureFilter;
    }
}