package dragon.ir.clustering;

import dragon.ir.clustering.clustermodel.ClusterModel;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;

/**
 * <p>BisectKMean clustering</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BisectKMean extends AbstractClustering{
    protected ClusterModel distMetric;
    protected boolean refine;
    protected int maxIteration;
    protected DocClusterSet clusterSet;
    private boolean initAllObjs;

    public BisectKMean(IndexReader indexReader,ClusterModel distMetric, int clusterNum){
        this(indexReader,distMetric,clusterNum,false);
    }

    public BisectKMean(IndexReader indexReader,ClusterModel distMetric, int clusterNum, boolean initAllObjs) {
        super(indexReader);
        this.clusterNum =clusterNum;
        this.distMetric =distMetric;
        this.refine =false;
        this.maxIteration=200;
        this.randomSeed=0;
        this.initAllObjs =initAllObjs;
    }

    /**
     * If this option is true, the algorithm randomly assign all objects to the given number of clusters during initialization.
     * Otherwise, it picks only one object for each cluster during initialization.
     * @param option whether use all objects for initialization
     */
    public void setUseAllObjectForInitialization(boolean option){
        this.initAllObjs =option;
    }

    public boolean getUseAllObjectForInitialization(){
        return initAllObjs;
    }

    public boolean cluster(IRDoc[] arrDoc){
        if(featureFilter!=null){
            featureFilter.initialize(indexReader,arrDoc);
            distMetric.setFeatureFilter(featureFilter);
        }
        return cluster(arrDoc, 2);
    }

    public void setRefine(boolean refine){
        this.refine=refine;
    }

    public int getMaxIteration(){
        return maxIteration;
    }

    public void setMaxIteration(int iteration){
        this.maxIteration =iteration;
    }

    private boolean cluster(IRDoc[] arrDoc, int secNum){
        DocClusterSet mainDcs,dcs;
        DocCluster dc;
        BasicKMean bkm;
        int i,index,max,id;
        if(clusterNum<secNum) return false;

        distMetric.setClusterNum(secNum);
        bkm = new BasicKMean(indexReader, distMetric, secNum, initAllObjs);
        bkm.setFeatureFilter(featureFilter);
        bkm.setMaxIteration(maxIteration);
        bkm.setRandomSeed(randomSeed);
        bkm.setShowProgress(showProgress);
        id=0;
        mainDcs = new DocClusterSet(clusterNum);

        bkm.cluster(arrDoc);
        dcs = bkm.getClusterSet();
        for (i = 0; i <secNum; i++) {
             mainDcs.setDocCluster(dcs.getDocCluster(i),id++);
        }

        while(id<clusterNum){
            max = Integer.MIN_VALUE;
            index = -1;
            for(i=0;i<id;i++){
               dc=mainDcs.getDocCluster(i);
               if(max<dc.getDocNum()){
                   max=dc.getDocNum();
                   index=i;
               }
            }
            dc = mainDcs.getDocCluster(index);

            arrDoc = new IRDoc[dc.getDocNum()];
            for (i = 0; i < dc.getDocNum(); i++){
                arrDoc[i] = dc.getDoc(i);
                arrDoc[i].setCategory(-1);
            }
            distMetric.setClusterNum(secNum);
            bkm=new BasicKMean(indexReader,distMetric,secNum,initAllObjs);
            bkm.setMaxIteration(maxIteration);
            bkm.setRandomSeed(randomSeed);
            bkm.setShowProgress(showProgress);
            bkm.cluster(arrDoc);

            mainDcs.setDocCluster(bkm.getClusterSet().getDocCluster(0),index);
            for(i=1;i<bkm.getClusterNum();i++){
                mainDcs.setDocCluster(bkm.getClusterSet().getDocCluster(i),id++);
            }
        }

        if(refine){
            distMetric.setClusterNum(clusterNum);
            bkm=new BasicKMean(indexReader,distMetric,mainDcs);
            bkm.setMaxIteration(maxIteration);
            bkm.setRandomSeed(randomSeed);
            bkm.setShowProgress(showProgress);
            arrDoc = new IRDoc[indexReader.getCollection().getDocNum()];
            bkm.cluster(arrDoc);
        }
        this.clusterSet=mainDcs;

        return true;
    }

    public DocClusterSet getClusterSet(){
      return clusterSet;
  }

}