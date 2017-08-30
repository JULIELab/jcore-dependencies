package dragon.ir.search;

import dragon.ir.index.*;
import dragon.ir.query.*;
import dragon.ir.search.smooth.Smoother;
import dragon.nlp.compare.WeightComparator;
import java.util.*;

/**
 * <p>Bool rank searcher </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BoolRankSearcher extends AbstractSearcher{
    public BoolRankSearcher(IndexReader indexReader, Smoother smoother) {
        super(indexReader,smoother);
    }

    public int search(IRQuery query){
        if(!query.isRelBoolQuery()){
            hitlist = null;
            return 0;
        }

        this.query =query;
        hitlist=getHitList(query);
        Collections.sort(hitlist, new WeightComparator(true));
        return hitlist.size();
    }

    private ArrayList getHitList(IRQuery query){
        int i;
        ArrayList list;

        if(query.isPredicate())
        {
            if(checkSimpleTermPredicate(query))
                list = getDocList( (SimpleTermPredicate) query);
            else
                list=null;
        }
        else
        {
            list=getHitList(query.getChild(0));
            for (i = 1; i < query.getChildNum(); i++) {
                if(query.getOperator().toString().equalsIgnoreCase("AND"))
                    list = addDocList(list, getHitList(query.getChild(i)),true);
                else
                    list = addDocList(list, getHitList(query.getChild(i)),false);
            }
        }
        return list;
    }

    private ArrayList getDocList(SimpleTermPredicate predicate){
        IRDoc[] arrDoc;
        ArrayList list;
        int[] arrFreq;
        int i;

        try {
            arrDoc = indexReader.getTermDocList(predicate.getIndex());
            list=new ArrayList(arrDoc.length);
            arrFreq = indexReader.getTermDocFrequencyList(predicate.getIndex());
            smoother.setQueryTerm(predicate);
            for (i = 0; i < arrDoc.length; i++) {
                arrDoc[i].setWeight(smoother.getSmoothedProb(arrDoc[i], arrFreq[i]));
                list.add(arrDoc[i]);
            }
            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList addDocList(ArrayList hisDocList, ArrayList curDocList,  boolean interaction){
        int i, j, k;
        IRDoc hisDoc, curDoc;
        ArrayList newList;

        i=0;
        j=0;
        newList=new ArrayList();
        while(i<hisDocList.size() && j<curDocList.size()){
            hisDoc=(IRDoc)hisDocList.get(i);
            curDoc=(IRDoc)curDocList.get(j);
            if(hisDoc.getIndex()==curDoc.getIndex())
            {
                hisDoc.setWeight(hisDoc.getWeight()+curDoc.getWeight());
                newList.add(hisDoc);
                i++;
                j++;
            }
            else if(hisDoc.getIndex()<curDoc.getIndex())
            {
                if(!interaction) newList.add(hisDoc);
                i++;
            }
            else
            {
                if(!interaction) newList.add(curDoc);
                j++;
            }
        }

        if(j<curDocList.size() && !interaction)
        {
            for(k=j;k<curDocList.size();k++) newList.add(curDocList.get(k));
        }

        if(i<hisDocList.size() && !interaction)
        {
            for(k=i;k<hisDocList.size();k++)  newList.add(hisDocList.get(k));
        }

        return newList;
    }

    private boolean checkSimpleTermPredicate(IRQuery query){
        SimpleTermPredicate predicate;
        IRTerm curIRTerm;

        predicate=(SimpleTermPredicate)query;
        if(predicate.getDocFrequency()<=0)
        {
            curIRTerm = indexReader.getIRTerm(predicate.getKey());
            if (curIRTerm != null) {
                predicate.setDocFrequency(curIRTerm.getDocFrequency());
                predicate.setFrequency(curIRTerm.getFrequency());
                predicate.setIndex(curIRTerm.getIndex());
            }
        }
        if(predicate.getDocFrequency()<=0)
            return false;
        else
            return true;
    }
}