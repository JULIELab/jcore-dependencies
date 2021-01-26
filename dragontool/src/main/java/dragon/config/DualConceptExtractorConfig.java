package dragon.config;

import dragon.nlp.extract.DualConceptExtractor;

/**
 * <p>Dual concept extraction configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DualConceptExtractorConfig extends ConfigUtil{
    public DualConceptExtractorConfig() {
       super();
    }

    public DualConceptExtractorConfig(ConfigureNode root){
       super(root);
    }

    public DualConceptExtractorConfig(String configFile){
        super(configFile);
    }

    public DualConceptExtractor getDualConceptExtractor(int extractorID){
        return loadDualConceptExtractor(root,extractorID);
    }

    public DualConceptExtractor getDualConceptExtractor(ConfigureNode node, int extractorID){
        return loadDualConceptExtractor(node,extractorID);
    }

    private DualConceptExtractor loadDualConceptExtractor(ConfigureNode node, int extractorID){
        ConfigureNode extractorNode;
        String extractorName;

        extractorNode=getConfigureNode(node,"dualconceptextractor",extractorID);
        if(extractorNode==null)
            return null;
        extractorName=extractorNode.getNodeName();
        return loadDualConceptExtractor(extractorName,extractorNode);
    }

    protected DualConceptExtractor loadDualConceptExtractor(String extractorName,ConfigureNode extractorNode){
        return (DualConceptExtractor)loadResource(extractorNode);
    }
}
