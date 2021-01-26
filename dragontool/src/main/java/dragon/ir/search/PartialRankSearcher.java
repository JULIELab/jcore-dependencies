package dragon.ir.search;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.ir.query.IRQuery;
import dragon.ir.query.RelSimpleQuery;
import dragon.ir.query.SimpleTermPredicate;
import dragon.ir.search.smooth.Smoother;
import dragon.nlp.compare.WeightComparator;

import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>Partial rank searcher</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public  class PartialRankSearcher extends AbstractSearcher{
    public PartialRankSearcher(IndexReader indexReader, Smoother smoother) {
        super(indexReader,smoother);
    }

    public int search(IRQuery query){
        SimpleTermPredicate[] arrPredicate;
        ArrayList queryList;
        int i;

        if(!query.isRelSimpleQuery()){
            hitlist = null;
            return 0;
        }

        this.query =query;
        queryList=new ArrayList();
        arrPredicate=checkSimpleTermQuery((RelSimpleQuery)query);
        if(arrPredicate==null || arrPredicate.length==0){
            hitlist=null;
            return 0;
        }

        hitlist = addDocList(new ArrayList(),queryList, getHitList(arrPredicate[0]),arrPredicate[0]);
        queryList.add(arrPredicate[0]);

        for (i = 1; i < arrPredicate.length; i++) {
            hitlist = addDocList(hitlist,queryList, getHitList(arrPredicate[i]),arrPredicate[i]);
            queryList.add(arrPredicate[i]);
        }
        Collections.sort(hitlist, new WeightComparator(true));
        return hitlist.size();
    }

    private IRDoc[] getHitList(SimpleTermPredicate predicate) {
        IRDoc[] arrDoc;
        int[] arrFreq;
        int i;

        try {
            if(predicate==null) return null;

            arrDoc = indexReader.getTermDocList(predicate.getIndex());
            arrFreq = indexReader.getTermDocFrequencyList(predicate.getIndex());
            smoother.setQueryTerm(predicate);

            for (i = 0; i < arrDoc.length; i++) {
                arrDoc[i].setWeight(smoother.getSmoothedProb(arrDoc[i], arrFreq[i]));
            }
            return arrDoc;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList addDocList(ArrayList hisDocList, ArrayList hisQueryList, IRDoc[] curDocList, SimpleTermPredicate curPredicate){
        int i, j, k;
        IRDoc hisDoc;
        ArrayList newList;

        if(curDocList==null) return hisDocList;

        i=0;
        j=0;
        newList=new ArrayList();

        while(i<hisDocList.size() && j<curDocList.length){
            hisDoc=(IRDoc)hisDocList.get(i);
            if(hisDoc.getIndex()==curDocList[j].getIndex())
            {
                hisDoc.setWeight(hisDoc.getWeight()+curDocList[j].getWeight());
                newList.add(hisDoc);
                i++;
                j++;
            }
            else if(hisDoc.getIndex()<curDocList[j].getIndex())
            {
                hisDoc.setWeight(hisDoc.getWeight()+smoother.getSmoothedProb(hisDoc, curPredicate));
                newList.add(hisDoc);
                i++;
            }
            else
            {
                curDocList[j].setWeight(curDocList[j].getWeight()+computeWeight(curDocList[j],hisQueryList));
                newList.add(curDocList[j]);
                j++;
            }
        }

        if(j<curDocList.length)
        {
            for(k=j;k<curDocList.length;k++){
                curDocList[k].setWeight(curDocList[k].getWeight()+computeWeight(curDocList[k],hisQueryList));
                newList.add(curDocList[k]);
            }
        }

        if(i<hisDocList.size())
        {
            for(k=i;k<hisDocList.size();k++){
                hisDoc=(IRDoc)hisDocList.get(k);
                hisDoc.setWeight(hisDoc.getWeight()+smoother.getSmoothedProb(hisDoc, curPredicate));
                newList.add(hisDoc);
            }
        }
        return newList;
    }

    private double computeWeight(IRDoc doc, ArrayList hisQueryList){
        int i;
        double weight;

        weight=0;
        smoother.setDoc(doc);
        for(i=0;i<hisQueryList.size();i++){
            weight += smoother.getSmoothedProb((SimpleTermPredicate) hisQueryList.get(i));
        }
        return weight;
    }
}