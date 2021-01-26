package dragon.ir.clustering;

import dragon.ir.clustering.clustermodel.ClusterModel;
import dragon.ir.clustering.clustermodel.MultinomialClusterModel;
import dragon.ir.index.IRDoc;
import dragon.matrix.DoubleDenseMatrix;
import dragon.matrix.DoubleFlatDenseMatrix;
import dragon.matrix.SparseMatrix;
import dragon.matrix.vector.DoubleVector;
import dragon.util.MathUtil;

/**
 * <p>Link-based K-Means Clustering Algorithm</p>
 * <p>The Link-based K-Means also incorporates pairwise object relationships into the clustering in addition to
 * tradidtional local features such as terms</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LinkKMean extends AbstractClustering{
    private Clustering initClustering;
    private SparseMatrix inLinks, outLinks;
    private ClusterModel distMetric;
    private int maxIteration;
    private boolean useWeight;

    /**
     * The constructor with symmetric pairwise object relationship matrix, i.e. the linkage btween objects is undirected.
     * @param initClustering the clustering method for initialization
     * @param links pairwise object relationship matrix. The matrix should be symmetric.
     */
    public LinkKMean(Clustering initClustering, SparseMatrix links){
        this(initClustering, links, null);
    }

    /**
     * The constructor with asymmetric pairwise object relationship matrix, i.e. the linkage btween objects is directed.
     * @param initClustering the clustering method for initialization
     * @param outLinks pairwise object relationship matrix.  Each row in this matrix stands for the linkages from the corressponding
     * object to other objects.
     * @param inLinks pairwise object relationship matrix.  It is the transposed matrix of the out link matrix. Each row in this
     * matrix stands for the linkages from other objects to the object the row corresponds to.
     */
    public LinkKMean(Clustering initClustering, SparseMatrix outLinks, SparseMatrix inLinks) {
        super(initClustering.getIndexReader());
        this.initClustering =initClustering;
        this.inLinks =inLinks;
        this.outLinks =outLinks;
        this.clusterNum=initClustering.getClusterNum();
        // using the multinomial cluster model with background smoothing
        distMetric=new MultinomialClusterModel(clusterNum,indexReader,0.5);
    }

    public void setUseWeight(boolean useWeight){
        this.useWeight=useWeight;
    }

    public boolean getUseWeight(){
        return useWeight;
    }

    protected boolean initialize(IRDoc[] arrDoc){
        initClustering.setRandomSeed(this.randomSeed);
        initClustering.cluster(arrDoc);
        distMetric.setFeatureFilter(initClustering.getFeatureFilter());
        clusterSet=initClustering.getClusterSet();
        return true;
    }

    public boolean cluster(IRDoc[] arrDoc){
        DoubleDenseMatrix transMatrix;
        DoubleVector probVector;
        double[] arrScore, inNeighbor, outNeighbor;
        int[] arrDocLabel, arrIndex;
        int movingObj, curCluster, oldCluster,iteration;
        int i,j, label;

        //initialization
        probVector=new DoubleVector(clusterNum);
        movingObj =arrDoc.length;
        iteration = 0;
        arrDocLabel=new int[indexReader.getCollection().getDocNum()];
        outNeighbor=new double[clusterNum];
        if(inLinks!=null)
            inNeighbor=new double[clusterNum];
        else
            inNeighbor=null;

        if(!initialize(arrDoc))
           return false;

        //loop
        while(movingObj>0 && iteration<maxIteration){
            if(showProgress){
                System.out.print((new java.util.Date()).toString()+" "+iteration++);
                System.out.print(" ");
                System.out.println(movingObj);
            }

            distMetric.setDocClusters(clusterSet);
            MathUtil.initArray(arrDocLabel,-1);
            for(i=0;i<arrDoc.length;i++)
                arrDocLabel[arrDoc[i].getIndex()]=arrDoc[i].getCategory();
            transMatrix=estimateClassTransferProb(arrDoc, arrDocLabel);

            movingObj=0;
            //assign objects to new cluster
            for (i = 0; i < arrDoc.length; i++) {
                MathUtil.initArray(outNeighbor,0);
                arrIndex=outLinks.getNonZeroColumnsInRow(arrDoc[i].getIndex());
                if(useWeight)
                    arrScore = outLinks.getNonZeroDoubleScoresInRow(arrDoc[i].getIndex());
                else
                    arrScore=null;
                if(arrIndex!=null){
                    for (j = 0; j < arrIndex.length; j++){
                        label = arrDocLabel[arrIndex[j]];
                        if (label >= 0){
                            if(!useWeight)
                                outNeighbor[label]++;
                            else
                                outNeighbor[label]+=arrScore[j];
                        }
                    }
                }
                if(inLinks!=null){
                    MathUtil.initArray(inNeighbor,0);
                    arrIndex = inLinks.getNonZeroColumnsInRow(arrDoc[i].getIndex());
                    if(useWeight)
                        arrScore = inLinks.getNonZeroDoubleScoresInRow(arrDoc[i].getIndex());
                    else
                        arrScore=null;
                    if (arrIndex != null) {
                        for (j = 0; j < arrIndex.length; j++){
                            label = arrDocLabel[arrIndex[j]];
                            if (label >= 0){
                                if(!useWeight)
                                    inNeighbor[label]++;
                                else
                                    inNeighbor[label]+=arrScore[j];
                            }
                        }
                    }
                }

                for(j=0;j<clusterNum;j++)
                    probVector.set(j,getLogLikelihood(arrDoc[i],j,transMatrix,outNeighbor,inNeighbor));
                curCluster=probVector.getDimWithMaxValue();
                oldCluster=arrDoc[i].getCategory();
                if(curCluster!=oldCluster)
                {
                    clusterSet.removeDoc(oldCluster,arrDoc[i]);
                    clusterSet.addDoc(curCluster,arrDoc[i]);
                    movingObj++;
                }
            }
        }
        return true;
    }

    public int getMaxIteration(){
        return maxIteration;
    }

    public void setMaxIteration(int iteration){
        this.maxIteration =iteration;
    }

    protected double getLogLikelihood(IRDoc doc, int clusterID, DoubleDenseMatrix transMatrix, double[] arrOutLinks, double[] arrInLinks){
        double sum;
        int i;

        sum=-distMetric.getDistance(doc, clusterID);
        for(i=0;i<clusterNum;i++)
            sum+=arrOutLinks[i]*transMatrix.getDouble(clusterID,i);
        if(arrInLinks!=null){
            for(i=0;i<clusterNum;i++)
                sum+=arrInLinks[i]*transMatrix.getDouble(i, clusterID);
        }
        return sum;
    }

    protected DoubleDenseMatrix estimateClassTransferProb(IRDoc[] arrDoc, int[] arrDocLabel){
        DoubleFlatDenseMatrix matrix;
        double sum, arrScore[];
        int[] arrIndex;
        int i,j, startLabel, endLabel;

        matrix=new DoubleFlatDenseMatrix(clusterNum,clusterNum);
        matrix.assign(1); //laplacian smoothing
        for(i=0;i<arrDoc.length;i++){
            startLabel=arrDocLabel[arrDoc[i].getIndex()];
            arrIndex=outLinks.getNonZeroColumnsInRow(arrDoc[i].getIndex());
            if(arrIndex==null) continue;
            if(useWeight)
                arrScore = outLinks.getNonZeroDoubleScoresInRow(arrDoc[i].getIndex());
            else
                arrScore=null;
            for(j=0;j<arrIndex.length;j++){
                endLabel=arrDocLabel[arrIndex[j]];
                if(endLabel>=0){
                    if(!useWeight)
                        matrix.add(startLabel,endLabel,1);
                    else
                        matrix.add(startLabel,endLabel,arrScore[j]);
                }
            }
        }

        for(i=0;i<clusterNum;i++){
            sum=matrix.getRowSum(i);
            for(j=0;j<clusterNum;j++)
                matrix.setDouble(i,j,Math.log(matrix.getDouble(i,j)/sum));
        }

        return matrix;
    }
}