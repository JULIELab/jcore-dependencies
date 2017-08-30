package dragon.ir.clustering.clustermodel;

import dragon.matrix.*;
import dragon.ir.clustering.*;
import dragon.ir.index.*;

/**
 * <p>Cluster model which uses vector cosine to compute the distance between a document and a cluster</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CosineClusterModel extends AbstractClusterModel{
    private double[][] arrClusterVector;
    private double [] arrClusterVectorLen;
    private SparseMatrix matrix;

    public CosineClusterModel(int clusterNum, SparseMatrix doctermMatrix) {
        super(clusterNum);
        this.matrix =doctermMatrix;
        arrClusterVectorLen=new double[clusterNum];
    }

    public void setClusterNum(int clusterNum){
        this.clusterNum = clusterNum;
        arrClusterVectorLen = new double[clusterNum];
    }

    public void setDocCluster(DocCluster cluster) {
        int i, j, featureNum, newIndex, clusterID, indexList[];
        double vectorLength, freqList[];

        if(featureFilter==null)
            featureNum=matrix.columns();
        else
            featureNum=featureFilter.getSelectedFeatureNum();
        if(arrClusterVector==null || arrClusterVector.length!=clusterNum || arrClusterVector[0].length!=featureNum)
            arrClusterVector = new double[clusterNum][featureNum];
        clusterID = cluster.getClusterID();
        vectorLength = 0;

        for (i = 0; i < cluster.getDocNum(); i++) {
            indexList =matrix.getNonZeroColumnsInRow(cluster.getDoc(i).getIndex());
            freqList = matrix.getNonZeroDoubleScoresInRow(cluster.getDoc(i).getIndex());
            for (j = 0; j < indexList.length; j++){
                newIndex = indexList[j];
                if (featureFilter != null)
                    newIndex = featureFilter.map(newIndex);
                if(newIndex>=0)
                    arrClusterVector[clusterID][newIndex] += freqList[j];
            }
        }

        for (i = 0; i < arrClusterVector[clusterID].length; i++) {
            if (arrClusterVector[clusterID][i] == 0)
                continue;
            arrClusterVector[clusterID][i] = arrClusterVector[clusterID][i] / cluster.getDocNum();
            vectorLength += arrClusterVector[clusterID][i] * arrClusterVector[clusterID][i];
        }
        arrClusterVectorLen[clusterID] = Math.sqrt(vectorLength);
    }

    public double getDistance(IRDoc doc, int clusterID) {
        int i,newIndex,indexList[];
        double product,docLength, freqList[];

        product=0;
        docLength=0;
        indexList = matrix.getNonZeroColumnsInRow(doc.getIndex());
        freqList = matrix.getNonZeroDoubleScoresInRow(doc.getIndex());

        for (i = 0; i < indexList.length; i++) {
            newIndex=indexList[i];
            if(featureFilter!=null)
                newIndex=featureFilter.map(newIndex);
            if(newIndex>=0){
                product += arrClusterVector[clusterID][newIndex] * freqList[i];
                docLength += freqList[i] * freqList[i];
            }
        }
        docLength=Math.sqrt(docLength);
        if(docLength==0 || arrClusterVectorLen[clusterID]==0)
            return 1;
        return 1-product/arrClusterVectorLen[clusterID]/docLength;
    }
}
