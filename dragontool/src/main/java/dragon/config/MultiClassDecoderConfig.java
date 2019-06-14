package dragon.config;

import dragon.ir.classification.multiclass.LossMultiClassDecoder;
import dragon.ir.classification.multiclass.MultiClassDecoder;
/**
 * <p>Multi-class Decoder Configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class MultiClassDecoderConfig extends ConfigUtil{
    public MultiClassDecoderConfig() {
       super();
    }

    public MultiClassDecoderConfig(ConfigureNode root){
       super(root);
    }

    public MultiClassDecoderConfig(String configFile){
        super(configFile);
    }

    public MultiClassDecoder getMultiClassDecoder(int decoderID){
        return getMultiClassDecoder(root,decoderID);
    }

    public MultiClassDecoder getMultiClassDecoder(ConfigureNode node, int decoderID){
        return loadMultiClassDecoder(node,decoderID);
    }

    private MultiClassDecoder loadMultiClassDecoder(ConfigureNode node, int decoderID){
        ConfigureNode decoderNode;
        String decoderName;

        decoderNode=getConfigureNode(node,"MultiClassDecoder",decoderID);
        if(decoderNode==null)
            return null;
        decoderName=decoderNode.getNodeName();
        return loadMultiClassDecoder(decoderName,decoderNode);
    }

    protected MultiClassDecoder loadMultiClassDecoder(String decoderName,ConfigureNode decoderNode){
        if(decoderName.equalsIgnoreCase("LossMultiClassDecoder"))
            return loadLossMultiClassDecoder(decoderNode);
        else
            return (MultiClassDecoder)loadResource(decoderNode);
    }

    private MultiClassDecoder loadLossMultiClassDecoder(ConfigureNode node){
        int lossFuncID;

        lossFuncID=node.getInt("lossfunction");
        if(lossFuncID<1)
            return null;
        return new LossMultiClassDecoder((new LossFunctionConfig()).getLossFunction(node,lossFuncID));
    }
}
