package dragon.config;

import dragon.ir.index.IndexReader;
import dragon.ir.search.Searcher;
import dragon.ir.search.feedback.*;
import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.extract.TokenExtractor;

/**
 * <p>Feedback configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FeedbackConfig extends ConfigUtil{
    public FeedbackConfig() {
       super();
    }

    public FeedbackConfig(ConfigureNode root){
       super(root);
    }

    public FeedbackConfig(String configFile){
        super(configFile);
    }

    public Feedback getFeedback(int feedbackID){
        return getFeedback(root,feedbackID);
    }

    public Feedback getFeedback(ConfigureNode node, int feedbackID){
        return loadFeedback(node,feedbackID);
    }

    private Feedback loadFeedback(ConfigureNode node, int feedbackID){
        ConfigureNode feedbackNode;
        String feedbackName;

        feedbackNode=getConfigureNode(node,"feedback",feedbackID);
        if(feedbackNode==null)
            return null;
        feedbackName=feedbackNode.getNodeName();
        return loadFeedback(feedbackName,feedbackNode);
    }

    protected Feedback loadFeedback(String feedbackName,ConfigureNode feedbackNode){
        if(feedbackName.equalsIgnoreCase("GenerativeFeedback"))
            return loadGenerativeFeedback(feedbackNode);
        else if(feedbackName.equalsIgnoreCase("MinDivergence"))
            return loadMinDivergenceFeedback(feedbackNode);
        else if(feedbackName.equalsIgnoreCase("RocchioFeedback"))
            return loadRocchioFeedback(feedbackNode);
        else if(feedbackName.equalsIgnoreCase("InformationFlowFeedback"))
            return loadInformationFlowFeedback(feedbackNode);
        else if(feedbackName.equalsIgnoreCase("RelationTransFeedback"))
            return loadRelationTransFeedback(feedbackNode);
        else if(feedbackName.equalsIgnoreCase("PhraseTransFeedback"))
            return loadPhraseTransFeedback(feedbackNode);
        else
            return (Feedback)loadResource(feedbackNode);
    }

    private Feedback loadGenerativeFeedback(ConfigureNode node){
        int feedbackDocNum, expandTermNum;
        double feedbackCoefficient, bkgCoefficient;

        feedbackDocNum=node.getInt("feedbackdocnum",10);
        expandTermNum=node.getInt("expandtermnum",10);
        feedbackCoefficient=node.getDouble("feedbackcoefficient",0.6);
        bkgCoefficient=node.getDouble("bkgcoefficient",0.5);
        return new GenerativeFeedback(getSearcher(node),feedbackDocNum,expandTermNum,feedbackCoefficient, bkgCoefficient);
    }

    private Feedback loadMinDivergenceFeedback(ConfigureNode node){
        int feedbackDocNum, expandTermNum;
        double feedbackCoefficient, bkgCoefficient;

        feedbackDocNum=node.getInt("feedbackdocnum",10);
        expandTermNum=node.getInt("expandtermnum",10);
        feedbackCoefficient=node.getDouble("feedbackcoefficient",0.6);
        bkgCoefficient=node.getDouble("bkgcoefficient",0.5);
        return new GenerativeFeedback(getSearcher(node),feedbackDocNum,expandTermNum,feedbackCoefficient, bkgCoefficient);
    }

    private Feedback loadRocchioFeedback(ConfigureNode node){
        int feedbackDocNum, expandTermNum;
        boolean useBM25;
        double feedbackCoefficient, bm25k1, bm25b;

        feedbackDocNum=node.getInt("feedbackdocnum",10);
        expandTermNum=node.getInt("expandtermnum",10);
        feedbackCoefficient=node.getDouble("feedbackcoefficient",0.6);
        useBM25=node.getBoolean("usebm25",false);
        if(useBM25){
            bm25k1= node.getDouble("bm25k1", 2.0);
            bm25b= node.getDouble("bm25b", 0.75);
            return new RocchioFeedback(getSearcher(node), feedbackDocNum, expandTermNum, feedbackCoefficient, bm25k1,bm25b);
        }
        else
            return new RocchioFeedback(getSearcher(node),feedbackDocNum,expandTermNum,feedbackCoefficient);
    }

    private Feedback loadInformationFlowFeedback(ConfigureNode node){
        ConceptExtractorConfig ceConfig;
        TokenExtractor te;
        InformationFlowFeedback feedback;
        int feedbackDocNum, expandTermNum, teID;
        double feedbackCoefficient;

        teID=node.getInt("tokenextractor",0);
        if(teID<=0)
            return null;
        ceConfig=new ConceptExtractorConfig();
        te=(TokenExtractor)ceConfig.getConceptExtractor(node,teID);
        feedbackDocNum=node.getInt("feedbackdocnum",10);
        expandTermNum=node.getInt("expandtermnum",10);
        feedbackCoefficient=node.getDouble("feedbackcoefficient",0.6);
        feedback=new InformationFlowFeedback(te, getSearcher(node),feedbackDocNum,expandTermNum,feedbackCoefficient);
        feedback.setHALWindowSize(node.getInt("windowsize",8));
        feedback.setInfrequentTermThreshold(node.getInt("minfrequency",25));
        feedback.setDominantVectorWeight(node.getDouble("dominance1",0.5) );
        feedback.setDominantVectorThreshold(node.getDouble("threshold1",0) );
        feedback.setSubordinateVectorWeight(node.getDouble("dominance1",0.3) );
        feedback.setSubordinateVectorThreshold(node.getDouble("threshold1",0) );
        feedback.setMultiplier(node.getDouble("multiplier",2.0) );
        return feedback;
    }

    private Feedback loadRelationTransFeedback(ConfigureNode node){
        DoubleSparseMatrix transMatrix;
        Searcher searcher;
        int transMatrixID;
        int feedbackDocNum, expandTermNum;
        double feedbackCoefficient, bkgCoefficient;
        boolean selfTranslation, generativeModel;

        feedbackDocNum=node.getInt("feedbackdocnum",10);
        expandTermNum=node.getInt("expandtermnum",10);
        feedbackCoefficient=node.getDouble("feedbackcoefficient",0.6);
        bkgCoefficient=node.getDouble("bkgcoefficient",0.5);
        selfTranslation=node.getBoolean("selftranslation",true);
        generativeModel=node.getBoolean("generativemodel",false);
        searcher=getSearcher(node);

        if(selfTranslation){
            if(generativeModel)
                return new RelationTransFeedback(searcher,feedbackDocNum,expandTermNum,feedbackCoefficient,bkgCoefficient);
            else
                return new RelationTransFeedback(searcher,feedbackDocNum,expandTermNum,feedbackCoefficient);
        }
        else{
            transMatrixID=node.getInt("transmatrix");
            transMatrix=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,transMatrixID);
            if(generativeModel)
                return new RelationTransFeedback(searcher, feedbackDocNum, expandTermNum, feedbackCoefficient, bkgCoefficient,transMatrix);
            else
                return new RelationTransFeedback(searcher,feedbackDocNum,expandTermNum,feedbackCoefficient, transMatrix);
        }
    }

    private Feedback loadPhraseTransFeedback(ConfigureNode node){
        DoubleSparseMatrix transMatrix;
        Searcher searcher;
        IndexReader phraseIndexReader;
        int transMatrixID;
        int feedbackDocNum, expandTermNum, phraseIndexReaderID;
        double feedbackCoefficient, bkgCoefficient;
        boolean selfTranslation;

        feedbackDocNum=node.getInt("feedbackdocnum",10);
        expandTermNum=node.getInt("expandtermnum",10);
        feedbackCoefficient=node.getDouble("feedbackcoefficient",0.6);
        bkgCoefficient=node.getDouble("bkgcoefficient",0.5);
        selfTranslation=node.getBoolean("selftranslation",true);
        phraseIndexReaderID=node.getInt("phraseindexreader");
        phraseIndexReader=(new IndexReaderConfig()).getIndexReader(node,phraseIndexReaderID);
        searcher=getSearcher(node);
        if(selfTranslation)
            return new PhraseTransFeedback(searcher,feedbackDocNum,expandTermNum,feedbackCoefficient,phraseIndexReader, bkgCoefficient);
        else{
            transMatrixID=node.getInt("transmatrix");
            transMatrix=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,transMatrixID);
            return new PhraseTransFeedback(searcher, feedbackDocNum, expandTermNum, feedbackCoefficient, phraseIndexReader,bkgCoefficient, transMatrix);
        }
    }

    private Searcher getSearcher(ConfigureNode node){
        SearcherConfig config;
        int searcherID;

        config=new SearcherConfig();
        searcherID=node.getInt("searcher");
        return config.getSearcher(node,searcherID);
    }
}
