package dragon.config;

import dragon.ir.classification.multiclass.*;
/**
 * <p>Code Matrix Configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CodeMatrixConfig extends ConfigUtil{
    public CodeMatrixConfig() {
       super();
    }

    public CodeMatrixConfig(ConfigureNode root){
       super(root);
    }

    public CodeMatrixConfig(String configFile){
        super(configFile);
    }

    public CodeMatrix getCodeMatrix(int codeMatrixID){
        return getCodeMatrix(root,codeMatrixID);
    }

    public CodeMatrix getCodeMatrix(ConfigureNode node, int codeMatrixID){
        return loadCodeMatrix(node,codeMatrixID);
    }

    private CodeMatrix loadCodeMatrix(ConfigureNode node, int codeMatrixID){
        ConfigureNode codeMatrixNode;
        String codeMatrixName;

        codeMatrixNode=getConfigureNode(node,"CodeMatrix",codeMatrixID);
        if(codeMatrixNode==null)
            return null;
        codeMatrixName=codeMatrixNode.getNodeName();
        return loadCodeMatrix(codeMatrixName,codeMatrixNode);
    }

    protected CodeMatrix loadCodeMatrix(String codeMatrixName,ConfigureNode codeMatrixNode){
        if(codeMatrixName.equalsIgnoreCase("OVACodeMatrix"))
            return new OVACodeMatrix(codeMatrixNode.getInt("classnum",1));
        else if(codeMatrixName.equalsIgnoreCase("AllPairCodeMatrix"))
            return new AllPairCodeMatrix(codeMatrixNode.getInt("classnum",1));
        else
            return (CodeMatrix)loadResource(codeMatrixNode);
    }
}
