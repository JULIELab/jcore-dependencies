package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.*;
/**
 * <p>Abstract Smoother</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSmoother implements Smoother{
    protected boolean useLog;
    protected double queryWeight;
    protected boolean docFirstOptimal, querytermFirstOptimal;

    abstract protected double computeSmoothedProb(int termFreq);

    public boolean isDocFirstOptimal(){
        return docFirstOptimal;
    }
    public boolean isQueryTermFirstOptimal(){
        return querytermFirstOptimal;
    }

    public void setLogLikelihoodOption(boolean option){
        useLog=option;
    }

    public boolean getLogLikelihoodOption(){
        return useLog;
    }

    public double getSmoothedProb(int termFreq){
        return computeSmoothedProb(termFreq);
    }

    public double getSmoothedProb(IRDoc doc, int termFreq){
        setDoc(doc);
        return computeSmoothedProb(termFreq);
    }

    public double getSmoothedProb(IRDoc doc){
        setDoc(doc);
        return computeSmoothedProb(0);
    }

    public double getSmoothedProb(SimpleTermPredicate queryTerm, int termFreq){
        setQueryTerm(queryTerm);
        return computeSmoothedProb(termFreq);
    }

    public double getSmoothedProb(SimpleTermPredicate queryTerm){
        setQueryTerm(queryTerm);
        return computeSmoothedProb(0);
    }

    public double getSmoothedProb(IRDoc doc, SimpleTermPredicate queryTerm){
        setDoc(doc);
        setQueryTerm(queryTerm);
        return computeSmoothedProb(0);
    }

    public double getSmoothedProb(IRDoc doc, SimpleTermPredicate queryTerm, int termFreq){
        setDoc(doc);
        setQueryTerm(queryTerm);
        return computeSmoothedProb(termFreq);
    }

    protected double getProb(double originalProb){
        if(useLog){
            return Math.log(originalProb);
        }
        else
            return originalProb;
    }
}