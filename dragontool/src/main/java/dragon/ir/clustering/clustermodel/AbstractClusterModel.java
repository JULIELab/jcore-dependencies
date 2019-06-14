package dragon.ir.clustering.clustermodel;

import dragon.ir.clustering.DocCluster;
import dragon.ir.clustering.DocClusterSet;
import dragon.ir.clustering.featurefilter.FeatureFilter;
import dragon.ir.index.IRDoc;

/**
 * <p>Abstract cluster for modeled clustering  result </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractClusterModel implements ClusterModel{
    protected FeatureFilter featureFilter;
    protected int clusterNum;

    public AbstractClusterModel(int clusterNum){
        this.clusterNum =clusterNum;
    }

    public void setDocClusters(DocClusterSet clusterSet){
        int i;

        for(i=0;i<clusterSet.getClusterNum();i++)
            setDocCluster(clusterSet.getDocCluster(i));
    }

    public double getDistance(IRDoc doc, DocCluster cluster){
        setDocCluster(cluster);
        return getDistance(doc,cluster.getClusterID());
    }

    public int getClusterNum(){
        return clusterNum;
    }

    public void setFeatureFilter(FeatureFilter selector){
        this.featureFilter=selector;
    }

    public FeatureFilter getFeatureFilter(){
        return featureFilter;
    }
}