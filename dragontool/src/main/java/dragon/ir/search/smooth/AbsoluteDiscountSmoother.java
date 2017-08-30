package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.SimpleTermPredicate;
/**
 * <p>Absolute Discount Smoother</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AbsoluteDiscountSmoother extends AbstractSmoother{
    private long collectionTermCount;
    private double absoluteDiscount;
    private double collectionProb;
    private double delta;
    private int docTermCount;

    public AbsoluteDiscountSmoother(IRCollection collection, double absoluteDiscount) {
        collectionTermCount=collection.getTermCount();
        this.absoluteDiscount =absoluteDiscount;
        this.useLog =true;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
    }

    public boolean setParameters(double[] params){
        if(params!=null && params.length>=1)
        {
            absoluteDiscount=params[0];
            return true;
        }
        else
            return false;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        this.queryWeight =queryTerm.getWeight();
        collectionProb=((double)queryTerm.getFrequency())/collectionTermCount;
    }

    public void setDoc(IRDoc doc){
        docTermCount=doc.getTermCount();
        if(docTermCount<=0) docTermCount=1;
        delta=absoluteDiscount*doc.getTermNum()/docTermCount;
    }

    protected double computeSmoothedProb(int termFreq){
        if(termFreq>absoluteDiscount)
            return queryWeight*getProb((termFreq-absoluteDiscount)/docTermCount+delta*collectionProb);
        else
            return queryWeight*getProb(delta*collectionProb);
    }
}
