package dragon.ir.clustering.clustermodel;

import dragon.ir.clustering.DocCluster;
import dragon.ir.index.IRDoc;
import dragon.matrix.SparseMatrix;

/**
 * <p>Cluster model which computes Euclidean distance between a document and a cluster</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class EuclideanClusterModel extends AbstractClusterModel{
    private double[][] arrClusterVector;
    private double [] arrClusterVectorLen;
    private SparseMatrix matrix;

    public EuclideanClusterModel(int clusterNum, SparseMatrix docTerm) {
        super(clusterNum);
        arrClusterVectorLen=new double[clusterNum];
        this.matrix=docTerm;
    }

    public void setClusterNum(int clusterNum) {
        this.clusterNum = clusterNum;
        arrClusterVectorLen=new double[clusterNum];
    }

    public void setDocCluster(DocCluster cluster) {
        int i, j, featureNum, newIndex, clusterID, indexList[];
        double freqList[];

        if(featureFilter==null)
            featureNum=matrix.columns();
        else
            featureNum=featureFilter.getSelectedFeatureNum();
        if(arrClusterVector==null || arrClusterVector.length!=clusterNum || arrClusterVector[0].length!=featureNum)
            arrClusterVector = new double[clusterNum][featureNum];
        clusterID = cluster.getClusterID();

        for (i = 0; i < cluster.getDocNum(); i++) {
            indexList = matrix.getNonZeroColumnsInRow(cluster.getDoc(i).getIndex());
            freqList = matrix.getNonZeroDoubleScoresInRow(cluster.getDoc(i).getIndex());
            for (j = 0; j < indexList.length; j++){
                newIndex = indexList[j];
                if (featureFilter != null)
                    newIndex = featureFilter.map(newIndex);
                if(newIndex>=0)
                    arrClusterVector[clusterID][newIndex] += freqList[j];
            }
        }

        arrClusterVectorLen[clusterID]=0;
        for (i = 0; i < arrClusterVector[clusterID].length; i++) {
            if (arrClusterVector[clusterID][i] == 0)
                continue;
            arrClusterVector[clusterID][i] = arrClusterVector[clusterID][i] / cluster.getDocNum();
            arrClusterVectorLen[clusterID] += arrClusterVector[clusterID][i] * arrClusterVector[clusterID][i];
        }
    }

    public double getDistance(IRDoc doc, int clusterID) {
      int i,newIndex, indexList[];
      double product, freqList[];

      product=arrClusterVectorLen[clusterID];
      indexList = matrix.getNonZeroColumnsInRow(doc.getIndex());
      freqList = matrix.getNonZeroDoubleScoresInRow(doc.getIndex());

      for (i = 0; i < indexList.length; i++) {
          newIndex=indexList[i];
          if(featureFilter!=null)
              newIndex=featureFilter.map(newIndex);
          if(newIndex>=0){
              product += freqList[i]*(freqList[i]-2*arrClusterVector[clusterID][newIndex]);
          }
      }
      return Math.sqrt(product);
  }
}