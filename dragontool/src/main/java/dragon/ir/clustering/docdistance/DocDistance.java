package dragon.ir.clustering.docdistance;

import dragon.ir.clustering.featurefilter.FeatureFilter;
import dragon.ir.index.IRDoc;
/**
 * <p>Interface of pair-wised document distance metric</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface DocDistance {
    /**
     * @param first the first document
     * @param second the second document
     * @return the distance of the given document pair
     */
    public double getDistance(IRDoc first, IRDoc second);

    /**
     * A feature selector is set. After that, the excluded features do not count in cluster model any more.
     * @param selector the feature selector
     */
    public void setFeatureFilter(FeatureFilter selector);

    /**
     * @return the feature selector used for the cluster model
     */
    public FeatureFilter getFeatureFilter();

}