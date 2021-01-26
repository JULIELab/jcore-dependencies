package dragon.ir.search;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IRTerm;
import dragon.ir.index.IndexReader;
import dragon.ir.query.IRQuery;
import dragon.ir.query.RelSimpleQuery;
import dragon.ir.query.SimpleTermPredicate;
import dragon.ir.search.smooth.Smoother;
import dragon.nlp.compare.WeightComparator;

import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>Full rank searcher </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FullRankSearcher extends AbstractSearcher{
    private boolean docFirst;

    public FullRankSearcher(IndexReader indexReader, Smoother smoother) {
        super(indexReader,smoother);
        smoother.setLogLikelihoodOption(true);
        if(smoother.isQueryTermFirstOptimal())
            this.docFirst =false;
        else
            this.docFirst =true;
    }

    public FullRankSearcher(IndexReader indexReader, Smoother smoother, boolean docFirst) {
        super(indexReader,smoother);
        smoother.setLogLikelihoodOption(true);
        this.docFirst =docFirst;
    }

    public int search(IRQuery query){
        this.query =query;
        if(docFirst)
            return breadthFirstSearch(query);
        else
            return depthFirstSearch(query);
    }

    public int breadthFirstSearch(IRQuery query){
        SimpleTermPredicate[] arrPredicate;
        IRTerm curTerm;
        IRDoc curDoc;
        int docNum,i,j;
        double weight;

        if(!query.isRelSimpleQuery()){
            hitlist = null;
            return 0;
        }

        docNum=indexReader.getCollection().getDocNum();
        hitlist=new ArrayList(docNum);
        arrPredicate=checkSimpleTermQuery((RelSimpleQuery)query);
        if(arrPredicate==null || arrPredicate.length==0){
            hitlist=null;
            return 0;
        }

        for (i = 0; i < docNum; i++) {
            curDoc=indexReader.getDoc(i);
            smoother.setDoc(curDoc);
            weight=0;
            for(j=0;j<arrPredicate.length;j++){
                curTerm=indexReader.getIRTerm(arrPredicate[j].getIndex(),i);
                if(curTerm!=null)
                    weight += smoother.getSmoothedProb(arrPredicate[j],curTerm.getFrequency());
                else
                    weight += smoother.getSmoothedProb(arrPredicate[j]);
            }
            curDoc.setWeight(weight);
            hitlist.add(curDoc);
        }
        Collections.sort(hitlist, new WeightComparator(true));
        return hitlist.size();
    }

    public int depthFirstSearch(IRQuery query){
        SimpleTermPredicate[] arrPredicate;
        IRDoc arrDoc[];
        int[] arrIndex, arrFreq;
        int docNum, i,j,k;

        if(!query.isRelSimpleQuery()){
            hitlist = null;
            return 0;
        }

        docNum=indexReader.getCollection().getDocNum();
        hitlist=new ArrayList(docNum);
        arrPredicate=checkSimpleTermQuery((RelSimpleQuery)query);
        if(arrPredicate==null || arrPredicate.length==0){
            hitlist=null;
            return 0;
        }

        arrDoc=new IRDoc[docNum];
        for (i = 0; i < docNum; i++){
            arrDoc[i]=indexReader.getDoc(i);
            arrDoc[i].setWeight(0);
        }

        for (i = 0; i < arrPredicate.length; i++) {
            smoother.setQueryTerm(arrPredicate[i]);

            arrIndex=indexReader.getTermDocIndexList(arrPredicate[i].getIndex());
            arrFreq=indexReader.getTermDocFrequencyList(arrPredicate[i].getIndex());
            k=0;
            for (j = 0; j <arrIndex.length; j++){
                while(k<arrIndex[j]){
                    arrDoc[k].setWeight(arrDoc[k].getWeight() + smoother.getSmoothedProb(arrDoc[k]));
                    k++;
                }
                arrDoc[k].setWeight(arrDoc[k].getWeight() + smoother.getSmoothedProb(arrDoc[k],arrFreq[j]));
                k++;
            }
            while(k<docNum){
                arrDoc[k].setWeight(arrDoc[k].getWeight() + smoother.getSmoothedProb(arrDoc[k]));
                k++;
            }
        }

        for (i = 0; i < docNum; i++)  hitlist.add(arrDoc[i]);
        Collections.sort(hitlist, new WeightComparator(true));
        return hitlist.size();
    }

}