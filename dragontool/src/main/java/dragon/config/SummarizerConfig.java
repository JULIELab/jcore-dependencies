package dragon.config;

import dragon.ir.index.sentence.OnlineSentenceIndexer;
import dragon.ir.kngbase.KnowledgeBase;
import dragon.ir.summarize.GenericMultiDocSummarizer;
import dragon.ir.summarize.LexRankSummarizer;
import dragon.ir.summarize.SemanticRankSummarizer;

/**
 * <p>Summarizer configuration</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SummarizerConfig extends ConfigUtil{
    public SummarizerConfig() {
       super();
    }

    public SummarizerConfig(ConfigureNode root){
       super(root);
    }

    public SummarizerConfig(String configFile){
        super(configFile);
    }

    public GenericMultiDocSummarizer getGenericMultiDocSummarizer(int summarizerID){
        return getGenericMultiDocSummarizer(root,summarizerID);
    }

    public GenericMultiDocSummarizer getGenericMultiDocSummarizer(ConfigureNode node, int summarizerID){
        return loadGenericMultiDocSummarizer(node,summarizerID);
    }

    private GenericMultiDocSummarizer loadGenericMultiDocSummarizer(ConfigureNode node, int summarizerID){
        ConfigureNode summarizerNode;
        String summarizerName;

        summarizerNode=getConfigureNode(node,"genericmultidocsummarizer",summarizerID);
        if(summarizerNode==null)
            return null;
        summarizerName=summarizerNode.getNodeName();
        return loadGenericMultiDocSummarizer(summarizerName,summarizerNode);
    }

    protected GenericMultiDocSummarizer loadGenericMultiDocSummarizer(String summarizerName,ConfigureNode summarizerNode){
        if(summarizerName.equalsIgnoreCase("LexRankSummarizer"))
            return loadLexRankSummarizer(summarizerNode);
        else if(summarizerName.equalsIgnoreCase("SemanticRankSummarizer"))
            return loadSemanticRankSummarizer(summarizerNode);
        else
            return (GenericMultiDocSummarizer)loadResource(summarizerNode);
    }

    private GenericMultiDocSummarizer loadLexRankSummarizer(ConfigureNode node){
        LexRankSummarizer summarizer;
        OnlineSentenceIndexer indexer;
        boolean tfidf, continuous;
        int indexerID;
        double similarityThreshold;

        tfidf=node.getBoolean("tfidf",true);
        continuous=node.getBoolean("continuousscore",true);
        similarityThreshold=node.getDouble("similaritythreshold");
        indexerID=node.getInt("onlinesentenceindexer");
        indexer=(OnlineSentenceIndexer)(new IndexerConfig()).getIndexer(node,indexerID);
        summarizer=new LexRankSummarizer(indexer,tfidf);
        summarizer.setContinuousScoreOpiton(continuous);
        summarizer.setSimilarityThreshold(similarityThreshold);
        return summarizer;
    }

    private GenericMultiDocSummarizer loadSemanticRankSummarizer(ConfigureNode node){
        SemanticRankSummarizer summarizer;
        OnlineSentenceIndexer tokenIndexer, phraseIndexer;
        KnowledgeBase kngBase;
        int indexerID, kngID;
        double transCoefficient, bkgCoefficient;

        transCoefficient=node.getDouble("transcoefficient",0.3);
        bkgCoefficient=node.getDouble("bkgcoefficient",0.5);
        indexerID=node.getInt("tokenindexer");
        tokenIndexer=(OnlineSentenceIndexer)(new IndexerConfig()).getIndexer(node,indexerID);
        kngID=node.getInt("knowledgebase");
        kngBase=(new KnowledgeBaseConfig()).getKnowledgeBase(node,kngID);
        indexerID=node.getInt("phraseindexer");
        if(indexerID>0){
            phraseIndexer = (OnlineSentenceIndexer) (new IndexerConfig()).getIndexer(node, indexerID);
            summarizer = new SemanticRankSummarizer(tokenIndexer, phraseIndexer, kngBase);
        }
        else
            summarizer = new SemanticRankSummarizer(tokenIndexer, kngBase);
        summarizer.setBackgroundCoefficient(bkgCoefficient);
        summarizer.setTranslationCoefficient(transCoefficient);
        summarizer.setMaxKLDivDistance(node.getDouble("maxkldivdistance",10));
        return summarizer;
    }
}
