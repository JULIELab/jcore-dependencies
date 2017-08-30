package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.SimpleTermPredicate;
/**
 * <p>Two-stage smoother</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TwoStageSmoother extends AbstractSmoother implements Smoother{
    private double bkgCoefficient, dirichletCoefficient;
    private int docTermCount;
    private long collectionTermCount;
    private double docCollectionProb, queryBackgroundProb;

    public TwoStageSmoother(IRCollection collection, double bkgCoefficient, double dirichletCoefficient) {
        collectionTermCount=collection.getTermCount();
        this.bkgCoefficient =bkgCoefficient;
        this.dirichletCoefficient =dirichletCoefficient;
        this.useLog =true;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
    }

    public boolean setParameters(double[] params){
        if(params!=null && params.length>=2)
        {
            bkgCoefficient=params[0];
            dirichletCoefficient=params[1];
            return true;
        }
        else
            return false;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        this.queryWeight =queryTerm.getWeight();
        docCollectionProb=dirichletCoefficient* queryTerm.getFrequency()/collectionTermCount;
        queryBackgroundProb=bkgCoefficient* queryTerm.getFrequency()/collectionTermCount;
    }

    public void setDoc(IRDoc doc){
        docTermCount=doc.getTermCount();
    }

    protected double computeSmoothedProb(int termFreq){
        return queryWeight*getProb((1-bkgCoefficient)*(termFreq+docCollectionProb)/(docTermCount+dirichletCoefficient)+queryBackgroundProb);
    }
}
