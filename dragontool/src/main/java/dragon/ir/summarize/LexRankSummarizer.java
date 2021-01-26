package dragon.ir.summarize;

import dragon.ir.clustering.docdistance.CosineDocDistance;
import dragon.ir.clustering.docdistance.DocDistance;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.ir.index.sentence.OnlineSentenceIndexReader;
import dragon.ir.index.sentence.OnlineSentenceIndexer;
import dragon.ir.kngbase.DocRepresentation;
import dragon.matrix.DoubleDenseMatrix;
import dragon.matrix.DoubleFlatDenseMatrix;
import dragon.matrix.vector.DoubleVector;
import dragon.matrix.vector.PowerMethod;
import dragon.onlinedb.CollectionReader;

import java.util.ArrayList;

/**
 * <p>LexRank Summerizer </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LexRankSummarizer extends AbstractSentenceSum implements GenericMultiDocSummarizer{
    protected OnlineSentenceIndexReader indexReader;
    protected OnlineSentenceIndexer indexer;
    protected CollectionReader collectionReader;
    protected DocDistance distanceMetric;
    protected PowerMethod powerMethod;
    protected double threshold;
    protected boolean useContinuousValue, useTFIDF;

    public LexRankSummarizer(OnlineSentenceIndexer indexer){
        this(indexer,true);
    }

    public LexRankSummarizer(OnlineSentenceIndexer indexer, boolean useTFIDF) {
        this.indexer =indexer;
        threshold=0.1;
        useContinuousValue=true;
        this.useTFIDF=useTFIDF;
        powerMethod=new PowerMethod(0.0001, 0.15);
        powerMethod.setMessageOption(false);
        powerMethod.setMaxIteration(50);
    }

    public void setSimilarityThreshold(double threshold){
        this.threshold =threshold;
    }

    public void setContinuousScoreOpiton(boolean option){
        this.useContinuousValue =option;
    }

    public String summarize(CollectionReader collectionReader, int maxLength){
        DoubleVector vector;
        ArrayList sentSet;
        String summary;

        this.collectionReader =collectionReader;
        indexReader=new OnlineSentenceIndexReader(indexer, collectionReader);
        indexReader.initialize();
        sentSet=getSentenceSet(indexReader);
        vector=powerMethod.getEigenVector(buildWeightMatrix(sentSet));
        summary=buildSummary(indexReader, sentSet, maxLength,vector);
        indexReader.close();
        distanceMetric=null;
        return summary;
    }

    protected DoubleDenseMatrix buildWeightMatrix(ArrayList docSet){
        DoubleFlatDenseMatrix matrix;
        IRDoc first, second;
        double similarity;
        int i, j;

        matrix=new DoubleFlatDenseMatrix(docSet.size(),docSet.size());
        for(i=0;i<docSet.size();i++){
            matrix.setDouble(i,i,1);
            first=(IRDoc)docSet.get(i);
            for(j=i+1;j<docSet.size();j++){
                second=(IRDoc)docSet.get(j);
                similarity=computeSimilarity(first,second);
                if(!useContinuousValue){
                    if (similarity <= threshold)
                        similarity = 0;
                    else
                        similarity = 1;
                }
                matrix.setDouble(i,j,similarity);
                matrix.setDouble(j,i,similarity);
            }
        }
        return matrix;
    }

    /**
     * LexRank Summarizer uses cosine similrity. This method can be overrided.
     * @param firstSentIndex: the number of the first sentence
     * @param secondSentIndex: the number of the second sentence
     * @return the similarity between two specified sentences. The similarity value
     * ranges from 0 to 1.
     */
    protected double computeSimilarity(IRDoc firstSent, IRDoc secondSent){

        if(distanceMetric==null){
            if(useTFIDF){
                DocRepresentation docRepresentation = new DocRepresentation(indexReader);
                docRepresentation.setMessageOption(false);
                distanceMetric = new CosineDocDistance(docRepresentation.genTFIDFMatrix());
            }
            else
                distanceMetric=new CosineDocDistance(indexReader.getDocTermMatrix());
        }
        return 1-distanceMetric.getDistance(firstSent,secondSent) ;
    }

    protected ArrayList getSentenceSet(IndexReader indexReader){
        ArrayList list;
        int i, docNum;

        docNum = indexReader.getCollection().getDocNum();
        list = new ArrayList(docNum);
        for (i = 0; i < docNum; i++)
            list.add(indexReader.getDoc(i));
        return list;
    }
}