package dragon.ir.search.feedback;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IRTerm;
import dragon.ir.index.IndexReader;
import dragon.ir.query.IRQuery;
import dragon.ir.query.SimpleTermPredicate;
import dragon.ir.search.Searcher;
import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.Token;
import dragon.nlp.compare.IndexComparator;
import dragon.nlp.compare.WeightComparator;
import dragon.util.SortedArray;

import java.util.ArrayList;

/**
 * <p>Feedback based on phrase-word translation</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class PhraseTransFeedback extends AbstractFeedback{
    private IndexReader phraseIndexer;
    private DoubleSparseMatrix transMatrix;
    private int expandTermNum;
    private double bkgCoeffi;
    private boolean selfTranslation;

    public PhraseTransFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi, IndexReader phraseIndexer,double bkgCoeffi) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.bkgCoeffi =bkgCoeffi;
        this.transMatrix =null;
        this.phraseIndexer =phraseIndexer;
        this.selfTranslation=true;
    }

    public PhraseTransFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi, IndexReader phraseIndexer,double bkgCoeffi, DoubleSparseMatrix transMatrix) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.bkgCoeffi =bkgCoeffi;
        this.transMatrix =transMatrix;
        this.phraseIndexer =phraseIndexer;
        this.selfTranslation=false;
    }

    protected ArrayList estimateNewQueryModel(IRQuery oldQuery){
        ArrayList termList, phraseList, newPredicateList;
        SimpleTermPredicate curPredicate;
        Token curToken;
        int docNum, predicateNum,i;
        double weightSum;

        searcher.search(oldQuery);
        docNum=feedbackDocNum<searcher.getRetrievedDocNum()?feedbackDocNum:searcher.getRetrievedDocNum();
        if(docNum==0) return null;

        phraseList=generativeModel(docNum);
        termList=translate(phraseList);

        //build new query
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

    private ArrayList generativeModel(int docNum){
        SortedArray phraseList;
        Token curToken;
        IRDoc curDoc;
        int[] arrIndex, arrFreq;
        int iterationNum, i, j;
        double[] arrProb, arrCollectionProb;
        double weightSum, collectionTermCount;

        //prepare data for EM
        phraseList=new SortedArray(new IndexComparator());
        for (i = 0; i <docNum; i++) {
            curDoc=searcher.getIRDoc(i);
            arrIndex = phraseIndexer.getTermIndexList(curDoc.getIndex());
            arrFreq=phraseIndexer.getTermFrequencyList(curDoc.getIndex());
            for (j = 0; j < arrIndex.length; j++){
                curToken=new Token(null);
                curToken.setIndex(arrIndex[j]);
                curToken.setFrequency(arrFreq[j]);
                if(!phraseList.add(curToken)){
                    ((Token)phraseList.get(phraseList.insertedPos())).addFrequency(curToken.getFrequency());
                }
            }
        }

        //initialization
        iterationNum=15;
        arrProb=new double[phraseList.size()];
        arrCollectionProb=new double[phraseList.size()];
        collectionTermCount=phraseIndexer.getCollection().getTermCount();
        for(i=0;i<phraseList.size();i++){
            curToken=(Token)phraseList.get(i);
            curToken.setWeight(1.0/phraseList.size());
            arrCollectionProb[i]=bkgCoeffi*phraseIndexer.getIRTerm(curToken.getIndex()).getFrequency()/collectionTermCount;
        }

        //iteration
        for(i=0;i<iterationNum;i++){
            weightSum=0;
            for(j=0;j<phraseList.size();j++){
                curToken=(Token)phraseList.get(j);
                arrProb[j]=(1-bkgCoeffi)*curToken.getWeight()/((1-bkgCoeffi)*curToken.getWeight()+arrCollectionProb[j])*curToken.getFrequency();
                weightSum+=arrProb[j];
            }
            for(j=0;j<phraseList.size();j++)
                ((Token)phraseList.get(j)).setWeight(arrProb[j]/weightSum);
        }
        phraseList.setComparator(new WeightComparator(true));
        return phraseList;
    }

    private SortedArray translate(ArrayList phraseList){
        IndexReader reader;
        SortedArray termList;
        IRTerm curTerm;
        Token curToken;
        String curPhrase, arrWord[];
        double[] arrWeight, arrTransProb;
        int[] arrIndex;
        int phraseNum;

        int i, j;

        reader=searcher.getIndexReader();
        arrWeight=new double[reader.getCollection().getTermNum()];
        for(i=0;i<arrWeight.length;i++) arrWeight[i]=0;

        if(selfTranslation)
            phraseNum=phraseList.size();
        else
            phraseNum=100<phraseList.size()?100:phraseList.size();

        for(i=0;i<phraseNum;i++){
            curToken=(Token)phraseList.get(i);
            if(selfTranslation){
                curPhrase=phraseIndexer.getTermKey(curToken.getIndex());
                arrWord=curPhrase.split(" ");
                for(j=0;j<arrWord.length;j++){
                    curTerm=reader.getIRTerm(arrWord[j]);
                    if(curTerm!=null)
                        arrWeight[curTerm.getIndex()] += curToken.getWeight();
                }
            }
            else{
                arrIndex=transMatrix.getNonZeroColumnsInRow(curToken.getIndex());
                arrTransProb=transMatrix.getNonZeroDoubleScoresInRow(curToken.getIndex());
                if(arrIndex!=null){
                    for (j = 0; j < arrIndex.length; j++)
                        arrWeight[arrIndex[j]] += curToken.getWeight() * arrTransProb[j];
                }
            }
        }

        termList=new SortedArray(new WeightComparator(true));
        for(i=0;i<arrWeight.length;i++){
            if(arrWeight[i]>0){
                curToken=new Token(null);
                curToken.setWeight(arrWeight[i]);
                curToken.setIndex(i);
                termList.add(curToken);
            }
        }
        return termList;
    }
}
