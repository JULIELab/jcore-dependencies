package dragon.ir.clustering.clustermodel;

import dragon.ir.clustering.DocCluster;
import dragon.ir.clustering.featurefilter.FeatureFilter;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IRTerm;
import dragon.ir.index.IndexReader;
import dragon.ir.kngbase.KnowledgeBase;
import dragon.matrix.DoubleSparseMatrix;
import dragon.util.MathUtil;

/**
 * <p>Multinormial model based clustering</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class MultinomialClusterModel extends AbstractClusterModel{
    private static final int SMOOTH_LAPLACIAN=0;
    private static final int SMOOTH_BKG=1;
    private static final int SMOOTH_TRANS=2;

    private IndexReader indexReader;
    private IndexReader topicIndexReader;
    private DoubleSparseMatrix topicTransMatrix;
    private double[][] arrClusterModel;
    private double[] arrBkgModel;
    private double bkgCoefficient;
    private double transCoefficient;
    private int[] topicMap, termMap;
    private int featureNum;
    private int smoothingMethod;

    public MultinomialClusterModel(int clusterNum, IndexReader indexReader) {
        super(clusterNum);
        this.indexReader =indexReader;
        this.smoothingMethod =SMOOTH_LAPLACIAN;
        featureNum =indexReader.getCollection().getTermNum();
    }

    public MultinomialClusterModel(int clusterNum, IndexReader indexReader, double bkgCoefficient) {
        super(clusterNum);
        this.indexReader =indexReader;
        this.bkgCoefficient =bkgCoefficient;
        this.smoothingMethod =SMOOTH_BKG;
        featureNum =indexReader.getCollection().getTermNum();
    }

    public MultinomialClusterModel(int clusterNum, IndexReader indexReader, IndexReader topicIndexReader,
                                    DoubleSparseMatrix topicTransMatrix, double transCoefficient, double bkgCoefficient) {
        super(clusterNum);
        this.indexReader =indexReader;
        this.topicIndexReader =topicIndexReader;
        this.topicTransMatrix =topicTransMatrix;
        this.transCoefficient =transCoefficient;
        this.bkgCoefficient =bkgCoefficient;
        this.smoothingMethod =SMOOTH_TRANS;
        featureNum =indexReader.getCollection().getTermNum();
        topicMap=new int[topicIndexReader.getCollection().getTermNum()];
        for(int i=0;i<topicMap.length;i++)
            topicMap[i]=i;
        termMap=new int[indexReader.getCollection().getTermNum()];
        for(int i=0;i<termMap.length;i++)
            termMap[i]=i;
    }

    public MultinomialClusterModel(int clusterNum, IndexReader indexReader, IndexReader topicIndexReader,
                                    KnowledgeBase kngBase, double transCoefficient, double bkgCoefficient) {
        super(clusterNum);
        this.indexReader =indexReader;
        this.topicIndexReader =topicIndexReader;
        this.topicTransMatrix =kngBase.getKnowledgeMatrix();
        this.transCoefficient =transCoefficient;
        this.bkgCoefficient =bkgCoefficient;
        this.smoothingMethod =SMOOTH_TRANS;
        featureNum =indexReader.getCollection().getTermNum();

        //map topic signatures to the topics in the given knowledge base
        int i;
        topicMap=new int[topicIndexReader.getCollection().getTermNum()];
        for(i=0;i<topicMap.length;i++)
            topicMap[i]=kngBase.getRowKeyList().search(topicIndexReader.getTermKey(i));

        //map terms in the knowlege base to terms in the current dataset.
        IRTerm curTerm;
        termMap=new int[kngBase.getColumnKeyList().size()];
        for(i=0;i<termMap.length;i++){
            curTerm=indexReader.getIRTerm(kngBase.getColumnKeyList().search(i));
            if(curTerm==null)
                termMap[i]=-1;
            else
                termMap[i]=curTerm.getIndex();
        }
    }

    public double getTranslationCoefficient(){
        return transCoefficient;
    }

    public void setTranslationCoefficient(double transCoefficient){
        this.transCoefficient =transCoefficient;
    }

    public double getBackgroundCoefficient(){
        return bkgCoefficient;
    }

    public void setBackgroundCoefficient(double bkgCoefficient){
        this.bkgCoefficient =bkgCoefficient;
    }

    public void setFeatureFilter(FeatureFilter featureFilter){
        this.featureFilter =featureFilter;
        if(featureFilter!=null)
            featureNum=featureFilter.getSelectedFeatureNum();
        else
            featureNum =indexReader.getCollection().getTermNum();
    }

    public double getDistance(IRDoc doc, int clusterID){
        int arrIndex[], arrFreq[];
        double sum;
        int len, i, newIndex;

        arrIndex=indexReader.getTermIndexList(doc.getIndex());
        arrFreq=indexReader.getTermFrequencyList(doc.getIndex());
        if(arrIndex==null)
            len=0;
        else
            len=arrIndex.length;
        sum=0;
        for(i=0;i<len;i++){
            if(featureFilter==null)
                newIndex=arrIndex[i];
            else
                newIndex=featureFilter.map(arrIndex[i]);
            if(newIndex>=0)
                sum += arrFreq[i] * arrClusterModel[clusterID][newIndex];
        }
        return -sum;
    }

    public void setClusterNum(int clusterNum){
        this.clusterNum = clusterNum;
    }

    public void setDocCluster(DocCluster cluster) {
        IRDoc curDoc;
        int[] arrCount;
        int[] arrIndex, arrFreq;
        int i, j, len, newIndex;

        if(arrClusterModel==null || arrClusterModel.length!=clusterNum || arrClusterModel[0].length!=featureNum)
            arrClusterModel=new double[clusterNum][featureNum];
        arrCount=new int[featureNum];
        for(i=0;i<cluster.getDocNum();i++){
            curDoc=cluster.getDoc(i);
            arrIndex=indexReader.getTermIndexList(curDoc.getIndex());
            arrFreq=indexReader.getTermFrequencyList(curDoc.getIndex());
            if(arrIndex==null)
                len=0;
            else
                len=arrIndex.length;
            for(j=0;j<len;j++){
                if(featureFilter==null)
                    newIndex=arrIndex[j];
                else
                    newIndex=featureFilter.map(arrIndex[j]);
                if(newIndex>=0)
                    arrCount[newIndex] += arrFreq[j];
            }
        }

        if(smoothingMethod==SMOOTH_LAPLACIAN)
            laplacianSmoothing(arrCount,cluster.getClusterID());
        else if(smoothingMethod==SMOOTH_BKG)
            backgroundSmoothing(arrCount,cluster.getClusterID());
        else
            translationSmoothing(arrCount, computeTranslationModel(cluster),cluster.getClusterID());

    }

    private void translationSmoothing(int[] arrCount, double[] arrTransModel, int clusterID){
        double sum;
        double a, b, c;
        int i;

        if(arrBkgModel==null || arrBkgModel.length!=featureNum)
            arrBkgModel=getBackgroundModel(indexReader);
        sum=getSummation(arrCount);
        a=transCoefficient;
        b=(1-bkgCoefficient)*(1-transCoefficient)/sum;
        c=bkgCoefficient*(1-transCoefficient);

        for(i=0;i<featureNum;i++)
            arrClusterModel[clusterID][i]=Math.log(arrTransModel[i]*a+arrCount[i]*b+arrBkgModel[i]*c);
    }

    private double[] computeTranslationModel(DocCluster cluster){
        IRDoc curDoc;
        double[] arrScore, arrModel, arrSelectedModel;
        double sum, rate;
        int[] arrCount,arrIndex, arrFreq;
        int i, j, topicIndex,termIndex, termNum, topicNum, docNum;

        //compute counts of topic signatures
        topicNum=topicIndexReader.getCollection().getTermNum();
        arrCount=new int[topicNum];
        termNum=indexReader.getCollection().getTermNum();
        docNum=topicIndexReader.getCollection().getDocNum();
        for(i=0;i<cluster.getDocNum();i++){
            curDoc=cluster.getDoc(i);
            if(curDoc.getIndex()>=docNum) continue;

            arrIndex=topicIndexReader.getTermIndexList(curDoc.getIndex());
            arrFreq=topicIndexReader.getTermFrequencyList(curDoc.getIndex());
            if(arrIndex==null)
                continue;
            for(j=0;j<arrIndex.length;j++)
                arrCount[arrIndex[j]]+=arrFreq[j];
        }

        // check if the translation of the topic signature exists
        for(i=0;i<topicMap.length;i++){
            topicIndex=topicMap[i];
            if(topicIndex<0) //can not find the topic signature in the knowledge base
                arrCount[i]=0;
            else if(topicIndex>=topicTransMatrix.rows())
                arrCount[i]=0;
            else if(topicTransMatrix.getNonZeroNumInRow(topicIndex)<=0) // the translation does not exist
                arrCount[i]=0;
        }

        //topic signature translaiton
        sum=MathUtil.sumArray(arrCount);
        arrModel=new double[termNum];
        for(i=0;i<topicNum;i++){
            if(arrCount[i]<=0)
                continue;
            topicIndex=topicMap[i];
            rate=arrCount[i]/sum;
            arrIndex=topicTransMatrix.getNonZeroColumnsInRow(topicIndex);
            arrScore=topicTransMatrix.getNonZeroDoubleScoresInRow(topicIndex);
            for(j=0;j<arrIndex.length;j++){
                termIndex=termMap[arrIndex[j]];
                if(termIndex>=0)
                    arrModel[termIndex] += rate*arrScore[j];
            }
        }

        //map to selected features
        if(arrModel.length==featureFilter.getSelectedFeatureNum())
            return arrModel;

        arrSelectedModel=new double[featureFilter.getSelectedFeatureNum()];
        sum=0;
        for(i=0;i<arrModel.length;i++){
            termIndex=featureFilter.map(i);
            if(termIndex>=0){
                sum+=arrModel[i];
                arrSelectedModel[termIndex]=arrModel[i];
            }
        }
        for(i=0;i<arrSelectedModel.length;i++)
            arrSelectedModel[i]=arrSelectedModel[i]/sum;
        return arrSelectedModel;
    }

    private void backgroundSmoothing(int[] arrCount, int clusterID){
        double sum;
        int i;

        if(arrBkgModel==null || arrBkgModel.length!=featureNum)
            arrBkgModel=getBackgroundModel(indexReader);
        sum=getSummation(arrCount);
        for(i=0;i<featureNum;i++)
            arrClusterModel[clusterID][i]=Math.log(arrCount[i]/sum*(1-bkgCoefficient)+bkgCoefficient*arrBkgModel[i]);
    }

    private void laplacianSmoothing(int[] arrCount, int clusterID){
        double sum;
        int i;

        sum=getSummation(arrCount)+featureNum;
        for(i=0;i<featureNum;i++)
            arrClusterModel[clusterID][i]=Math.log((arrCount[i]+1.0)/sum);
    }

    private double getSummation(int[] arrCount){
        long sum;
        int i;

        sum=0;
        for(i=0;i<arrCount.length;i++)
            sum+=arrCount[i];
        return sum;
    }

    private double[] getBackgroundModel(IndexReader reader){
        double[] arrModel;
        double sum;
        int termNum, newIndex,i;

        termNum=reader.getCollection().getTermNum();
        sum=0;
        arrModel=new double[featureNum];
        for(i=0;i<termNum;i++){
            if(featureFilter==null)
                newIndex=i;
            else
                newIndex=featureFilter.map(i);
            if(newIndex>=0){
                arrModel[newIndex] = reader.getIRTerm(i).getFrequency();
                sum+=arrModel[newIndex];
            }
        }
        for(i=0;i<featureNum;i++)
            arrModel[i]=arrModel[i]/sum;
        return arrModel;
    }
}