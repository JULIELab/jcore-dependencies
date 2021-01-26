package dragon.ir.summarize;

import dragon.ir.clustering.docdistance.KLDivDocDistance;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.ir.index.sentence.OnlineSentenceIndexReader;
import dragon.ir.index.sentence.OnlineSentenceIndexer;
import dragon.ir.kngbase.DocRepresentation;
import dragon.ir.kngbase.KnowledgeBase;
import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.SimpleElementList;

/**
 * <p>Semantic based ranking summarizer which can supports differnt semantic similarity measure such as ontology based
 * and model based.</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SemanticRankSummarizer extends LexRankSummarizer{
    private OnlineSentenceIndexer phraseIndexer;
    private KnowledgeBase kngBase;
    private double transCoefficient, bkgCoefficient;
    private double maxKLDivDistance;

    public SemanticRankSummarizer(OnlineSentenceIndexer tokenIndexer,KnowledgeBase kngBase) {
        this(tokenIndexer,null,kngBase);
    }

    public SemanticRankSummarizer(OnlineSentenceIndexer tokenIndexer, OnlineSentenceIndexer phraseIndexer, KnowledgeBase kngBase) {
        super(tokenIndexer);
        this.phraseIndexer =phraseIndexer;
        if(phraseIndexer!=null)
            phraseIndexer.setMinSentenceLength(tokenIndexer.getMinSentenceLength());
        this.kngBase =kngBase;
        this.transCoefficient =0.3;
        this.bkgCoefficient =0.4;
        this.maxKLDivDistance=10;
    }

    public void setTranslationCoefficient(double coefficient){
        this.transCoefficient =coefficient;
    }

    public void setBackgroundCoefficient(double coefficient){
        this.bkgCoefficient =coefficient;
    }

    public void setMaxKLDivDistance(double distance){
        this.maxKLDivDistance =distance;
    }

    private DoubleSparseMatrix computeTransDocTermMatrix(){
        DocRepresentation docRep;
        DoubleSparseMatrix transMatrix;
        OnlineSentenceIndexReader phraseIndexReader;
        int[] termMap, phraseMap;

        if(phraseIndexer!=null){
            collectionReader.restart();
            phraseIndexReader = new OnlineSentenceIndexReader(phraseIndexer, collectionReader);
            phraseIndexReader.initialize();
            phraseMap = getTermMap(phraseIndexReader, kngBase.getRowKeyList());
            termMap = getTermMap(indexReader, kngBase.getColumnKeyList());
            docRep = new DocRepresentation(indexReader, termMap);
            transMatrix = docRep.genModelMatrix(phraseIndexReader, phraseMap, kngBase.getKnowledgeMatrix(),
                                                transCoefficient, bkgCoefficient, true, 0.001);
            phraseIndexReader.close();
        }
        else{
            termMap = getTermMap(indexReader, kngBase.getColumnKeyList());
            docRep = new DocRepresentation(indexReader, termMap);
            transMatrix = docRep.genModelMatrix(indexReader, termMap, kngBase.getKnowledgeMatrix(),
                                                transCoefficient, bkgCoefficient, true, 0.001);
        }
        return transMatrix;
    }

    private int[] getTermMap(IndexReader reader, SimpleElementList newTermList){
        String curKey;
        int[] termMap;
        int i, newIndex, newTermNum;

        termMap=new int[reader.getCollection().getTermNum()];
        newTermNum=newTermList.size();
        for(i=0;i<termMap.length;i++){
            curKey=reader.getTermKey(i);
            newIndex=newTermList.search(curKey);
            if(newIndex>=0)
                termMap[i]=newIndex;
            else
                termMap[i]=newTermNum++;
        }
        return termMap;
    }

    protected double computeSimilarity(IRDoc firstSent, IRDoc secondSent){
        if(distanceMetric==null){
            distanceMetric=new KLDivDocDistance(computeTransDocTermMatrix(),maxKLDivDistance);
        }
        return Math.max(1-distanceMetric.getDistance(firstSent,secondSent),1-distanceMetric.getDistance(secondSent,firstSent));
    }
}