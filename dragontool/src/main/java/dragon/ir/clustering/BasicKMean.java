package dragon.ir.clustering;

import dragon.ir.clustering.clustermodel.ClusterModel;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;

import java.util.Random;

/**
 * <p>Basic KMeans clustering</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicKMean extends AbstractClustering{
    protected ClusterModel distMetric;
    protected boolean initialized;
    protected int maxIteration;
    private boolean initAllObjs;

    public BasicKMean(IndexReader indexReader,ClusterModel distMetric, int clusterNum){
        this(indexReader,distMetric,clusterNum,false);
    }

    public BasicKMean(IndexReader indexReader,ClusterModel distMetric, int clusterNum, boolean initAllObjs) {
        super(indexReader);
        this.clusterNum =clusterNum;
        this.distMetric =distMetric;
        this.initialized =false;
        this.maxIteration =200;
        this.randomSeed=0;
        this.initAllObjs =initAllObjs;
    }

    public BasicKMean(IndexReader indexReader,ClusterModel distMetric, DocClusterSet initClusterSet) {
        super(indexReader);
        this.clusterNum =initClusterSet.getClusterNum();
        this.distMetric =distMetric;
        this.clusterSet=initClusterSet;
        distMetric.setDocClusters(initClusterSet);
        this.initialized =true;
        this.maxIteration =200;
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

    protected boolean initialize(IRDoc[] arrDoc){
        Random random;
        int i, curDocNo;

        random=new Random();
        if(randomSeed>0)
            random.setSeed(randomSeed);
        clusterSet=new DocClusterSet(clusterNum);
        for(i=0;i<arrDoc.length;i++)
            arrDoc[i].setCategory(-1);

        i=0;
        //randomly assign one object to each cluster
        while(i<clusterNum){
            curDocNo=(int)(random.nextDouble()*arrDoc.length);
            if(arrDoc[curDocNo].getCategory()==-1){
                clusterSet.addDoc(i,arrDoc[curDocNo]);
                i++;
            }
        }
        // randomly assign remaining objects to given clusters if initAllObjs option is true.
        for(i=0;initAllObjs && i<arrDoc.length;i++){
            if(arrDoc[i].getCategory()!=-1)
                continue;
            clusterSet.addDoc(random.nextInt(clusterNum),arrDoc[i]);
        }
        distMetric.setDocClusters(clusterSet);
        return true;
    }

    public boolean cluster(IRDoc[] arrDoc){
        Random random;
        double curDist, minDist;
        int[] arrCanCluster;
        int candidateNum, movingObj, docNum, curCluster,oldCluster,iteration;
        int i,j;

        //initialization
        if(featureFilter!=null){
            featureFilter.initialize(indexReader,arrDoc);
            distMetric.setFeatureFilter(featureFilter);
        }
        random=new Random();
        if(randomSeed>0)
            random.setSeed(randomSeed);
        arrCanCluster=new int[clusterNum];
        docNum=arrDoc.length;
        movingObj =docNum;
        iteration = 0;
        if(!initialized && !initialize(arrDoc))
           return false;

        //loop
        while(movingObj>0 && iteration<maxIteration){
            if(showProgress){
                System.out.print((new java.util.Date()).toString()+" "+iteration++);
                System.out.print(" ");
                System.out.println(movingObj);
            }

            movingObj=0;
            //assign objects to new cluster
            for (i = 0; i < docNum; i++) {
                minDist=Double.MAX_VALUE;

                //find out the closest cluster
                candidateNum=0;
                for (j = 0; j < clusterNum; j++) {
                    curDist = distMetric.getDistance(arrDoc[i],j);
                    if (curDist<=minDist-0.00001) {
                        minDist = curDist;
                        arrCanCluster[0]=j;
                        candidateNum = 1;
                    }
                    else if(Math.abs(curDist-minDist)<0.00001){
                        if(curDist<minDist)
                            minDist=curDist;
                        arrCanCluster[candidateNum]=j;
                        candidateNum++;
                    }
                }
                if(candidateNum==1)
                    curCluster=arrCanCluster[0];
                else{
                    curCluster=(int)(random.nextDouble()*candidateNum);
                    if(curCluster==candidateNum)
                        curCluster=arrCanCluster[curCluster-1];
                    else
                        curCluster=arrCanCluster[curCluster];
                }
                // end of finding

                oldCluster=arrDoc[i].getCategory();
                if(curCluster!=oldCluster)
                //if the old cluster contains only one document, don't remove the document from the old cluster
                //if(curCluster!=oldCluster && (oldCluster<0 || clusterSet.getDocCluster(oldCluster).getDocNum()>=2))
                {
                    clusterSet.removeDoc(arrDoc[i].getCategory(),arrDoc[i]);
                    clusterSet.addDoc(curCluster,arrDoc[i]);
                    movingObj++;
                }
            }

            //re-compute the centroids
            if(movingObj==0) break;
            distMetric.setDocClusters(clusterSet);
        }
        return true;
    }

    public int getMaxIteration(){
        return maxIteration;
    }

    public void setMaxIteration(int iteration){
        this.maxIteration =iteration;
    }
}
