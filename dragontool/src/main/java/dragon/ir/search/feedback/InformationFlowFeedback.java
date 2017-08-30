package dragon.ir.search.feedback;

import dragon.ir.index.*;
import dragon.ir.kngbase.HALSpace;
import dragon.ir.query.*;
import dragon.ir.search.*;
import dragon.ir.search.expand.*;
import dragon.nlp.*;
import dragon.nlp.extract.TokenExtractor;
import java.util.*;

/**
 * <p>Feedback based on information flow</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class InformationFlowFeedback extends AbstractFeedback{
    private InformationFlowQE qe;
    private TokenExtractor te;
    private SimpleElementList vocabulary;
    private int windowSize;
    private int minFrequency;

    public InformationFlowFeedback(TokenExtractor te, Searcher searcher, int feedbackDocNum, int expandTermNum, double feedbackCoeffi) {
        super(searcher,feedbackDocNum,feedbackCoeffi);
        this.te=te;
        this.qe=new InformationFlowQE(searcher.getIndexReader(),expandTermNum,feedbackCoeffi);
        windowSize=8;
        minFrequency=25;
    }

    protected ArrayList estimateNewQueryModel(IRQuery oldQuery){
        return null;
    }

    public void setHALWindowSize(int size){
        this.windowSize=size;
    }

    public int getHALWindowSize(){
        return this.windowSize;
    }

    public void setInfrequentTermThreshold(int threshold){
        this.minFrequency =threshold;
    }

    public int getInfrequentTermThreshold(){
        return this.minFrequency;
    }

    public void setMultiplier(double multiplier){
        qe.setMultiplier(multiplier);
    }

    public double getMultiplier() {
        return qe.getMultiplier();
    }

    public void setDominantVectorWeight(double weight) {
        qe.setDominantVectorWeight(weight);
    }

    public double getDominantVectorWeight() {
        return qe.getDominantVectorWeight();
    }

    public void setSubordinateVectorWeight(double weight) {
        qe.setSubordinateVectorWeight(weight);
    }

    public double getSubordinateVectorWeight() {
        return qe.getSubordinateVectorWeight();
    }

    public void setDominantVectorThreshold(double threshold) {
        qe.setDominantVectorThreshold(threshold);
    }

    public double getDominantVectorThreshold() {
        return qe.getDominantVectorThreshold();
    }

    public void setSubordinateVectorThreshold(double threshold) {
        qe.setSubordinateVectorThreshold(threshold);
    }

    public double getSubordinateVectorThreshold() {
        return qe.getSubordinateVectorThreshold();
    }

    public IRQuery updateQueryModel(IRQuery oldQuery){
        HALSpace hal;
        ArrayList articleList;
        IndexReader reader;
        String docKey;
        int i, docNum;

        searcher.search(oldQuery);
        docNum=feedbackDocNum<searcher.getRetrievedDocNum()?feedbackDocNum:searcher.getRetrievedDocNum();
        if(docNum==0) return oldQuery;

        articleList=new ArrayList(docNum);
        reader=searcher.getIndexReader();
        for(i=0;i<docNum;i++){
            docKey=reader.getDocKey(searcher.getIRDoc(i).getIndex());
            articleList.add(reader.getOriginalDoc(docKey));
        }

        if(vocabulary==null)
            vocabulary=prepVocabulary(searcher.getIndexReader(),minFrequency);
        hal=new HALSpace(vocabulary,te,windowSize);
        hal.setShowProgress(false);
        hal.add(articleList);
        hal.finalizeData();
        qe.setHALSpace(hal);
        return qe.expand(oldQuery);
    }

    private SimpleElementList prepVocabulary(IndexReader reader, int freqThreshold){
        SimpleElementList newList;
        IRTerm curTerm;
        String key;
        int i, termNum, index;

        newList=new SimpleElementList();
        termNum=reader.getCollection().getTermNum();
        for(i=0;i<termNum;i++){
            curTerm=reader.getIRTerm(i);
            if(curTerm.getFrequency()>=freqThreshold){
                index=curTerm.getIndex();
                key=reader.getTermKey(index);
                newList.add(new SimpleElement(key,index));
            }
        }
        return newList;
    }
}