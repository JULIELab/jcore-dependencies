package dragon.ir.clustering.clustermodel;

import dragon.ir.clustering.*;
import dragon.ir.clustering.featurefilter.FeatureFilter;
import dragon.ir.index.*;

/**
 * <p>Interface of cluster model which compute the distance between a document and a document cluster</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface ClusterModel {
    /**
     * @param doc the document
     * @param cluster the document cluster
     * @return the distance between the document and the cluster
     */
    public double getDistance(IRDoc doc, DocCluster cluster);
    public double getDistance(IRDoc doc, int clusterNo);

    /**
     * Iterative partional approaches (e.g. K-Means) need to re-compute the cetroid of each cluster after each iteration. This method gives
     * the chance to re-compute the centroid.
     * @param cluster the new cluster
     */
    public void setDocCluster(DocCluster cluster);

    /**
     * This method is equal to calling the setDocCluster method for all clusters.
     * @param clusterSet all new clusters
     */
    public void setDocClusters(DocClusterSet clusterSet);
    public int getClusterNum();

    /**
     * @param clusterNum the number of clusters
     */
    public void setClusterNum(int clusterNum);

    /**
     * A feature selector is set. After that, the excluded features do not count in cluster model any more.
     * @param selector the feature selector
     */
    public void setFeatureFilter(FeatureFilter filter);

    /**
     * @return the feature selector used for the cluster model
     */
    public FeatureFilter getFeatureFilter();
}