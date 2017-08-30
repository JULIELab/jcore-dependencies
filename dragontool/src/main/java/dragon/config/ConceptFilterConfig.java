package dragon.config;

import dragon.nlp.extract.*;

/**
 * <p>Concept filter configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ConceptFilterConfig extends ConfigUtil{
    public ConceptFilterConfig() {
       super();
    }

    public ConceptFilterConfig(ConfigureNode root){
       super(root);
    }

    public ConceptFilterConfig(String configFile){
        super(configFile);
    }

    public ConceptFilter getConceptFilter(int taggerID){
        return getConceptFilter(root,taggerID);
    }

    public ConceptFilter getConceptFilter(ConfigureNode node, int conceptFilterID){
        return loadConceptFilter(node,conceptFilterID);
    }

    private ConceptFilter loadConceptFilter(ConfigureNode node, int conceptFilterID){
        ConfigureNode conceptFilterNode;
        String conceptFilterName;
        conceptFilterNode=getConfigureNode(node,"conceptFilter",conceptFilterID);
        if(conceptFilterNode==null)
            return null;

        conceptFilterName=conceptFilterNode.getNodeName();
        if(conceptFilterName.equalsIgnoreCase("BasicConceptFilter"))
            return loadBasicConceptFilter(conceptFilterNode);
        else
            return (ConceptFilter)loadResource(conceptFilterNode);
    }

    private ConceptFilter loadBasicConceptFilter(ConfigureNode curNode){
        BasicConceptFilter conceptFilter;
        String stoplistFile, excludedSTYFile, supportedSTYFile;
        String excludedSTY, supportedSTY;

        stoplistFile=curNode.getString("stoplistfile",null);
        excludedSTYFile=curNode.getString("excludedstyfile",null);
        supportedSTYFile=curNode.getString("supportedstyfile",null);
        excludedSTY=curNode.getString("excludedsty",null);
        supportedSTY=curNode.getString("supportedsty",null);
        conceptFilter=new BasicConceptFilter(stoplistFile,supportedSTYFile,excludedSTYFile);
        if(excludedSTY!=null && excludedSTY.trim().length()>0)
            conceptFilter.addMultiExcludedSTY(excludedSTY.trim());
        if(supportedSTY!=null && supportedSTY.trim().length()>0)
            conceptFilter.addMultiSupportedSTY(supportedSTY.trim());
        return conceptFilter;
    }
}
