package dragon.ir.clustering;

import dragon.ir.clustering.featurefilter.FeatureFilter;
import dragon.ir.index.*;

/**
 * <p>Abstract class for document clustering </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractClustering implements Clustering{
    protected IndexReader indexReader;
    protected FeatureFilter featureFilter;
    protected int clusterNum;
    protected DocClusterSet clusterSet;
    protected boolean showProgress;
    protected long randomSeed;

    public AbstractClustering(IndexReader indexReader) {
        this.indexReader =indexReader;
        this.showProgress =true;
    }

    public int getClusterNum(){
        return clusterNum;
    }

    public long getRandomSeed(){
        return randomSeed;
    }

    public void setRandomSeed(long seed){
        this.randomSeed =seed;
    }

    public DocClusterSet getClusterSet(){
        return clusterSet;
    }

    public DocCluster getCluster(int index){
        return clusterSet.getDocCluster(index);
    }

    public IndexReader getIndexReader(){
        return indexReader;
    }

    public boolean cluster(){
        IRDoc[] arrDoc;
        int i;

        arrDoc=new IRDoc[indexReader.getCollection().getDocNum()];
        for(i=0;i<arrDoc.length;i++)
            arrDoc[i]=indexReader.getDoc(i);
        return cluster(arrDoc);
    }

    public FeatureFilter getFeatureFilter(){
        return featureFilter;
    }


    public void setFeatureFilter(FeatureFilter selector){
        this.featureFilter =selector;
    }

    public void setShowProgress(boolean option){
        this.showProgress =option;
    }
}