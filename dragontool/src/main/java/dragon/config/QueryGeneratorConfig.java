package dragon.config;

import dragon.ir.kngbase.KnowledgeBase;
import dragon.ir.query.BasicQueryGenerator;
import dragon.ir.query.PhraseQEGenerator;
import dragon.ir.query.QueryGenerator;
import dragon.nlp.extract.PhraseExtractor;
import dragon.nlp.extract.TokenExtractor;

/**
 * <p>Query generator configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class QueryGeneratorConfig extends ConfigUtil{
    public QueryGeneratorConfig() {
       super();
    }

    public QueryGeneratorConfig(ConfigureNode root){
       super(root);
    }

    public QueryGeneratorConfig(String configFile){
        super(configFile);
    }

    public QueryGenerator getQueryGenerator(int queryGeneratorID){
        return getQueryGenerator(root,queryGeneratorID);
    }

    public QueryGenerator getQueryGenerator(ConfigureNode node, int queryGeneratorID){
        return loadQueryGenerator(node,queryGeneratorID);
    }

    private QueryGenerator loadQueryGenerator(ConfigureNode node, int queryGeneratorID){
        ConfigureNode queryGeneratorNode;
        String queryGeneratorName;

        queryGeneratorNode=getConfigureNode(node,"queryGenerator",queryGeneratorID);
        if(queryGeneratorNode==null)
            return null;
        queryGeneratorName=queryGeneratorNode.getNodeName();
        return loadQueryGenerator(queryGeneratorName,queryGeneratorNode);
    }

    protected QueryGenerator loadQueryGenerator(String queryGeneratorName,ConfigureNode queryGeneratorNode){
        if(queryGeneratorName.equalsIgnoreCase("BasicQueryGenerator"))
            return loadBasicQueryGenerator(queryGeneratorNode);
        else if(queryGeneratorName.equalsIgnoreCase("PhraseQEGenerator"))
            return loadPhraseQEGenerator(queryGeneratorNode);
        else
            return (QueryGenerator)loadResource(queryGeneratorNode);
    }

    private QueryGenerator loadBasicQueryGenerator(ConfigureNode node){
        BasicQueryGenerator generator;
        boolean useConcept;
        int tripleExtractorID, conceptExtractorID;
        double title, body, abt, meta, subterm;

        generator=null;
        useConcept=node.getBoolean("useconcept",false);
        tripleExtractorID=node.getInt("tripleextractor",0);
        if(tripleExtractorID<=0){
            conceptExtractorID=node.getInt("conceptextractor");
            generator=new BasicQueryGenerator((new ConceptExtractorConfig()).getConceptExtractor(node,conceptExtractorID),useConcept);
        }
        else{
            generator=new BasicQueryGenerator((new TripleExtractorConfig()).getTripleExtractor(node,tripleExtractorID),useConcept);
        }
        title=node.getDouble("titleweight",0);
        body=node.getDouble("bodyweight",0);
        abt=node.getDouble("abstractweight",0);
        meta=node.getDouble("metaweight",0);
        subterm=node.getDouble("subterm",1);
        generator.initialize(title,abt,body,meta,subterm);
        return generator;
    }

    private QueryGenerator loadPhraseQEGenerator(ConfigureNode node){
        PhraseQEGenerator generator;
        KnowledgeBase kngBase;
        PhraseExtractor phraseExtractor;
        TokenExtractor tokenExtractor;
        double transCoefficient;
        int expandTermNum, kngBaseID, tokenExtractorID, phraseExtractorID;
        boolean useTitle, useBody, useAbstract, useMeta;

        useTitle=node.getBoolean("usetitle",true);
        useAbstract=node.getBoolean("useabstract",false);
        useBody=node.getBoolean("usebody",false);
        useMeta=node.getBoolean("usemeta",false);
        expandTermNum=node.getInt("expandtermnum",10);
        transCoefficient=node.getDouble("transcoefficient",0.5) ;
        tokenExtractorID=node.getInt("tokenextractor");
        phraseExtractorID=node.getInt("phraseextractor");
        kngBaseID=node.getInt("knowledgebase");
        kngBase=(new KnowledgeBaseConfig()).getKnowledgeBase(node,kngBaseID);
        tokenExtractor=(TokenExtractor)(new ConceptExtractorConfig()).getConceptExtractor(node,tokenExtractorID);
        if(phraseExtractorID>0){
            phraseExtractor=(PhraseExtractor)(new ConceptExtractorConfig()).getConceptExtractor(node,phraseExtractorID);
            generator = new PhraseQEGenerator(kngBase, phraseExtractor, tokenExtractor, transCoefficient, expandTermNum);
        }
        else
            generator = new PhraseQEGenerator(kngBase, tokenExtractor, transCoefficient, expandTermNum);
        generator.initialize(useTitle, useAbstract, useBody, useMeta);
        return generator;
    }
}
