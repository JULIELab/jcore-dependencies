package dragon.ir.search;

import dragon.ir.index.*;
import dragon.ir.query.*;
import dragon.ir.search.smooth.Smoother;
import java.util.*;

/**
 * <p>Abstract class of Searcher</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSearcher implements Searcher{
    protected IndexReader indexReader;
    protected ArrayList hitlist;
    protected IRQuery query;
    protected Smoother smoother;
    private boolean queryWeighting;

    public AbstractSearcher(IndexReader indexReader, Smoother smoother) {
        this.indexReader=indexReader;
        this.smoother =smoother;
        this.queryWeighting =true;
    }

    protected SimpleTermPredicate[] checkSimpleTermQuery(RelSimpleQuery query){
        SimpleTermPredicate predicate, arrPredicate[];
        IRTerm curIRTerm;
        ArrayList list;
        int i;

        list=new ArrayList();
        for(i=0;i<query.getChildNum();i++){
            if(((Predicate)query.getChild(i)).isTermPredicate()){
                predicate = (SimpleTermPredicate) query.getChild(i);
                if (predicate.getDocFrequency()<=0) {
                    curIRTerm=indexReader.getIRTerm(predicate.getKey());
                    if(curIRTerm!=null){
                        predicate.setDocFrequency(curIRTerm.getDocFrequency());
                        predicate.setFrequency(curIRTerm.getFrequency());
                        predicate.setIndex(curIRTerm.getIndex());
                    }
                }
                if(predicate.getDocFrequency()>0){
                    list.add(predicate);
                }
            }
        }
        arrPredicate=new SimpleTermPredicate[list.size()];
        for(i=0;i<list.size();i++){
            arrPredicate[i] = ( (SimpleTermPredicate) list.get(i)).copy();
            if(!queryWeighting){
                arrPredicate[i].setWeight(1);
            }
        }
        return arrPredicate;
    }

    public IRDoc getIRDoc(int ranking){
        return (IRDoc)hitlist.get(ranking);
    }

    public ArrayList getRankedDocumentList(){
        return hitlist;
    }

    public int getRetrievedDocNum(){
        return hitlist.size();
    }

    public Smoother getSmoother(){
        return smoother;
    }

    public IndexReader getIndexReader(){
        return indexReader;
    }

    public IRQuery getQuery(){
        return query;
    }

    public void setQueryWeightingOption(boolean option){
        this.queryWeighting =option;
    }

    public boolean getQueryWeightingOption(){
        return queryWeighting;
    }
}