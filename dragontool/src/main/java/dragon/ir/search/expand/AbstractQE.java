package dragon.ir.search.expand;

import dragon.ir.index.IRTerm;
import dragon.ir.index.IndexReader;
import dragon.ir.query.*;
import dragon.nlp.compare.IndexComparator;
import dragon.util.SortedArray;

import java.util.ArrayList;
/**
 * <p>Abstract Query Expansion</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AbstractQE {
    IndexReader indexReader;

    public AbstractQE(IndexReader indexReader) {
        this.indexReader =indexReader;
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
        }
        return arrPredicate;
    }

    protected SimpleTermPredicate buildSimpleTermPredicate(int termIndex, double queryWeight){
        SimpleTermPredicate predicate;
        IRTerm curTerm;

        curTerm = indexReader.getIRTerm(termIndex);
        predicate = new SimpleTermPredicate(new SimpleExpression("TERM", new Operator("="), curTerm.getKey()));
        predicate.setWeight(queryWeight);
        predicate.setFrequency(curTerm.getFrequency());
        predicate.setDocFrequency(curTerm.getDocFrequency());
        predicate.setIndex(termIndex);
        return predicate;
    }

    protected IRQuery buildQuery(SimpleTermPredicate[] oldQuery, SimpleTermPredicate[] newQuery, double expandCoeffi){
        RelSimpleQuery query;
        SortedArray list;
        double weight;
        int i;

        query=new RelSimpleQuery();
        list=new SortedArray(new IndexComparator());
        normalizeQuery(oldQuery);
        normalizeQuery(newQuery);
        for(i=0;i<oldQuery.length;i++){
            oldQuery[i].setWeight(oldQuery[i].getWeight()*(1-expandCoeffi));
            list.add(oldQuery[i]);
        }
        for(i=0;i<newQuery.length;i++){
            newQuery[i].setWeight(newQuery[i].getWeight()*expandCoeffi);
            if(!list.add(newQuery[i])){
                weight = ( (SimpleTermPredicate) list.get(list.insertedPos())).getWeight();
                ( (SimpleTermPredicate) list.get(list.insertedPos())).setWeight(weight+newQuery[i].getWeight());
            }
        }
        for(i=0;i<list.size();i++)
            query.add((SimpleTermPredicate)list.get(i));
        return query;
    }

    private void normalizeQuery(SimpleTermPredicate[] query){
        double sum;
        int i;

        sum=0;
        for(i=0;i<query.length;i++)
            sum+=query[i].getWeight();
        for(i=0;i<query.length;i++)
            query[i].setWeight(query[i].getWeight()/sum);
    }


}