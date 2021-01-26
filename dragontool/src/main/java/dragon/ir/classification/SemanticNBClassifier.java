package dragon.ir.classification;

import dragon.ir.classification.featureselection.NullFeatureSelector;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IRTerm;
import dragon.ir.index.IndexReader;
import dragon.ir.kngbase.KnowledgeBase;
import dragon.matrix.DoubleFlatDenseMatrix;
import dragon.matrix.DoubleSparseMatrix;
import dragon.util.MathUtil;

/**
 * <p>Naive Bayesian Classifier with Semantic Smoothing or Background Smoothing</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SemanticNBClassifier extends NBClassifier {
    private IndexReader topicIndexReader;
    private DoubleSparseMatrix topicTransMatrix;
    private double transCoefficient, bkgCoefficient;
    private int[] topicMap, termMap;

    public SemanticNBClassifier(String modelFile){
    	super(modelFile);
    }

    public SemanticNBClassifier(IndexReader indexReader,double bkgCoefficient) {
        super(indexReader);
        this.topicIndexReader =null;
        this.topicTransMatrix =null;
        this.transCoefficient =0;
        this.bkgCoefficient =bkgCoefficient;
        this.featureSelector =new NullFeatureSelector();
    }

    public SemanticNBClassifier(IndexReader indexReader, IndexReader topicIndexReader,
                                DoubleSparseMatrix topicTransMatrix, double transCoefficient, double bkgCoefficient) {
        super(indexReader);
        this.featureSelector =new NullFeatureSelector();
        this.topicIndexReader =topicIndexReader;
        this.topicTransMatrix =topicTransMatrix;
        this.transCoefficient =transCoefficient;
        this.bkgCoefficient =bkgCoefficient;
        topicMap=new int[topicIndexReader.getCollection().getTermNum()];
        for(int i=0;i<topicMap.length;i++)
            topicMap[i]=i;
        termMap=new int[indexReader.getCollection().getTermNum()];
        for(int i=0;i<termMap.length;i++)
            termMap[i]=i;

    }

    public SemanticNBClassifier(IndexReader indexReader, IndexReader topicIndexReader,
                                KnowledgeBase kngBase, double transCoefficient, double bkgCoefficient) {
        super(indexReader);
        this.featureSelector =new NullFeatureSelector();
        this.topicIndexReader =topicIndexReader;
        this.topicTransMatrix =kngBase.getKnowledgeMatrix();
        this.transCoefficient =transCoefficient;
        this.bkgCoefficient =bkgCoefficient;

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

    public void train(DocClassSet trainingDocSet){
        DocClass cur;
        IRDoc curDoc;
        double[] bkgModel, transModel;
        int[] arrIndex, arrFreq;
        int i, j, k, classSum, newTermIndex;
        double a, b;

        if(indexReader==null && doctermMatrix==null)
        	return;

        classPrior=getClassPrior(trainingDocSet);
        featureSelector.train(indexReader,trainingDocSet);
        arrLabel=new String[trainingDocSet.getClassNum()];
        for(i=0;i<trainingDocSet.getClassNum();i++)
            arrLabel[i]=trainingDocSet.getDocClass(i).getClassName();
        model=new DoubleFlatDenseMatrix(trainingDocSet.getClassNum(),featureSelector.getSelectedFeatureNum());
        bkgModel=getBackgroundModel(indexReader);
        for(i=0;i<trainingDocSet.getClassNum();i++){
            classSum=0;
            cur=trainingDocSet.getDocClass(i);
            for(j=0;j<cur.getDocNum();j++){
                curDoc=cur.getDoc(j);
                arrIndex=indexReader.getTermIndexList(curDoc.getIndex());
                arrFreq=indexReader.getTermFrequencyList(curDoc.getIndex());
                for(k=0;k<arrIndex.length;k++){
                    newTermIndex=featureSelector.map(arrIndex[k]);
                    if(newTermIndex>=0){
                        classSum+=arrFreq[k];
                        model.add(i,newTermIndex,arrFreq[k]);
                    }
                }
            }

            if(topicTransMatrix!=null){
                transModel = computeTranslationModel(cur);
                a = (1 - bkgCoefficient) * (1 - transCoefficient) / classSum;
                b = (1 - transCoefficient) * bkgCoefficient;
                for (k = 0; k < model.columns(); k++)
                    // attention: log is used
                    model.setDouble(i, k,Math.log(transModel[k] * transCoefficient + model.getDouble(i, k) * a+bkgModel[k] * b));
            }
            else{
                a = (1 - bkgCoefficient)/classSum;
                for (k = 0; k < model.columns(); k++)
                    // attention: log is used
                    model.setDouble(i, k,Math.log(model.getDouble(i, k)*a + bkgModel[k] * bkgCoefficient));
            }
        }
    }

    private double[] computeTranslationModel(DocClass curClass){
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
        for(i=0;i<curClass.getDocNum();i++){
            curDoc=curClass.getDoc(i);
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
        if(arrModel.length==featureSelector.getSelectedFeatureNum())
            return arrModel;

        arrSelectedModel=new double[featureSelector.getSelectedFeatureNum()];
        sum=0;
        for(i=0;i<arrModel.length;i++){
            termIndex=featureSelector.map(i);
            if(termIndex>=0){
                sum+=arrModel[i];
                arrSelectedModel[termIndex]=arrModel[i];
            }
        }
        for(i=0;i<arrSelectedModel.length;i++)
            arrSelectedModel[i]=arrSelectedModel[i]/sum;
        return arrSelectedModel;
    }

    private double[] getBackgroundModel(IndexReader reader){
        double[] arrModel;
        double sum;
        int termNum, featureNum, newIndex,i;

        termNum=reader.getCollection().getTermNum();
        featureNum=featureSelector.getSelectedFeatureNum();
        sum=0;
        arrModel=new double[featureNum];
        for(i=0;i<termNum;i++){
            newIndex=featureSelector.map(i);
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