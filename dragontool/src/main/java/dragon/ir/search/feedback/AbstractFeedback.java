package dragon.ir.search.feedback;

import dragon.ir.index.IRTerm;
import dragon.ir.query.*;
import dragon.ir.search.Searcher;
import dragon.nlp.compare.IndexComparator;
import dragon.nlp.compare.WeightComparator;
import dragon.util.SortedArray;

import java.util.ArrayList;
/**
 * <p>Abstract Feedback</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractFeedback implements Feedback{
    protected int feedbackDocNum;
    protected Searcher searcher;
    protected double feedbackCoeffi;

    abstract protected ArrayList estimateNewQueryModel(IRQuery oldQuery);

    public AbstractFeedback(Searcher searcher, int feedbackDocNum, double feedbackCoeffi) {
        this.searcher =searcher;
        this.feedbackDocNum =feedbackDocNum;
        this.feedbackCoeffi =feedbackCoeffi;
    }

    public int getFeedbackDocNum(){
        return feedbackDocNum;
    }

    public void setFeedbackDocNum(int docNum){
        this.feedbackDocNum =docNum;
    }

    public Searcher getSearcher(){
        return searcher;
    }

    public void setSearcher(Searcher searcher){
        this.searcher =searcher;
    }

    public IRQuery updateQueryModel(IRQuery oldQuery){
        SortedArray oldTermList;
        ArrayList newTermList;
        RelSimpleQuery query;
        SimpleTermPredicate curPredicate, oldPredicate;
        int i, termNum;
        double weightSum;

        newTermList=estimateNewQueryModel(oldQuery);
        if(newTermList==null || newTermList.size()==0)
            return oldQuery;

        termNum=newTermList.size();
        oldTermList=new SortedArray(new IndexComparator());
        weightSum=0;
        for(i=0;i<oldQuery.getChildNum();i++){
            curPredicate=(((SimpleTermPredicate) oldQuery.getChild(i))).copy();
            if(curPredicate.getDocFrequency()>0){
                oldTermList.add(curPredicate);
                weightSum+=curPredicate.getWeight();
            }
        }
        for(i=0;i<oldTermList.size();i++){
            curPredicate=(SimpleTermPredicate)oldTermList.get(i);
            curPredicate.setWeight(curPredicate.getWeight()/weightSum*(1 - feedbackCoeffi));
        }

        for(i=0;i<newTermList.size();i++){
            curPredicate=(SimpleTermPredicate)newTermList.get(i);
            curPredicate.setWeight(curPredicate.getWeight()*feedbackCoeffi);
            if(!oldTermList.add(curPredicate)){
                oldPredicate=(SimpleTermPredicate) oldTermList.get(oldTermList.insertedPos());
                oldPredicate.setWeight(oldPredicate.getWeight()+curPredicate.getWeight());
            }
        }
        oldTermList.setComparator(new WeightComparator(true));

        weightSum=0;
        for(i=0;i<termNum;i++)
            weightSum+=((SimpleTermPredicate)oldTermList.get(i)).getWeight();

        query=new RelSimpleQuery();
        for(i=0;i<termNum;i++){
            curPredicate=(SimpleTermPredicate)oldTermList.get(i);
            curPredicate.setWeight(curPredicate.getWeight()/weightSum);
            query.add(curPredicate);
        }
        return query;
    }

    protected IRTerm buildIRTerm(SimpleTermPredicate predicate){
        IRTerm cur;

        cur=new IRTerm(predicate.getIndex(),predicate.getFrequency(),predicate.getDocFrequency());
        cur.setKey(predicate.getKey());
        return cur;
    }

    protected SimpleTermPredicate buildSimpleTermPredicate(int termIndex, double queryWeight){
        SimpleTermPredicate predicate;
        IRTerm curTerm;

        curTerm = searcher.getIndexReader().getIRTerm(termIndex);
        predicate = new SimpleTermPredicate(new SimpleExpression("TERM", new Operator("="), curTerm.getKey()));
        predicate.setWeight(queryWeight);
        predicate.setFrequency(curTerm.getFrequency());
        predicate.setDocFrequency(curTerm.getDocFrequency());
        predicate.setIndex(termIndex);
        return predicate;
    }

}