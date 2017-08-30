package dragon.ir.search.feedback;

import dragon.ir.index.*;
import dragon.ir.query.*;
import dragon.ir.search.*;
import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.compare.*;
import dragon.nlp.Token;
import dragon.util.SortedArray;
import java.util.ArrayList;

/**
 * <p>Feedback based on relation-term translation</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class RelationTransFeedback extends AbstractFeedback{
    DoubleSparseMatrix transMatrix;
    private int expandTermNum;
    private double bkgCoeffi;
    private boolean selfTranslation, generativeModel;

    public RelationTransFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.transMatrix =null;
        this.selfTranslation=true;
        this.generativeModel =false;
    }

    public RelationTransFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi, DoubleSparseMatrix transMatrix) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.transMatrix =transMatrix;
        this.selfTranslation=false;
        this.generativeModel =false;
    }

    public RelationTransFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi, double bkgCoeffi) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.bkgCoeffi =bkgCoeffi;
        this.transMatrix =null;
        this.selfTranslation=true;
        this.generativeModel =true;
    }

    public RelationTransFeedback(Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi, double bkgCoeffi, DoubleSparseMatrix transMatrix) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.expandTermNum =expandTermNum;
        this.bkgCoeffi =bkgCoeffi;
        this.transMatrix =transMatrix;
        this.selfTranslation=false;
        this.generativeModel =true;
    }

    protected ArrayList estimateNewQueryModel(IRQuery oldQuery){
        ArrayList termList, relationList, newPredicateList;
        SimpleTermPredicate curPredicate;
        Token curToken;
        int docNum, predicateNum,i;
        double weightSum;

        searcher.search(oldQuery);
        docNum=feedbackDocNum<searcher.getRetrievedDocNum()?feedbackDocNum:searcher.getRetrievedDocNum();
        if(docNum==0) return null;

        if(generativeModel)
            relationList=generativeModel(docNum);
        else
            relationList=associationModel(docNum,oldQuery);
        termList=translate(relationList);

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

    private ArrayList associationModel(int docNum, IRQuery oldQuery){
        IndexReader indexReader;
        SortedArray oldTermList,relationList;
        SimpleTermPredicate curPredicate;
        IRRelation curRelation;
        IRDoc curDoc;
        Token curToken, oldToken;
        int[] arrRelationIndex, arrRelationFreq;
        int i, j;
        double weightSum;

        indexReader=searcher.getIndexReader();
        relationList=new SortedArray(new IndexComparator());
        oldTermList=new SortedArray();
        for(i=0;i<oldQuery.getChildNum();i++){
            curPredicate=(SimpleTermPredicate) oldQuery.getChild(i);
            if (curPredicate.getDocFrequency()>0) {
                oldTermList.add(new Integer(curPredicate.getIndex()));
            }
        }

        weightSum=0;
        for (i = 0; i <docNum; i++) {
            curDoc=searcher.getIRDoc(i);
            arrRelationIndex = indexReader.getRelationIndexList(curDoc.getIndex());
            arrRelationFreq=indexReader.getRelationFrequencyList(curDoc.getIndex());
            for (j = 0; j < arrRelationIndex.length; j++){
                curRelation=indexReader.getIRRelation(arrRelationIndex[j]);
                if(oldTermList.contains(new Integer(curRelation.getFirstTerm())) ||
                   oldTermList.contains(new Integer(curRelation.getSecondTerm()))){
                    curToken=new Token(null);
                    curToken.setWeight(arrRelationFreq[j]);
                    curToken.setIndex(arrRelationIndex[j]);
                    if(!relationList.add(curToken)){
                        oldToken=(Token)relationList.get(relationList.insertedPos());
                        oldToken.setWeight(oldToken.getWeight()+curToken.getWeight());
                    }
                    weightSum+=curToken.getWeight();
                }
            }
        }

        for(i=0;i<relationList.size();i++){
            curToken=(Token)relationList.get(i);
            curToken.setWeight(curToken.getWeight()/weightSum);
        }
        relationList.setComparator(new WeightComparator(true));
        return relationList;
    }

    private ArrayList generativeModel(int docNum){
        IndexReader indexReader;
        SortedArray relationList;
        Token curToken;
        IRDoc curDoc;
        int[] arrIndex, arrFreq;
        int iterationNum, i, j;
        double[] arrProb, arrCollectionProb;
        double weightSum, collectionRelationCount;

        indexReader=searcher.getIndexReader();

        //prepare data for EM
        relationList=new SortedArray(new IndexComparator());
        for (i = 0; i <docNum; i++) {
            curDoc=searcher.getIRDoc(i);
            arrIndex = indexReader.getRelationIndexList(curDoc.getIndex());
            arrFreq=indexReader.getRelationFrequencyList(curDoc.getIndex());
            for (j = 0; j < arrIndex.length; j++){
                curToken=new Token(null);
                curToken.setIndex(arrIndex[j]);
                curToken.setFrequency(arrFreq[j]);
                if(!relationList.add(curToken)){
                    ((Token)relationList.get(relationList.insertedPos())).addFrequency(curToken.getFrequency());
                }
            }
        }

        //initialization
        iterationNum=15;
        arrProb=new double[relationList.size()];
        arrCollectionProb=new double[relationList.size()];
        collectionRelationCount=indexReader.getCollection().getRelationCount();
        for(i=0;i<relationList.size();i++){
            curToken=(Token)relationList.get(i);
            curToken.setWeight(1.0/relationList.size());
            arrCollectionProb[i]=bkgCoeffi*indexReader.getIRRelation(curToken.getIndex()).getFrequency()/collectionRelationCount;
        }

        //iteration
        for(i=0;i<iterationNum;i++){
            weightSum=0;
            for(j=0;j<relationList.size();j++){
                curToken=(Token)relationList.get(j);
                arrProb[j]=(1-bkgCoeffi)*curToken.getWeight()/((1-bkgCoeffi)*curToken.getWeight()+arrCollectionProb[j])*curToken.getFrequency();
                weightSum+=arrProb[j];
            }
            for(j=0;j<relationList.size();j++)
                ((Token)relationList.get(j)).setWeight(arrProb[j]/weightSum);
        }
        relationList.setComparator(new WeightComparator(true));
        return relationList;
    }

    private SortedArray translate(ArrayList relationList){
        IndexReader reader;
        SortedArray termList;
        Token curToken;
        IRRelation curRelation;
        double[] arrWeight, arrTransProb;
        int[] arrIndex;
        int relationNum;

        int i, j;

        reader=searcher.getIndexReader();
        arrWeight=new double[reader.getCollection().getTermNum()];
        for(i=0;i<arrWeight.length;i++) arrWeight[i]=0;

        if(selfTranslation)
            relationNum=relationList.size();
        else
            relationNum=100<relationList.size()?100:relationList.size();

        for(i=0;i<relationNum;i++){
            curToken=(Token)relationList.get(i);
            if(selfTranslation){
                curRelation=reader.getIRRelation(curToken.getIndex());
                arrWeight[curRelation.getFirstTerm()]+=curToken.getWeight();
                arrWeight[curRelation.getSecondTerm()]+=curToken.getWeight();
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
