package dragon.ir.summarize;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IRRelation;
import dragon.ir.index.IndexReader;
import dragon.nlp.Token;
import dragon.nlp.compare.IndexComparator;
import dragon.util.SortedArray;

import java.util.ArrayList;

/**
 * <p>Generative relation based summarization </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class RelationGenerativeSum extends AbstractStructureSum implements StructureSummarizer{
    private double bkgCoeffi;

    public RelationGenerativeSum(IndexReader indexReader, double bkgCoefficient) {
        super(indexReader);
        this.bkgCoeffi =bkgCoefficient;
    }

    public TopicSummary summarize(ArrayList docSet, int maxLength){
        SortedArray relationList;
        TopicSummary summary;
        Token curToken;
        IRRelation curRelation;
        TextUnit curTextUnit;
        IRDoc curDoc;
        String curText;
        int[] arrIndex, arrFreq;
        int docNum, iterationNum, relationNum, i, j;
        double[] arrProb, arrCollectionProb;
        double weightSum, collectionRelationCount;

        //prepare data for EM
        docNum=docSet.size();
        relationList=new SortedArray(new IndexComparator());
        for (i = 0; i <docNum; i++) {
            curDoc=(IRDoc)docSet.get(i);
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
        collectionRelationCount=indexReader.getCollection().getTermCount();
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

        summary=new TopicSummary(TextUnit.UNIT_RELATION);
        relationNum=Math.min(relationList.size(),maxLength);
        for(i=0;i<relationNum;i++){
            curToken=(Token)relationList.get(i);
            curRelation=indexReader.getIRRelation(curToken.getIndex());
            curText=indexReader.getTermKey(curRelation.getFirstTerm())+"<->"+indexReader.getTermKey(curRelation.getSecondTerm());
            curTextUnit=new TextUnit(curText, curToken.getIndex(),curToken.getWeight());
            summary.addText(curTextUnit);
        }
        summary.sortByWegiht();
        return summary;
    }
}