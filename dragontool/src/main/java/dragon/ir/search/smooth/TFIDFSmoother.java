package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.SimpleTermPredicate;
/**
 * <p>TF-IDF Smoother for vector space model</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TFIDFSmoother extends AbstractSmoother{
    private int docNum;
    private double curTermIDF, curDocLengthRatio;
    private double avgDocLength;
    private double bm25k1, bm25b;
    private double param1, param2;
    private boolean useBM25;

    public TFIDFSmoother(IRCollection collection) {
        docNum=collection.getDocNum();
        this.useLog=false;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
        this.useBM25 =false;
    }

    public TFIDFSmoother(IRCollection collection, double bm25k1, double bm25b) {
        docNum=collection.getDocNum();
        avgDocLength=collection.getTermCount()*1.0/docNum;
        this.bm25b=bm25b;
        this.bm25k1 =bm25k1;
        this.useBM25 =true;
        this.useLog=false;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
        param1=bm25k1*(1-bm25b);
        param2=bm25k1*bm25b;
    }

    public boolean setParameters(double[] params){
        if(params!=null && params.length>=2)
        {
            this.bm25k1 =params[0];
            this.bm25b =params[1];
            param1=bm25k1*(1-bm25b);
            param2=bm25k1*bm25b;
            return true;
        }
        else
            return false;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        this.queryWeight =queryTerm.getWeight();
        curTermIDF=Math.log((1+docNum)/(0.5+queryTerm.getDocFrequency()));
    }

    public void setDoc(IRDoc doc){
        if(useBM25)
            curDocLengthRatio=param2*doc.getTermCount()/avgDocLength;
    }

    protected double computeSmoothedProb(int termFrequency){
        if(useBM25)
            return queryWeight*termFrequency*curTermIDF/(param1+curDocLengthRatio+termFrequency);
        else
            return queryWeight*termFrequency*curTermIDF;
    }
}
