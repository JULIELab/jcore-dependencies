package dragon.ir.search.smooth;

import dragon.ir.index.IRCollection;
import dragon.ir.index.IRDoc;
import dragon.ir.query.SimpleTermPredicate;

/**
 * <p>Dirichlet Smoother</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DirichletSmoother extends AbstractSmoother{
    private long collectionTermCount;
    private double dirichletCoeffi;
    private double collectionProb;
    private int docTermCount;

    public DirichletSmoother(IRCollection collection, double dirichletCoeffi) {
        collectionTermCount=collection.getTermCount();
        this.dirichletCoeffi =dirichletCoeffi;
        this.useLog =true;
        this.docFirstOptimal=true;
        this.querytermFirstOptimal=true;
    }

    public boolean setParameters(double[] params){
        if(params!=null && params.length>=1)
        {
            dirichletCoeffi=params[0];
            return true;
        }
        else
            return false;
    }

    public void setQueryTerm(SimpleTermPredicate queryTerm){
        this.queryWeight =queryTerm.getWeight();
        collectionProb=dirichletCoeffi* queryTerm.getFrequency()/collectionTermCount;
    }

    public void setDoc(IRDoc doc){
        docTermCount=doc.getTermCount();
    }

    protected double computeSmoothedProb(int termFreq){
        return queryWeight*getProb((termFreq+collectionProb)/(docTermCount+dirichletCoeffi));
    }
}