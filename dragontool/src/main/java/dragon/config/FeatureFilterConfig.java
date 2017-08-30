package dragon.config;

import dragon.ir.clustering.featurefilter.*;

/**
 * <p>Feature filter configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FeatureFilterConfig extends ConfigUtil{
    public FeatureFilterConfig() {
       super();
    }

    public FeatureFilterConfig(ConfigureNode root){
       super(root);
    }

    public FeatureFilterConfig(String configFile){
        super(configFile);
    }

    public FeatureFilter getFeatureFilter(int filterID){
        return getFeatureFilter(root,filterID);
    }

    public FeatureFilter getFeatureFilter(ConfigureNode node, int filterID){
        return loadFeatureFilter(node,filterID);
    }

    private FeatureFilter loadFeatureFilter(ConfigureNode node, int filterID){
        ConfigureNode filterNode;
        String filterName;

        filterNode=getConfigureNode(node,"featurefilter",filterID);
        if(filterNode==null)
            return null;
        filterName=filterNode.getNodeName();
        return loadFeatureFilter(filterName,filterNode);
    }

    protected FeatureFilter loadFeatureFilter(String filterName, ConfigureNode filterNode){
        if(filterName.equalsIgnoreCase("NullFeatureFilter"))
            return new NullFeatureFilter();
        else if(filterName.equalsIgnoreCase("DocFrequencyFilter"))
            return loadDocFrequencySelector(filterNode);
        else
            return (FeatureFilter)loadResource(filterNode);
    }

    private FeatureFilter loadDocFrequencySelector(ConfigureNode node){
        return new DocFrequencyFilter(node.getInt("mindocfrequency",5));
    }
}
