package dragon.ir.search.feedback;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.ir.query.IRQuery;
import dragon.ir.query.SimpleTermPredicate;
import dragon.ir.search.Searcher;
import dragon.nlp.Token;
import dragon.nlp.compare.IndexComparator;
import dragon.nlp.compare.WeightComparator;
import dragon.util.SortedArray;

import java.util.ArrayList;

/**
 * <p>Model-based Feedback</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class GenerativeFeedback extends AbstractFeedback{
    private int expandTermNum;
    private double bkgCoeffi;

    public GenerativeFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi, double bkgCoeffi) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.bkgCoeffi =bkgCoeffi;
    }

    protected ArrayList estimateNewQueryModel(IRQuery oldQuery){
        IndexReader indexReader;
        SortedArray termList;
        ArrayList newPredicateList;
        SimpleTermPredicate curPredicate;
        Token curToken;
        IRDoc curDoc;
        int[] arrIndex, arrFreq;
        int docNum, predicateNum, iterationNum, i, j;
        double[] arrProb, arrCollectionProb;
        double weightSum, collectionTermCount;

        indexReader=searcher.getIndexReader();
        searcher.search(oldQuery);
        docNum=feedbackDocNum<searcher.getRetrievedDocNum()?feedbackDocNum:searcher.getRetrievedDocNum();
        if(docNum==0) return null;

        //prepare data for EM
        termList=new SortedArray(new IndexComparator());
        for (i = 0; i <docNum; i++) {
            curDoc=searcher.getIRDoc(i);
            arrIndex = indexReader.getTermIndexList(curDoc.getIndex());
            arrFreq=indexReader.getTermFrequencyList(curDoc.getIndex());
            for (j = 0; j < arrIndex.length; j++){
                curToken=new Token(null);
                curToken.setIndex(arrIndex[j]);
                curToken.setFrequency(arrFreq[j]);
                if(!termList.add(curToken)){
                    ((Token)termList.get(termList.insertedPos())).addFrequency(curToken.getFrequency());
                }
            }
        }

        //initialization
        iterationNum=15;
        arrProb=new double[termList.size()];
        arrCollectionProb=new double[termList.size()];
        collectionTermCount=indexReader.getCollection().getTermCount();
        for(i=0;i<termList.size();i++){
            curToken=(Token)termList.get(i);
            curToken.setWeight(1.0/termList.size());
            arrCollectionProb[i]=bkgCoeffi*indexReader.getIRTerm(curToken.getIndex()).getFrequency()/collectionTermCount;
        }

        //iteration
        for(i=0;i<iterationNum;i++){
            weightSum=0;
            for(j=0;j<termList.size();j++){
                curToken=(Token)termList.get(j);
                arrProb[j]=(1-bkgCoeffi)*curToken.getWeight()/((1-bkgCoeffi)*curToken.getWeight()+arrCollectionProb[j])*curToken.getFrequency();
                weightSum+=arrProb[j];
            }
            for(j=0;j<termList.size();j++)
                ((Token)termList.get(j)).setWeight(arrProb[j]/weightSum);
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