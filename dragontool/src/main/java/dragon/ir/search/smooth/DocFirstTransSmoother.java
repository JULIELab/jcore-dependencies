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

public class DocFirstTransSmoother extends AbstractSmoother{
    private IndexReader srcIndexReader, destIndexReader;
    private DoubleSparseMatrix transMatrix;
    private Smoother basicSmoother;
    private double transCoefficient;
    private boolean relationTrans;
    private int curQueryTermIndex;
    private int docSignatureCount;
    private int[] arrIndex, arrFreq;

    public DocFirstTransSmoother(IndexReader indexReader, DoubleSparseMatrix transMatrix, boolean relationTrans, double transCoefficient, Smoother basicSmoother) {
        this.srcIndexReader=indexReader;
        this.destIndexReader =indexReader;
        this.transMatrix=transMatrix;
        this.basicSmoother =basicSmoother;
        basicSmoother.setLogLikelihoodOption(false);
        this.relationTrans=relationTrans;
        this.useLog =true;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=false;
        this.transCoefficient =transCoefficient;
    }

    public DocFirstTransSmoother(IndexReader srcIndexReader, IndexReader destIndexReader, DoubleSparseMatrix transMatrix, double transCoefficient, Smoother basicSmoother) {
        this.srcIndexReader=srcIndexReader;
        this.destIndexReader =destIndexReader;
        this.transMatrix=transMatrix;
        this.basicSmoother =basicSmoother;
        basicSmoother.setLogLikelihoodOption(false);
        this.relationTrans=false;
        this.useLog =true;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=false;
        this.transCoefficient =transCoefficient;
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

    public void setTranslationMatrix(DoubleSparseMatrix transMatrix){
        this.transMatrix =transMatrix;
    }

    public DoubleSparseMatrix getTranslationMatrix(){
        return transMatrix;
    }

    public Smoother getBasicSmoother(){
        return basicSmoother;
    }

    public double getTranslationCoefficient() {
        return transCoefficient;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        this.queryWeight =queryTerm.getWeight();
        curQueryTermIndex=queryTerm.getIndex();
        basicSmoother.setQueryTerm(queryTerm);
    }

    public void setDoc(IRDoc curDoc){
        IRDoc srcDoc;

        basicSmoother.setDoc(curDoc);
        if(srcIndexReader.equals(destIndexReader))
            srcDoc=curDoc;
        else
             srcDoc=srcIndexReader.getDoc(curDoc.getIndex());
        if(relationTrans){
            arrIndex = srcIndexReader.getRelationIndexList(curDoc.getIndex());
            arrFreq=srcIndexReader.getRelationFrequencyList(curDoc.getIndex());
            docSignatureCount=srcDoc.getRelationCount();
            if (docSignatureCount <= 0)  docSignatureCount = 1;
        }
        else{
            arrIndex=srcIndexReader.getTermIndexList(curDoc.getIndex());
            arrFreq=srcIndexReader.getTermFrequencyList(curDoc.getIndex());
            docSignatureCount=srcDoc.getTermCount();
            if (docSignatureCount <= 0)  docSignatureCount = 1;
        }
    }

    public double getTranslationProb(int termIndex){
        if(transMatrix==null)
            return getSelfTransProb(termIndex);
        else
            return getFullTransProb(termIndex);
    }

    protected double computeSmoothedProb(int termFrequency){
        double prob;

        if(transMatrix==null)
            prob=getSelfTransProb(curQueryTermIndex);
        else
            prob=getFullTransProb(curQueryTermIndex);
        prob=transCoefficient*prob+(1-transCoefficient)*basicSmoother.getSmoothedProb(termFrequency)/queryWeight;
        return queryWeight*getProb(prob);
    }

    private double getFullTransProb(int queryTermIndex){
        int i;
        double prob;

        prob=0;
        for(i=0;arrIndex!=null && i<arrIndex.length; i++){
            prob+=transMatrix.getDouble(arrIndex[i],queryTermIndex)*arrFreq[i]/docSignatureCount;
        }
        return prob;
    }

    private double getSelfTransProb(int queryTermIndex){
        IRRelation curRelation;
        int i, count;

        count=0;
        for(i=0;arrIndex!=null && i<arrIndex.length; i++){
            curRelation=srcIndexReader.getIRRelation(arrIndex[i]);
            if(curRelation.getFirstTerm()==queryTermIndex || curRelation.getSecondTerm()==queryTermIndex)
                count+=arrFreq[i];
        }
        return 0.5*count/docSignatureCount;
    }
}