package dragon.ir.clustering;

import dragon.ir.index.*;
import dragon.ir.clustering.docdistance.*;

/**
 * <p>Hierarchical clustering with options of single, complete, and average linkage </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class HierClustering extends AbstractClustering {
    public static final int SINGLE_LINKAGE=-1;
    public static final int AVERAGE_LINKAGE=0;
    public static final int COMPLETE_LINKAGE=1;

    private DocDistance distMetric;
    private double[][] ccDist;
    private int linkage;

    public HierClustering(IndexReader indexReader,DocDistance distMetric, int clusterNum,int linkage) {
        super(indexReader);
        this.distMetric=distMetric;
        this.clusterNum=clusterNum;
        this.linkage=linkage;
    }

     public boolean cluster(IRDoc[] arrDoc){
         DocClusterSet newClusterSet;
         double min;
         int i,j,k,indexI,indexJ;

         //initialization
         if(featureFilter!=null){
            featureFilter.initialize(indexReader,arrDoc);
            distMetric.setFeatureFilter(featureFilter);
        }

        if(showProgress)
             System.out.println(new java.util.Date() + " Computing the pairwise document similarity...");
         clusterSet = new DocClusterSet(arrDoc.length);
         ccDist = new double[arrDoc.length][arrDoc.length];
         for (i = 0; i < arrDoc.length; i++) {
             clusterSet.addDoc(i,arrDoc[i]);
             ccDist[i][i]=0;
             for (j = i+1; j < arrDoc.length; j++) {
                 ccDist[i][j] = Math.min(distMetric.getDistance(arrDoc[i], arrDoc[j]), distMetric.getDistance(arrDoc[j], arrDoc[i]));
                 ccDist[j][i]=ccDist[i][j];
             }
         }

         k=0;
         while ( k+clusterNum<arrDoc.length) {
             if(showProgress && k++%100==0)
                 System.out.println(new java.util.Date() + " " + (arrDoc.length - k));

             //find the closest cluster pair
             min = Double.MAX_VALUE;
             indexI = -1;
             indexJ = -1;
             for (i = 0; i < arrDoc.length; i++) {
                 if(ccDist[i][0]==-1) continue;
                 for (j = 0; j < arrDoc.length; j++) {
                     if (i == j || ccDist[i][j] == -1)
                         continue;
                     if (min > ccDist[i][j]) {
                         min = ccDist[i][j];
                         indexI = i;
                         indexJ = j;
                     }
                 }
             }
             mergeCluster(clusterSet.getDocCluster(indexI), clusterSet.getDocCluster(indexJ));
         }

         //post processing
         newClusterSet = new DocClusterSet(clusterNum);
         j=0;
         for(i=0;i<clusterSet.getClusterNum();i++){
             if(clusterSet.getDocCluster(i).getDocNum()==0) continue;
             newClusterSet.setDocCluster(clusterSet.getDocCluster(i),j++);
         }
         clusterSet=newClusterSet;

         return true;
     }

     private void mergeCluster(DocCluster mergingCluster, DocCluster deletingCluster){
         int curMerging, curDeleting;
         int i;

         curMerging=mergingCluster.getClusterID();
         curDeleting=deletingCluster.getClusterID();

         //re-compute the distance of the merged cluster to all other remaning clusters
         for (i = 0; i < clusterSet.getClusterNum(); i++) {
             if (i==curMerging || i==curDeleting || clusterSet.getDocCluster(i).getDocNum()==0)
                  continue;
             ccDist[curMerging][i] = getDistance(mergingCluster ,deletingCluster, i, linkage);
             ccDist[i][curMerging] = ccDist[curMerging][i];
         }

         //clear the distance related to the deleting cluster
         for(i=0; i< clusterSet.getClusterNum();i++){
             ccDist[curDeleting][i] = -1;
             ccDist[i][curDeleting] = -1;
         }

         //merge the deleting cluster into the merging cluster
        for (i = 0; i < deletingCluster.getDocNum(); i++)
            mergingCluster.addDoc(deletingCluster.getDoc(i));
        deletingCluster.removeAll();
     }

     private double getDistance(DocCluster mergingCluster, DocCluster deletingCluster, int clusterID, int linkage) {
         int curMerging, curDeleting;

         curMerging=mergingCluster.getClusterID();
         curDeleting=deletingCluster.getClusterID();
         if (linkage == SINGLE_LINKAGE)
             return Math.min(ccDist[curMerging][clusterID], ccDist[curDeleting][clusterID]);
         else if (linkage == COMPLETE_LINKAGE)
             return Math.max(ccDist[curMerging][clusterID], ccDist[curDeleting][clusterID]);
         else {
             return (ccDist[curMerging][clusterID] * mergingCluster.getDocNum()  +
                       ccDist[curDeleting][clusterID] * deletingCluster.getDocNum())/ (mergingCluster.getDocNum() + deletingCluster.getDocNum());
         }
     }
}