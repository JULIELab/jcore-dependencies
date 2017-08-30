package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.SimpleTermPredicate;
/**
 * <p>Okapi Smoother</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OkapiSmoother extends AbstractSmoother{
    private int docNum;
    private double curTermIDF, curDocLengthRatio;
    private double avgDocLength;
    private double bm25k1, bm25b;
    private double param1, param2;

    public OkapiSmoother(IRCollection collection) {
        docNum=collection.getDocNum();
        avgDocLength=collection.getTermCount()*1.0/docNum;
        this.bm25b=0.75;
        this.bm25k1 =2;
        this.useLog=false;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
        param1=bm25k1*(1-bm25b);
        param2=bm25k1*bm25b;
    }

    public OkapiSmoother(IRCollection collection, double bm25k1, double bm25b) {
        docNum=collection.getDocNum();
        avgDocLength=collection.getTermCount()*1.0/docNum;
        this.bm25b=bm25b;
        this.bm25k1 =bm25k1;
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
        curTermIDF=Math.log((docNum-queryTerm.getDocFrequency()+0.5)/(queryTerm.getDocFrequency()+0.5));
    }

    public void setDoc(IRDoc doc){
        curDocLengthRatio=param2*doc.getTermCount()/avgDocLength;
    }

    protected double computeSmoothedProb(int termFrequency){
        //we did not use bm25k3 in this implementation, ie., (s3*qtf/(k3+qtf) is implemented as qtf own.
        //query weight is equivalent to query term frequency (qtf);
        return queryWeight*termFrequency*curTermIDF/(param1+curDocLengthRatio+termFrequency);
    }
}