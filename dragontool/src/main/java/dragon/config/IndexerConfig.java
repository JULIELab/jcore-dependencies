package dragon.config;

import dragon.ir.index.BasicIndexer;
import dragon.ir.index.DualIndexer;
import dragon.ir.index.Indexer;
import dragon.ir.index.OnlineIndexer;
import dragon.ir.index.sentence.BasicSentenceIndexer;
import dragon.ir.index.sentence.DualSentenceIndexer;
import dragon.ir.index.sentence.OnlineSentenceIndexer;
import dragon.ir.index.sequence.BasicSequenceIndexer;
import dragon.ir.index.sequence.OnlineSequenceIndexer;
import dragon.nlp.extract.ConceptExtractor;
import dragon.nlp.extract.DualConceptExtractor;
import dragon.nlp.extract.TokenExtractor;
import dragon.nlp.extract.TripleExtractor;

/**
 * <p>Indexer configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IndexerConfig extends ConfigUtil{
    public IndexerConfig() {
       super();
    }

    public IndexerConfig(ConfigureNode root){
       super(root);
    }

    public IndexerConfig(String configFile){
        super(configFile);
    }

    public Indexer getIndexer(int indexerID){
        return getIndexer(root,indexerID);
    }

    public Indexer getIndexer(ConfigureNode node, int indexerID){
        return loadIndexer(node,indexerID);
    }

    private Indexer loadIndexer(ConfigureNode node, int indexerID){
        ConfigureNode indexerNode;
        String indexerName;

        indexerNode=getConfigureNode(node,"indexer",indexerID);
        if(indexerNode==null)
            return null;
        indexerName=indexerNode.getNodeName();
        return loadIndexer(indexerName,indexerNode);
    }

    protected Indexer loadIndexer(String indexerName, ConfigureNode indexerNode){
        if(indexerName.equalsIgnoreCase("BasicIndexer"))
            return loadBasicIndexer(indexerNode);
        else if(indexerName.equalsIgnoreCase("OnlineIndexer"))
            return loadOnlineIndexer(indexerNode);
        else if(indexerName.equalsIgnoreCase("BasicSequenceIndexer"))
            return loadBasicSequenceIndexer(indexerNode);
        else if(indexerName.equalsIgnoreCase("OnlineSequenceIndexer"))
            return loadOnlineSequenceIndexer(indexerNode);
        else if(indexerName.equalsIgnoreCase("OnlineSentenceIndexer"))
            return loadOnlineSentenceIndexer(indexerNode);
        else if(indexerName.equalsIgnoreCase("BasicSentenceIndexer"))
            return loadBasicSentenceIndexer(indexerNode);
        else if(indexerName.equalsIgnoreCase("DualSentenceIndexer"))
            return loadDualSentenceIndexer(indexerNode);
        else if(indexerName.equalsIgnoreCase("DualIndexer"))
            return loadDualIndexer(indexerNode);
        else
            return (Indexer)loadResource(indexerNode);
    }

    private Indexer loadBasicIndexer(ConfigureNode curNode){
        BasicIndexer indexer;
        ConceptExtractorConfig cptConfig;
        TripleExtractorConfig  tripleConfig;
        ConceptExtractor ce;
        TripleExtractor te;
        String logFile, cptIndexFolder, charIndexFolder;
        int extractorID;
        boolean indexAll, indexTitle, indexAbstract, indexBody, indexMeta;

        charIndexFolder=curNode.getString("characterindexfolder",null);
        cptIndexFolder=curNode.getString("conceptindexfolder",null);
        extractorID=curNode.getInt("conceptextractor",-1);
        if(extractorID>=0){
            cptConfig=new ConceptExtractorConfig();
            ce=cptConfig.getConceptExtractor(curNode,extractorID);
            indexer=new BasicIndexer(ce,charIndexFolder, cptIndexFolder);
        }
        else{
            extractorID=curNode.getInt("tripleextractor",-1);
            if(extractorID<0)
                return null;
            tripleConfig=new TripleExtractorConfig();
            te=tripleConfig.getTripleExtractor(curNode,extractorID);
            indexer=new BasicIndexer(te,charIndexFolder, cptIndexFolder);
        }
        logFile=curNode.getString("logfile",null);
        if(logFile!=null)
            indexer.setLog(logFile);

        indexAll=curNode.getBoolean("indexall",true);
        indexTitle=curNode.getBoolean("indextitle",false);
        indexAbstract=curNode.getBoolean("indexabstract",false);
        indexBody=curNode.getBoolean("indexbody",false);
        indexMeta=curNode.getBoolean("indexmeta",false);
        indexer.setSectionIndexOption(indexAll,indexTitle, indexAbstract, indexBody, indexMeta);
        indexer.initialize();
        return indexer;
    }

    private Indexer loadOnlineIndexer(ConfigureNode curNode){
        OnlineIndexer indexer;
        ConceptExtractorConfig cptConfig;
        TripleExtractorConfig  tripleConfig;
        ConceptExtractor ce;
        TripleExtractor te;
        int extractorID;
        boolean useConcept, indexTitle, indexAbstract, indexBody, indexMeta;



        useConcept=curNode.getBoolean("indexconceptentry",false);
        extractorID=curNode.getInt("conceptextractor",-1);
        if(extractorID>=0){
            cptConfig=new ConceptExtractorConfig();
            ce=cptConfig.getConceptExtractor(curNode,extractorID);
            indexer=new OnlineIndexer(ce, useConcept);
        }
        else{
            extractorID=curNode.getInt("tripleextractor",-1);
            if(extractorID<0)
                return null;
            tripleConfig=new TripleExtractorConfig();
            te=tripleConfig.getTripleExtractor(curNode,extractorID);
            indexer=new OnlineIndexer(te, useConcept);
        }

        indexTitle=curNode.getBoolean("indextitle",true);
        indexAbstract=curNode.getBoolean("indexabstract",true);
        indexBody=curNode.getBoolean("indexbody",true);
        indexMeta=curNode.getBoolean("indexmeta",true);
        indexer.screenArticleContent(indexTitle, indexAbstract, indexBody,indexMeta);
        indexer.initialize();
        return indexer;
    }

    private Indexer loadDualIndexer(ConfigureNode curNode){
        DualIndexer indexer;
        DualConceptExtractor ce;
        String logFile, firstIndexFolder, secondIndexFolder;
        int extractorID;
        boolean firstUseConcept, secondUseConcept;

        firstIndexFolder=curNode.getString("firstindexfolder");
        secondIndexFolder=curNode.getString("secondindexfolder");
        extractorID=curNode.getInt("dualconceptextractor");
        ce=(new DualConceptExtractorConfig()).getDualConceptExtractor(curNode,extractorID);
        firstUseConcept=curNode.getBoolean("firstindexconceptentry",false);
        secondUseConcept=curNode.getBoolean("secondindexconceptentry",false);
        indexer=new DualIndexer(ce,firstUseConcept, firstIndexFolder, secondUseConcept, secondIndexFolder);
        logFile=curNode.getString("logfile",null);
        if(logFile!=null)
            indexer.setLog(logFile);
        indexer.initialize();
        return indexer;
    }

    private Indexer loadBasicSentenceIndexer(ConfigureNode curNode){
        BasicSentenceIndexer indexer;
        ConceptExtractorConfig cptConfig;
        TripleExtractorConfig  tripleConfig;
        ConceptExtractor ce;
        TripleExtractor te;
        String logFile, indexFolder;
        int extractorID;
        boolean useConcept, indexTitle, indexAbstract, indexBody;

        indexFolder=curNode.getString("characterindexfolder",null);
        if(indexFolder!=null)
            useConcept=false;
        else{
            indexFolder = curNode.getString("conceptindexfolder", null);
            if(indexFolder==null)
                return null;
            else
                useConcept=true;
        }

        extractorID=curNode.getInt("conceptextractor",-1);
        if(extractorID>=0){
            cptConfig=new ConceptExtractorConfig();
            ce=cptConfig.getConceptExtractor(curNode,extractorID);
            indexer=new BasicSentenceIndexer(ce, useConcept,indexFolder);
        }
        else{
            extractorID=curNode.getInt("tripleextractor",-1);
            if(extractorID<0)
                return null;
            tripleConfig=new TripleExtractorConfig();
            te=tripleConfig.getTripleExtractor(curNode,extractorID);
            indexer=new BasicSentenceIndexer(te, useConcept, indexFolder);
        }
        logFile=curNode.getString("logfile",null);
        if(logFile!=null)
            indexer.setLog(logFile);

        indexTitle=curNode.getBoolean("indextitle",true);
        indexAbstract=curNode.getBoolean("indexabstract",true);
        indexBody=curNode.getBoolean("indexbody",true);
        indexer.screenArticleContent(indexTitle, indexAbstract, indexBody);
        indexer.initialize();
        indexer.setMinSentenceLength(curNode.getInt("minsentencelength",1));
        return indexer;
    }

    private Indexer loadOnlineSentenceIndexer(ConfigureNode curNode){
        OnlineSentenceIndexer indexer;
        ConceptExtractorConfig cptConfig;
        TripleExtractorConfig  tripleConfig;
        ConceptExtractor ce;
        TripleExtractor te;
        int extractorID;
        boolean useConcept, indexTitle, indexAbstract, indexBody;

        useConcept=curNode.getBoolean("indexconceptentry",false);
        extractorID=curNode.getInt("conceptextractor",-1);
        if(extractorID>=0){
            cptConfig=new ConceptExtractorConfig();
            ce=cptConfig.getConceptExtractor(curNode,extractorID);
            indexer=new OnlineSentenceIndexer(ce, useConcept);
        }
        else{
            extractorID=curNode.getInt("tripleextractor",-1);
            if(extractorID<0)
                return null;
            tripleConfig=new TripleExtractorConfig();
            te=tripleConfig.getTripleExtractor(curNode,extractorID);
            indexer=new OnlineSentenceIndexer(te, useConcept);
        }

        indexTitle=curNode.getBoolean("indextitle",true);
        indexAbstract=curNode.getBoolean("indexabstract",true);
        indexBody=curNode.getBoolean("indexbody",true);
        indexer.screenArticleContent(indexTitle, indexAbstract, indexBody);
        indexer.initialize();
        indexer.setMinSentenceLength(curNode.getInt("minsentencelength",1));
        return indexer;
    }

    private Indexer loadDualSentenceIndexer(ConfigureNode curNode){
        DualSentenceIndexer indexer;
        DualConceptExtractor ce;
        String logFile, firstIndexFolder, secondIndexFolder;
        int extractorID;
        boolean firstUseConcept, secondUseConcept;

        firstIndexFolder=curNode.getString("firstindexfolder");
        secondIndexFolder=curNode.getString("secondindexfolder");
        extractorID=curNode.getInt("dualconceptextractor");
        ce=(new DualConceptExtractorConfig()).getDualConceptExtractor(curNode,extractorID);
        firstUseConcept=curNode.getBoolean("firstindexconceptentry",false);
        secondUseConcept=curNode.getBoolean("secondindexconceptentry",false);
        indexer=new DualSentenceIndexer(ce,firstUseConcept, firstIndexFolder, secondUseConcept, secondIndexFolder);
        logFile=curNode.getString("logfile",null);
        if(logFile!=null)
            indexer.setLog(logFile);
        indexer.initialize();
        return indexer;
    }

    private Indexer loadBasicSequenceIndexer(ConfigureNode curNode){
        BasicSequenceIndexer indexer;
        ConceptExtractorConfig cptConfig;
        TokenExtractor te;
        String logFile, indexFolder;
        int extractorID;
        boolean indexTitle, indexAbstract, indexBody, indexMeta;

        extractorID=curNode.getInt("conceptextractor");
        cptConfig=new ConceptExtractorConfig();
        te=(TokenExtractor)cptConfig.getConceptExtractor(curNode,extractorID);
        indexFolder=curNode.getString("characterindexfolder");
        indexer=new BasicSequenceIndexer(te,indexFolder);
        logFile=curNode.getString("logfile",null);
        if(logFile!=null)
            indexer.setLog(logFile);
        indexTitle=curNode.getBoolean("indextitle",false);
        indexAbstract=curNode.getBoolean("indexabstract",false);
        indexBody=curNode.getBoolean("indexbody",false);
        indexMeta=curNode.getBoolean("indexmeta",false);
        indexer.setSectionIndexOption(indexTitle, indexAbstract, indexBody, indexMeta);
        indexer.initialize();
        return indexer;
    }

    private Indexer loadOnlineSequenceIndexer(ConfigureNode curNode){
        OnlineSequenceIndexer indexer;
        ConceptExtractorConfig cptConfig;
        TokenExtractor te;
        int extractorID;
        boolean indexTitle, indexAbstract, indexBody, indexMeta;

        extractorID=curNode.getInt("conceptextractor");
        cptConfig=new ConceptExtractorConfig();
        te=(TokenExtractor)cptConfig.getConceptExtractor(curNode,extractorID);
        indexer=new OnlineSequenceIndexer(te);
        indexTitle=curNode.getBoolean("indextitle",false);
        indexAbstract=curNode.getBoolean("indexabstract",false);
        indexBody=curNode.getBoolean("indexbody",false);
        indexMeta=curNode.getBoolean("indexmeta",false);
        indexer.setSectionIndexOption(indexTitle, indexAbstract, indexBody, indexMeta);
        indexer.initialize();
        return indexer;
    }
}
