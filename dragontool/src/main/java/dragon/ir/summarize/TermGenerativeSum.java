package dragon.ir.summarize;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.nlp.Token;
import dragon.nlp.compare.IndexComparator;
import dragon.util.SortedArray;

import java.util.ArrayList;
/**
 * <p>Generative term based summarization</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TermGenerativeSum extends AbstractStructureSum implements StructureSummarizer{
    private double bkgCoeffi;

    public TermGenerativeSum(IndexReader indexReader,double bkgCoefficient) {
        super(indexReader);
        this.bkgCoeffi =bkgCoefficient;
    }

    public TopicSummary summarize(ArrayList docSet, int maxLength){
        SortedArray termList;
        TopicSummary summary;
        Token curToken;
        IRDoc curDoc;
        TextUnit curTerm;
        int[] arrIndex, arrFreq;
        int docNum, iterationNum, termNum, i, j;
        double[] arrProb, arrCollectionProb;
        double weightSum, collectionTermCount;

        //prepare data for EM
        docNum=docSet.size();
        termList=new SortedArray(new IndexComparator());
        for (i = 0; i <docNum; i++) {
            curDoc=(IRDoc)docSet.get(i);
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

        summary=new TopicSummary(TextUnit.UNIT_TERM);
        termNum=Math.min(termList.size(),maxLength);
        for(i=0;i<termNum;i++){
            curToken=(Token)termList.get(i);
            curTerm=new TextUnit(indexReader.getTermKey(curToken.getIndex()), curToken.getIndex(),curToken.getWeight());
            summary.addText(curTerm);
        }
        summary.sortByWegiht();
        return summary;
    }
}