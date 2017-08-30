package dragon.config;

import dragon.ir.classification.multiclass.*;
/**
 * <p>Loss Function Configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LossFunctionConfig extends ConfigUtil{
    public LossFunctionConfig() {
       super();
    }

    public LossFunctionConfig(ConfigureNode root){
       super(root);
    }

    public LossFunctionConfig(String configFile){
        super(configFile);
    }

    public LossFunction getLossFunction(int lossFuncID){
        return getLossFunction(root,lossFuncID);
    }

    public LossFunction getLossFunction(ConfigureNode node, int lossFuncID){
        return loadLossFunction(node,lossFuncID);
    }

    private LossFunction loadLossFunction(ConfigureNode node, int lossFuncID){
        ConfigureNode lossFuncNode;
        String lossFuncName;

        lossFuncNode=getConfigureNode(node,"LossFunction",lossFuncID);
        if(lossFuncNode==null)
            return null;
        lossFuncName=lossFuncNode.getNodeName();
        return loadLossFunction(lossFuncName,lossFuncNode);
    }

    protected LossFunction loadLossFunction(String lossFuncName,ConfigureNode lossFuncNode){
        if(lossFuncName.equalsIgnoreCase("HingeLoss"))
            return new HingeLoss();
        else if(lossFuncName.equalsIgnoreCase("LinearLoss"))
            return new LinearLoss();
        else
            return (LossFunction)loadResource(lossFuncNode);
    }
}
