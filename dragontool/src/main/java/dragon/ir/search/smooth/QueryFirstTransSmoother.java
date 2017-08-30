package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.SimpleTermPredicate;
import dragon.matrix.*;

/**
 * <p>A Smoother using relation-term translations</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class QueryFirstTransSmoother extends AbstractSmoother{
    private IndexReader srcIndexReader, destIndexReader;
    private DoubleSparseMatrix transMatrix;
    private Smoother basicSmoother;
    private boolean relationTrans;
    private double transCoefficient;
    private int docNum, arrDocCount[];
    private int curDocIndex;
    private double arrTrans[];

    public QueryFirstTransSmoother(IndexReader reader, DoubleSparseMatrix transposedTransMatrix, boolean relationTrans, double transCoefficient, Smoother basicSmoother) {
        this.srcIndexReader=reader;
        this.destIndexReader=reader;
        this.transMatrix=transposedTransMatrix;
        this.relationTrans=relationTrans;
        this.useLog =true;
        this.docFirstOptimal=false;
        this.querytermFirstOptimal=true;
        this.basicSmoother=basicSmoother;
        basicSmoother.setLogLikelihoodOption(false);
        this.transCoefficient =transCoefficient;

        docNum=destIndexReader.getCollection().getDocNum();
        arrTrans=new double[docNum];
        arrDocCount=new int[docNum];
        for(int i=0;i<docNum;i++){
            if(relationTrans)
                arrDocCount[i]=srcIndexReader.getDoc(i).getRelationCount();
            else
                arrDocCount[i]=srcIndexReader.getDoc(i).getTermCount();
            if(arrDocCount[i]<=0) arrDocCount[i]=1;
        }
    }

    public QueryFirstTransSmoother(IndexReader srcIndexReader, IndexReader destIndexReader, DoubleSparseMatrix transposedTransMatrix, double transCoefficient, Smoother basicSmoother) {
        if(srcIndexReader.getCollection().getDocNum()!=destIndexReader.getCollection().getDocNum())
            return;

        this.srcIndexReader=srcIndexReader;
        this.destIndexReader=destIndexReader;
        this.transMatrix=transposedTransMatrix;
        this.relationTrans=false;
        this.useLog =true;
        this.docFirstOptimal=false;
        this.querytermFirstOptimal=true;
        this.basicSmoother=basicSmoother;
        basicSmoother.setLogLikelihoodOption(false);
        this.transCoefficient =transCoefficient;

        docNum=destIndexReader.getCollection().getDocNum();
        arrTrans=new double[docNum];
        arrDocCount=new int[docNum];
        for(int i=0;i<docNum;i++){
            if(relationTrans)
                arrDocCount[i]=srcIndexReader.getDoc(i).getRelationCount();
            else
                arrDocCount[i]=srcIndexReader.getDoc(i).getTermCount();
            if(arrDocCount[i]<=0) arrDocCount[i]=1;
        }
    }

    public void setTranslationMatrix(DoubleSparseMatrix transposedTransMatrix){
        this.transMatrix = transposedTransMatrix;
    }

    public DoubleSparseMatrix getTranslationMatrix() {
        return transMatrix;
    }

    public boolean setParameters(double[] params){
        if(params!=null && params.length>=1)
        {
            transCoefficient=params[0];
            return true;
        }
        else
            return false;
    }

    public Smoother getBasicSmoother(){
        return basicSmoother;
    }

    public double getTranslationCoefficient() {
        return transCoefficient;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        Cell transCell;
        int[] arrIndex, arrFreq;
        int num;
        int j,k;

        this.queryWeight =queryTerm.getWeight();
        basicSmoother.setQueryTerm(queryTerm);
        for (j = 0; j < docNum; j++)  arrTrans[j]=0;

        num=transMatrix.getNonZeroNumInRow(queryTerm.getIndex());
        for(j=0;j<num;j++){
            transCell = transMatrix.getNonZeroCellInRow(queryTerm.getIndex(), j);
            if(relationTrans){
                arrIndex = srcIndexReader.getRelationDocIndexList(transCell.getColumn());
                arrFreq = srcIndexReader.getRelationDocFrequencyList(transCell.getColumn());
            }
            else{
                arrIndex = srcIndexReader.getTermDocIndexList(transCell.getColumn());
                arrFreq = srcIndexReader.getTermDocFrequencyList(transCell.getColumn());
            }
            for(k=0;k<arrIndex.length;k++){
                arrTrans[arrIndex[k]]+=transCell.getDoubleScore()*arrFreq[k]/arrDocCount[arrIndex[k]];
            }
        }
    }

    public void setDoc(IRDoc doc){
        curDocIndex=doc.getIndex();
        basicSmoother.setDoc(doc);
    }

    public double getTranslationProb(int docIndex){
        return arrTrans[docIndex];
    }

    protected double computeSmoothedProb(int termFrequency){
        return queryWeight*getProb((1-transCoefficient)*basicSmoother.getSmoothedProb(termFrequency)/queryWeight+transCoefficient*arrTrans[curDocIndex]);
    }
}
