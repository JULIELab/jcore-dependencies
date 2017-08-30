package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.SimpleTermPredicate;
/**
 * <p>Linear interpolation smoother</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class JMSmoother extends AbstractSmoother implements Smoother{
    private double bkgCoefficient;
    private int docTermCount;
    private long collectionTermCount;
    private double collectionProb;

    public JMSmoother(IRCollection collection, double bkgCoefficient) {
        collectionTermCount=collection.getTermCount();
        this.bkgCoefficient =bkgCoefficient;
        this.useLog =true;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
    }

    public boolean setParameters(double[] params){
        if(params!=null && params.length>=1)
        {
            bkgCoefficient=params[0];
            return true;
        }
        else
            return false;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        this.queryWeight =queryTerm.getWeight();
        collectionProb=bkgCoefficient* queryTerm.getFrequency()/collectionTermCount;
    }

    public void setDoc(IRDoc doc){
        docTermCount=doc.getTermCount();
        if(docTermCount<=0) docTermCount=1;
    }

    protected double computeSmoothedProb(int termFreq){
        return queryWeight*getProb(termFreq*(1-bkgCoefficient)/docTermCount+collectionProb);
    }
}