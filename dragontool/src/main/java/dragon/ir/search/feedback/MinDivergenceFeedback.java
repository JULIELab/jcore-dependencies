package dragon.ir.search.feedback;

import dragon.ir.index.*;
import dragon.ir.query.*;
import dragon.ir.search.*;
import dragon.nlp.compare.*;
import dragon.nlp.Token;
import dragon.util.SortedArray;
import java.util.ArrayList;

/**
 * <p>Feedback based on Minimum Divergence</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class MinDivergenceFeedback extends AbstractFeedback{
    private int expandTermNum;
    private double bkgCoeffi;

    public MinDivergenceFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi, double bkgCoeffi) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.bkgCoeffi =bkgCoeffi;
    }

    protected ArrayList estimateNewQueryModel(IRQuery oldQuery){
        IndexReader indexReader;
        SortedArray termList;
        ArrayList newPredicateList;
        SimpleTermPredicate curPredicate;
        Token curToken, oldToken;
        IRDoc curDoc;
        int[] arrIndex, arrFreq;
        int docNum, predicateNum, i, j;
        double weight, weightSum, collectionTermCount;

        indexReader=searcher.getIndexReader();
        searcher.search(oldQuery);
        docNum=feedbackDocNum<searcher.getRetrievedDocNum()?feedbackDocNum:searcher.getRetrievedDocNum();
        if(docNum==0) return null;

        termList=new SortedArray(new IndexComparator());
        for (i = 0; i <docNum; i++) {
            curDoc=searcher.getIRDoc(i);
            arrIndex = indexReader.getTermIndexList(curDoc.getIndex());
            arrFreq=indexReader.getTermFrequencyList(curDoc.getIndex());
            for (j = 0; j < arrIndex.length; j++){
                curToken=new Token(null);
                curToken.setIndex(arrIndex[j]);
                curToken.setWeight(Math.log(arrFreq[j]*1.0/curDoc.getTermCount()));
                if(!termList.add(curToken)){
                    oldToken=(Token)termList.get(termList.insertedPos());
                    oldToken.setWeight(oldToken.getWeight()+curToken.getWeight());
                }
            }
        }
        collectionTermCount=indexReader.getCollection().getTermCount();
        for(i=0;i<termList.size();i++){
            curToken=(Token)termList.get(i);
            weight=curToken.getWeight()/docNum-bkgCoeffi*indexReader.getIRTerm(curToken.getIndex()).getFrequency()/collectionTermCount;
            curToken.setWeight(Math.exp(weight/(1-bkgCoeffi)));
        }

        //build new query
        termList.setComparator(new WeightComparator(true));
        predicateNum=oldQuery.getChildNum()+expandTermNum<termList.size()? oldQuery.getChildNum()+expandTermNum:termList.size();
        newPredicateList=new ArrayList(predicateNum);
        weightSum=0;
        for(i=0;i<predicateNum;i++) weightSum+=((Token)termList.get(i)).getWeight();
        for(i=0;i<predicateNum;i++){
            curToken=(Token)termList.get(i);
            curPredicate=buildSimpleTermPredicate(curToken.getIndex(),curToken.getWeight()/weightSum);
            newPredicateList.add(curPredicate);
        }
        return newPredicateList;
    }
}
