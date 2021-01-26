package dragon.ir.search.smooth;

import dragon.ir.index.IRCollection;
import dragon.ir.index.IRDoc;
import dragon.ir.query.SimpleTermPredicate;

/**
 * <p>Pivoted Normalization Smoother</p>
 * <p>Smoother for Vector Space Model </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class PivotedNormSmoother extends AbstractSmoother{
    private int docTermCount;
    private int docNum;
    private double s, avgDocLength;
    private double idf;

    public PivotedNormSmoother(IRCollection collection) {
        useLog=false;
        s=0.20;
        docNum=collection.getDocNum();
        avgDocLength=collection.getTermCount()*1.0/docNum;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
    }

    public PivotedNormSmoother(IRCollection collection,double s) {
        useLog=false;
        this.s=s;
        docNum=collection.getDocNum();
        avgDocLength=collection.getTermCount()*1.0/docNum;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
    }

    public boolean setParameters(double[] params){
        s=params[0];
        return true;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        this.queryWeight =queryTerm.getWeight();
        idf=Math.log((docNum+1.0)/queryTerm.getDocFrequency());
    }

    public void setDoc(IRDoc doc){
        docTermCount=doc.getTermCount();
    }

    protected double computeSmoothedProb(int termFrequency){
        return queryWeight*(1+Math.log(1+Math.log(termFrequency)))*idf/(1-s+s*docTermCount/avgDocLength);
    }
}