package dragon.ir.summarize;

import dragon.config.ConfigureNode;
import dragon.config.IndexerConfig;
import dragon.ir.clustering.BasicKMean;
import dragon.ir.clustering.clustermodel.ClusterModel;
import dragon.ir.clustering.clustermodel.CosineClusterModel;
import dragon.ir.clustering.docdistance.CosineDocDistance;
import dragon.ir.index.IRDoc;
import dragon.ir.index.sentence.OnlineSentenceIndexReader;
import dragon.ir.index.sentence.OnlineSentenceIndexer;
import dragon.ir.kngbase.DocRepresentation;
import dragon.matrix.DoubleSparseMatrix;
import dragon.matrix.vector.DoubleVector;
import dragon.onlinedb.CollectionReader;

import java.util.ArrayList;

/**
 * <p>An extension of LexRank text summarizer</p>
 * <p>LexRank select sentences purely according to the importance of sentences generated by the power method. A poetential problem
 * is that selected top sentences may belong to the same cluster, i.e. not representative. To handle this problem, ClusterLexRank namely
 * clusters those sentences, and then select one sentences with highest score in each cluster and choose remaining sentences acoording to
 * the ranking score globally if nencessary</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ClusterLexRankSummarizer extends LexRankSummarizer {
    protected ClusterModel clusterModel;
    private int clusterNum;

    public static ClusterLexRankSummarizer getClusterLexRankSummarizer(ConfigureNode node){
        ClusterLexRankSummarizer summarizer;
        OnlineSentenceIndexer indexer;
        boolean tfidf, continuous;
        int indexerID, clusterNum;
        double similarityThreshold;

        tfidf=node.getBoolean("tfidf",true);
        continuous=node.getBoolean("continuousscore",true);
        similarityThreshold=node.getDouble("similaritythreshold");
        clusterNum=node.getInt("clusternum");
        indexerID=node.getInt("onlinesentenceindexer");
        indexer=(OnlineSentenceIndexer)(new IndexerConfig()).getIndexer(node,indexerID);
        summarizer=new ClusterLexRankSummarizer(indexer,tfidf, clusterNum);
        summarizer.setContinuousScoreOpiton(continuous);
        summarizer.setSimilarityThreshold(similarityThreshold);
        return summarizer;
    }

    public ClusterLexRankSummarizer(OnlineSentenceIndexer indexer, int clusterNum){
        super(indexer);
        this.clusterNum =clusterNum;
    }

    public ClusterLexRankSummarizer(OnlineSentenceIndexer indexer, boolean useTFIDF, int clusterNum) {
        super(indexer,useTFIDF);
        this.clusterNum =clusterNum;
    }

    public String summarize(CollectionReader collectionReader, int maxLength){
        DoubleVector vector;
        BasicKMean kmean;
        ArrayList sentSet;
        IRDoc[] arrDoc;
        String summary;
        int i;

        this.collectionReader =collectionReader;
        indexReader=new OnlineSentenceIndexReader(indexer, collectionReader);
        indexReader.initialize();
        sentSet=getSentenceSet(indexReader);
        arrDoc=new IRDoc[sentSet.size()];
        for(i=0;i<arrDoc.length;i++)
            arrDoc[i]=(IRDoc)sentSet.get(i);
        vector=powerMethod.getEigenVector(buildWeightMatrix(sentSet));

        kmean=new BasicKMean(indexReader,clusterModel,clusterNum);
        kmean.setMaxIteration(50);
        kmean.setRandomSeed(100);
        kmean.setShowProgress(false);
        kmean.cluster();
        summary=buildSummary(indexReader, sentSet, maxLength,vector, kmean.getClusterSet());
        indexReader.close();
        distanceMetric=null;
        clusterModel=null;
        return summary;
    }

    protected double computeSimilarity(IRDoc firstSent, IRDoc secondSent){
        DoubleSparseMatrix matrix;

        if(distanceMetric==null){
            if(useTFIDF){
                DocRepresentation docRepresentation = new DocRepresentation(indexReader);
                docRepresentation.setMessageOption(false);
                matrix=docRepresentation.genTFIDFMatrix();
                distanceMetric = new CosineDocDistance(matrix);
                clusterModel=new CosineClusterModel(clusterNum,matrix);
            }
            else{
                distanceMetric = new CosineDocDistance(indexReader.getDocTermMatrix());
                clusterModel=new CosineClusterModel(clusterNum,indexReader.getDocTermMatrix());
            }
        }
        return 1-distanceMetric.getDistance(firstSent,secondSent) ;
    }


}