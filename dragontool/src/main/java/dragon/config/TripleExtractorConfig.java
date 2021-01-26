package dragon.config;

import dragon.nlp.extract.BasicTripleExtractor;
import dragon.nlp.extract.ConceptFilter;
import dragon.nlp.extract.TermExtractor;
import dragon.nlp.extract.TripleExtractor;

/**
 * <p>Triple extraction configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TripleExtractorConfig extends ConfigUtil{
    public TripleExtractorConfig() {
       super();
    }

    public TripleExtractorConfig(ConfigureNode root){
       super(root);
    }

    public TripleExtractorConfig(String configFile){
        super(configFile);
    }

    public TripleExtractor getTripleExtractor(int tripleExtractorID){
        return getTripleExtractor(root,tripleExtractorID);
    }

    public TripleExtractor getTripleExtractor(ConfigureNode node, int tripleExtractorID){
        return loadTripleExtractor(node,tripleExtractorID);
    }

    private TripleExtractor loadTripleExtractor(ConfigureNode node, int tripleExtractorID){
        ConfigureNode tripleExtractorNode;
        String tripleExtractorName;

        tripleExtractorNode=getConfigureNode(node,"tripleextractor",tripleExtractorID);
        if(tripleExtractorNode==null)
            return null;
        tripleExtractorName=tripleExtractorNode.getNodeName();
        return loadTripleExtractor(tripleExtractorName,tripleExtractorNode);
    }

    protected TripleExtractor loadTripleExtractor(String tripleExtractorName,ConfigureNode tripleExtractorNode){
        if(tripleExtractorName.equalsIgnoreCase("BasicTripleExtractor"))
            return loadBasicTripleExtractor(tripleExtractorNode);
        else
            return (TripleExtractor)loadResource(tripleExtractorNode);
    }

    private TripleExtractor loadBasicTripleExtractor(ConfigureNode curNode){
        ConceptExtractorConfig extractorConfig;
        TermExtractor extractor;
        ConceptFilterConfig filterConfig;
        ConceptFilter filter;
        BasicTripleExtractor tripleExtractor;
        int extractorID, filterID;
        boolean filterOption, semanticCheck, relationCheck, coreference, clauseIdentify, coordinatingCheck;

        extractorID=curNode.getInt("conceptextractor",0);
        filterID=curNode.getInt("conceptfilter",0);
        filterOption=curNode.getBoolean("filteroption",true);
        relationCheck=curNode.getBoolean("relationcheck",true);
        coreference=curNode.getBoolean("coreference",false);
        coordinatingCheck=curNode.getBoolean("coordinatingcheck",false);
        semanticCheck=curNode.getBoolean("semanticcheck",true);
        clauseIdentify=curNode.getBoolean("clauseidentify",true);

        extractorConfig=new ConceptExtractorConfig();
        extractor=(TermExtractor)extractorConfig.getConceptExtractor(curNode,extractorID);
        filterConfig=new ConceptFilterConfig();
        filter=filterConfig.getConceptFilter(curNode,filterID);
        tripleExtractor=new BasicTripleExtractor(extractor);
        tripleExtractor.setConceptFilter(filter);
        tripleExtractor.setFilteringOption(filterOption);
        tripleExtractor.setCoordinatingCheckOption(coordinatingCheck);
        tripleExtractor.setRelationCheckOption(relationCheck);
        tripleExtractor.setSemanticCheckOption(semanticCheck);
        tripleExtractor.setCoReferenceOption(coreference);
        tripleExtractor.setClauseIdentifyOption(clauseIdentify);
        return tripleExtractor;
    }
}
