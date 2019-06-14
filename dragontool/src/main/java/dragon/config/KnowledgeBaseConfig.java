package dragon.config;

import dragon.ir.kngbase.BasicKnowledgeBase;
import dragon.ir.kngbase.HALSpace;
import dragon.ir.kngbase.KnowledgeBase;
import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.SimpleElementList;
import dragon.nlp.extract.TokenExtractor;
import dragon.onlinedb.CollectionReader;

/**
 * <p>Knowledge base configuration</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class KnowledgeBaseConfig extends ConfigUtil{
    public KnowledgeBaseConfig() {
       super();
    }

    public KnowledgeBaseConfig(ConfigureNode root){
       super(root);
    }

    public KnowledgeBaseConfig(String configFile){
        super(configFile);
    }

    public KnowledgeBase getKnowledgeBase(int kngBaseID){
        return getKnowledgeBase(root,kngBaseID);
    }

    public KnowledgeBase getKnowledgeBase(ConfigureNode node, int kngBaseID){
        return loadKnowledgeBase(node,kngBaseID);
    }

    private KnowledgeBase loadKnowledgeBase(ConfigureNode node, int kngBaseID){
        ConfigureNode kngBaseNode;
        String kngBaseName;

        kngBaseNode=getConfigureNode(node,"knowledgebase",kngBaseID);
        if(kngBaseNode==null)
            return null;
        kngBaseName=kngBaseNode.getNodeName();
        return loadKnowledgeBase(kngBaseName,kngBaseNode);
    }

    protected KnowledgeBase loadKnowledgeBase(String kngBaseName,ConfigureNode kngBaseNode){
        if(kngBaseName.equalsIgnoreCase("BasicKnowledgeBase"))
            return loadBasicKnowledgeBase(kngBaseNode);
        else if(kngBaseName.equalsIgnoreCase("HALSpace"))
            return loadHALSpace(kngBaseNode);
        else
            return (KnowledgeBase)loadResource(kngBaseNode);
    }

    private KnowledgeBase loadBasicKnowledgeBase(ConfigureNode node){
        DoubleSparseMatrix kngMatrix;
        String rowKeyListFile, columnKeyListFile;
        int kngMatrixID;

        rowKeyListFile=node.getString("rowkeyfile");
        columnKeyListFile=node.getString("columnkeyfile");
        kngMatrixID=node.getInt("knowledgematrix");
        kngMatrix=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,kngMatrixID);
        return new BasicKnowledgeBase(kngMatrix,new SimpleElementList(rowKeyListFile,false), new SimpleElementList(columnKeyListFile,false));
    }

    private KnowledgeBase loadHALSpace(ConfigureNode node){
        HALSpace hal;
        TokenExtractor extractor;
        CollectionReader reader;
        String termListFile,matrixFile, indexFile;
        int windowSize, tokenExtractorID, collectionID;

        matrixFile=node.getString("matrixfile");
        indexFile=node.getString("indexfile");
        termListFile=node.getString("termkeyfile");
        windowSize=node.getInt("windowsize");
        collectionID=node.getInt("collectionreader");
        reader=(new CollectionReaderConfig()).getCollectionReader(node,collectionID);
        tokenExtractorID=node.getInt("tokenextractor");
        extractor=(TokenExtractor)(new ConceptExtractorConfig()).getConceptExtractor(node,tokenExtractorID);
        if(matrixFile!=null && indexFile!=null)
            hal=new HALSpace(new SimpleElementList(termListFile,false),extractor, windowSize, indexFile, matrixFile);
        else if(termListFile!=null)
            hal=new HALSpace(new SimpleElementList(termListFile,false),extractor, windowSize);
        else
            hal=new HALSpace(extractor, windowSize);
        if(reader!=null){
            hal.add(reader);
            hal.finalizeData();
        }
        return hal;
    }
}
