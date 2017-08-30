package dragon.ir.clustering.featurefilter;

import dragon.ir.index.*;

/**
 * <p>Interface of unsupervised feature selector for text clustering.</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface FeatureFilter {
    /**
     * This method chooses a subset of features for text clustering
     * @param indexReader the index reader for a collection
     * @param docSet the document set for clustering
     */
    public void initialize(IndexReader indexReader, IRDoc[] docSet);

    /**
     * @param originalFeatureIndex the index of the feature in the old feature space
     * @return true if the given feature is selected for text clustering
     */
    public boolean isSelected(int originalFeatureIndex);

    /**
     * @return the number of selected features
     */
    public int getSelectedFeatureNum();

    /**
     * Map the old feature index to the index in the new feature space.
     * @param originalFeatureIndex the index of the feature before feature selection
     * @return the index of the feature the new space. If the feature is not selected, it will return -1.
     */
    public int map(int originalFeatureIndex);
}
