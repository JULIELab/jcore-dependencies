package dragon.ir.clustering;

import dragon.ir.clustering.featurefilter.FeatureFilter;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;

/**
 * <p>Interface of text clustering method</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Clustering {
    /**
     * @return the number of clusters
     */
    public int getClusterNum();

    public long getRandomSeed();
    public void setRandomSeed(long seed);

    /**
     * One should call the cluster method before calling this method.
     * @return all clusters
     */
    public DocClusterSet getClusterSet();

    /**
     * One should call the cluster method before calling this method.
     * @param index the index of the cluster of interest
     * @return the given cluster
     */
    public DocCluster getCluster(int index);

    /**
     * @return the index reader the clustering method is working on
     */
    public IndexReader getIndexReader();

    /**
     * Cluster all documents in the index reader to the given number of clusters
     * @return true if clustering successfully
     */
    public boolean cluster();

    /**
     * Cluster given testing documents to the given number of clusters
     * @param arrDoc the document set for clustering
     * @return true if clustering successfully
     */
    public boolean cluster(IRDoc[] arrDoc);

    public FeatureFilter getFeatureFilter();

    public void setFeatureFilter(FeatureFilter featureFilter);
}