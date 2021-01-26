package dragon.config;

import dragon.ir.classification.featureselection.*;

/**
 * <p>Feature selector configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FeatureSelectorConfig extends ConfigUtil{
    public FeatureSelectorConfig() {
       super();
    }

    public FeatureSelectorConfig(ConfigureNode root){
       super(root);
    }

    public FeatureSelectorConfig(String configFile){
        super(configFile);
    }

    public FeatureSelector getFeatureSelector(int selectorID){
        return getFeatureSelector(root,selectorID);
    }

    public FeatureSelector getFeatureSelector(ConfigureNode node, int selectorID){
        return loadFeatureSelector(node,selectorID);
    }

    private FeatureSelector loadFeatureSelector(ConfigureNode node, int selectorID){
        ConfigureNode selectorNode;
        String selectorName;

        selectorNode=getConfigureNode(node,"featureselector",selectorID);
        if(selectorNode==null)
            return null;
        selectorName=selectorNode.getNodeName();
        return loadFeatureSelector(selectorName,selectorNode);
    }

    protected FeatureSelector loadFeatureSelector(String selectorName, ConfigureNode selectorNode){
        if(selectorName.equalsIgnoreCase("NullFeatureSelector"))
            return new NullFeatureSelector();
        else if(selectorName.equalsIgnoreCase("ChiFeatureSelector"))
            return loadChiFeatureSelector(selectorNode);
        else if(selectorName.equalsIgnoreCase("DocFrequencySelector"))
            return loadDocFrequencySelector(selectorNode);
        else if(selectorName.equalsIgnoreCase("MutualInfoSelector"))
            return loadMutualInfoFeatureSelector(selectorNode);
        else if(selectorName.equalsIgnoreCase("InfoGainFeatureSelector"))
            return loadInfoGainFeatureSelector(selectorNode);
        else
            return (FeatureSelector)loadResource(selectorNode);
    }

    private FeatureSelector loadChiFeatureSelector(ConfigureNode node){
        return new ChiFeatureSelector(node.getDouble("toppercentage",0.1),node.getBoolean("avgmode",true));
    }

    private FeatureSelector loadMutualInfoFeatureSelector(ConfigureNode node){
        return new MutualInfoFeatureSelector(node.getDouble("toppercentage",0.1),node.getBoolean("avgmode",true));
    }

    private FeatureSelector loadInfoGainFeatureSelector(ConfigureNode node){
        return new InfoGainFeatureSelector(node.getDouble("toppercentage",0.1));
    }

    private FeatureSelector loadDocFrequencySelector(ConfigureNode node){
        return new DocFrequencySelector(node.getInt("mindocfrequency",5));
    }
}
