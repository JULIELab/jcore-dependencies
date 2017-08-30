package dragon.config;

import dragon.ir.index.*;
import dragon.ir.index.sentence.*;
import dragon.ir.index.sequence.*;
import dragon.nlp.*;
import dragon.nlp.extract.*;
import dragon.onlinedb.CollectionReader;

/**
 * <p>Index reader configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IndexReaderConfig extends ConfigUtil{
    public IndexReaderConfig() {
       super();
    }

    public IndexReaderConfig(ConfigureNode root){
       super(root);
    }

    public IndexReaderConfig(String configFile){
        super(configFile);
    }

    public IRCollection getIRCollectionStat(int collectionID){
        return getIRCollectionStat(root,collectionID);
    }

    public IRCollection getIRCollectionStat(ConfigureNode node, int collectionID){
        IRCollection collection;
        ConfigureNode collectionNode;
        String collectionName;
        String indexFolder,indexSection;

        collectionNode=getConfigureNode(node,"collectionstat",collectionID);
        if(collectionNode==null)
            return null;
        collectionName=collectionNode.getNodeName();
        if(!collectionName.equalsIgnoreCase("IRCollection"))
            return null;
        indexFolder=collectionNode.getString("indexfolder");
        indexSection=collectionNode.getString("indexsection",null);
        collection=new IRCollection();
        if(indexSection==null || indexSection.trim().length()==0)
            collection.load(indexFolder+"/collection.stat");
        else
            collection.load(indexFolder+"/"+indexSection+"/collection.stat");
        return collection;
    }

    public IndexReader getIndexReader(int indexReaderID){
        return getIndexReader(root,indexReaderID);
    }

    public IndexReader getIndexReader(ConfigureNode node, int indexReaderID){
        return loadIndexReader(node,indexReaderID);
    }

    private IndexReader loadIndexReader(ConfigureNode node, int indexReaderID){
        ConfigureNode indexReaderNode;
        String indexReaderName;
        indexReaderNode=getConfigureNode(node,"indexreader",indexReaderID);
        if(indexReaderNode==null)
            return null;
        indexReaderName=indexReaderNode.getNodeName();
        return loadIndexReader(indexReaderName,indexReaderNode);
    }

    protected IndexReader loadIndexReader(String indexReaderName, ConfigureNode indexReaderNode){
        if(indexReaderName.equalsIgnoreCase("BasicIndexReader"))
            return loadBasicIndexReader(indexReaderNode);
        else if(indexReaderName.equalsIgnoreCase("OnlineIndexReader"))
            return loadOnlineIndexReader(indexReaderNode);
        else if(indexReaderName.equalsIgnoreCase("BasicSentenceIndexReader"))
            return loadBasicSentenceIndexReader(indexReaderNode);
        else if(indexReaderName.equalsIgnoreCase("OnlineSentenceIndexReader"))
            return loadOnlineSentenceIndexReader(indexReaderNode);
        else if(indexReaderName.equalsIgnoreCase("BasicSequenceIndexReader"))
            return loadBasicSequenceIndexReader(indexReaderNode);
        else if(indexReaderName.equalsIgnoreCase("OnlineSequenceIndexReader"))
            return loadOnlineSequenceIndexReader(indexReaderNode);
        else
            return (IndexReader)loadResource(indexReaderNode);
    }

    private IndexReader loadBasicSentenceIndexReader(ConfigureNode curNode){
        BasicSentenceIndexReader reader;
        String folder;

        folder=curNode.getString("indexfolder");
        reader=new BasicSentenceIndexReader(folder,true);
        reader.initialize();
        return reader;
    }

    private IndexReader loadOnlineSentenceIndexReader(ConfigureNode curNode){
        OnlineSentenceIndexReader reader;
        OnlineSentenceIndexer indexer;
        CollectionReader collectionReader;
        int collectionReaderID, indexerID;

        indexerID=curNode.getInt("onlineindexer");
        collectionReaderID=curNode.getInt("collectionreader");
        indexer=(OnlineSentenceIndexer)(new IndexerConfig()).getIndexer(curNode, indexerID);
        collectionReader=(new CollectionReaderConfig()).getCollectionReader(curNode,collectionReaderID);
        reader=new OnlineSentenceIndexReader(indexer,collectionReader);
        reader.initialize();
        return reader;
    }

    private IndexReader loadBasicSequenceIndexReader(ConfigureNode curNode){
        BasicSequenceIndexReader reader;
        CollectionReader collectionReader;
        String folder;
        int collectionReaderID;

        collectionReaderID=curNode.getInt("collectionreader",-1);
        if(collectionReaderID==-1)
            collectionReader=null;
        else
            collectionReader=(new CollectionReaderConfig()).getCollectionReader(curNode,collectionReaderID);
        folder=curNode.getString("indexfolder");
        reader=new BasicSequenceIndexReader(folder,collectionReader);
        reader.initialize();
        return reader;
    }

    private IndexReader loadOnlineSequenceIndexReader(ConfigureNode curNode){
        OnlineSequenceIndexReader reader;
        ConceptExtractor ce;
        OnlineSequenceIndexer indexer;
        CollectionReader collectionReader;
        int collectionReaderID, extractorID, indexerID;

        collectionReaderID=curNode.getInt("collectionreader");
        if(collectionReaderID<=0)
            return null;
        else
            collectionReader=(new CollectionReaderConfig()).getCollectionReader(curNode,collectionReaderID);
        extractorID=curNode.getInt("conceptextractor");
        if(extractorID>0){
            ce = (new ConceptExtractorConfig()).getConceptExtractor(curNode, extractorID);
            reader = new OnlineSequenceIndexReader(ce, collectionReader);
            reader.initialize();
            return reader;
        }
        else{
            indexerID=curNode.getInt("onlineindexer");
            indexer=(OnlineSequenceIndexer)(new IndexerConfig()).getIndexer(curNode,indexerID);
            if(indexer==null)
                return null;
            reader = new OnlineSequenceIndexReader(indexer, collectionReader);
            reader.initialize();
            return reader;
        }
    }

    private IndexReader loadOnlineIndexReader(ConfigureNode curNode){
        OnlineIndexReader reader;
        OnlineIndexer indexer;
        CollectionReader collectionReader;
        int collectionReaderID, indexerID;

        indexerID=curNode.getInt("onlineindexer");
        collectionReaderID=curNode.getInt("collectionreader");
        indexer=(OnlineIndexer)(new IndexerConfig()).getIndexer(curNode, indexerID);
        collectionReader=(new CollectionReaderConfig()).getCollectionReader(curNode,collectionReaderID);
        reader=new OnlineIndexReader(indexer,collectionReader);
        reader.initialize();
        return reader;
    }

    private IndexReader loadBasicIndexReader(ConfigureNode curNode){
        BasicIndexReader reader;
        CollectionReader collectionReader;
        String folder, indexSection;
        int collectionReaderID;

        collectionReaderID=curNode.getInt("collectionreader",-1);
        if(collectionReaderID==-1)
            collectionReader=null;
        else
            collectionReader=(new CollectionReaderConfig()).getCollectionReader(curNode,collectionReaderID);
        folder=curNode.getString("indexfolder");
        indexSection=curNode.getString("indexsection", null);
        if(indexSection==null || indexSection.trim().length()==0){
            reader=new BasicIndexReader(folder,true,collectionReader);
            reader.initialize();
        }
        else{
            reader=new BasicIndexReader(folder+"/"+indexSection,true, collectionReader);
            reader.initialize();
            reader.setIRDocKeyList(new SimpleElementList(folder + "/dockey.list", false));
            reader.setIRTermKeyList(new SimpleElementList(folder + "/termkey.list", false));
        }
        return reader;
    }
}
