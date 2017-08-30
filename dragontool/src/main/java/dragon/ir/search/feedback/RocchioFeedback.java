package dragon.ir.search.feedback;

import dragon.ir.index.*;
import dragon.ir.query.*;
import dragon.ir.search.*;
import dragon.nlp.compare.*;
import dragon.nlp.Token;
import dragon.util.SortedArray;
import java.util.ArrayList;

/**
 * <p>Rocchio Feedback</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class RocchioFeedback extends AbstractFeedback{
    private int expandTermNum;
    // for BM25 term weighting
    private boolean useBM25;
    private double param1, param2;
    private double avgDocLength;

    public RocchioFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double relevantDocCoeffi) {
        super(searcher,feedbackDocNum,relevantDocCoeffi);
        this.expandTermNum =expandTermNum;
        useBM25=false;
    }

    public RocchioFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double relevantDocCoeffi, double bm25k1, double bm25b) {
        super(searcher,feedbackDocNum,relevantDocCoeffi);
        this.expandTermNum =expandTermNum;

        useBM25 =true;
        IRCollection collection=searcher.getIndexReader().getCollection();
        avgDocLength=collection.getTermCount()*1.0/collection.getDocNum();
        param1=bm25k1*(1-bm25b);
        param2=bm25k1*bm25b;
    }

    protected ArrayList estimateNewQueryModel(IRQuery oldQuery){
        IndexReader indexReader;
        SortedArray termList;
        ArrayList newPredicateList;
        SimpleTermPredicate curPredicate;
        Token curToken;
        IRDoc curDoc;
        int[] arrIndex, arrFreq;
        int releDocNum, totalDocNum, predicateNum, i, j;
        double weightSum, weight;

        indexReader=searcher.getIndexReader();
        searcher.search(oldQuery);
        releDocNum=feedbackDocNum<searcher.getRetrievedDocNum()?feedbackDocNum:searcher.getRetrievedDocNum();
        if(releDocNum==0) return null;

        totalDocNum=indexReader.getCollection().getDocNum();

        //calcuate the weight of the terms in the relevant documents
        termList=new SortedArray(new IndexComparator());
        for (i = 0; i <releDocNum; i++) {
            curDoc=searcher.getIRDoc(i);
            arrIndex = indexReader.getTermIndexList(curDoc.getIndex());
            arrFreq = indexReader.getTermFrequencyList(curDoc.getIndex());
            for (j = 0; j < arrIndex.length; j++){
                curToken=new Token(null);
                curToken.setIndex(arrIndex[j]);
                weight=getTermWeight(curDoc,arrFreq[j]);
                curToken.setWeight(weight);
                if(!termList.add(curToken)){
                    curToken=((Token)termList.get(termList.insertedPos()));
                    curToken.setWeight(curToken.getWeight()+weight);
                }
            }
        }
        for(i=0;i<termList.size();i++){
            curToken=(Token)termList.get(i);
            curToken.setWeight(curToken.getWeight()*getIDF(totalDocNum,indexReader.getIRTerm(curToken.getIndex()).getDocFrequency()));
        }

        //build new query
        termList.setComparator(new WeightComparator(true));
        predicateNum=oldQuery.getChildNum()+expandTermNum<termList.size()? oldQuery.getChildNum()+expandTermNum:termList.size();
        newPredicateList=new ArrayList(predicateNum);
        weightSum=0;
        for(i=0;i<predicateNum;i++){
            curToken=(Token)termList.get(i);
            if(curToken.getWeight()<=0)
                predicateNum=i;
            else
                weightSum += ( (Token) termList.get(i)).getWeight();
        }
        for(i=0;i<predicateNum;i++){
            curToken=(Token)termList.get(i);
            curPredicate=buildSimpleTermPredicate(curToken.getIndex(),curToken.getWeight()/weightSum);
            newPredicateList.add(curPredicate);
        }
        return newPredicateList;
    }

    private double getTermWeight(IRDoc curDoc, int termFreq){
        if(useBM25)
            return termFreq/(param1+param2*curDoc.getTermCount()/avgDocLength+termFreq);
        else
            return termFreq;
    }

    private double getIDF(int collectionDocNum, int termDocFrequency){
        return Math.log((collectionDocNum+1.0)/(termDocFrequency+0.5));
    }
}
