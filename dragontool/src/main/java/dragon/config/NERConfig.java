package dragon.config;

import dragon.nlp.tool.*;

/**
 * <p>NER configuration</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class NERConfig extends ConfigUtil{
    public NERConfig() {
       super();
    }

    public NERConfig(ConfigureNode root){
       super(root);
    }

    public NERConfig(String configFile){
        super(configFile);
    }

    public NER getNER(int taggerID){
        return getNER(root,taggerID);
    }

    public NER getNER(ConfigureNode node, int nerID){
        return loadNER(node,nerID);
    }

    private NER loadNER(ConfigureNode node, int nerID){
        ConfigureNode nerNode;
        String nerName;
        nerNode=getConfigureNode(node,"ner",nerID);
        if(nerNode==null)
            return null;

        nerName=nerNode.getNodeName();
        return loadNER(nerName,nerNode);
    }

    protected NER loadNER(String nerName, ConfigureNode nerNode){
        if(nerName.equalsIgnoreCase("Annie"))
            return loadAnnie(nerNode);
        else
            return (NER)loadResource(nerNode);
    }

    private Annie loadAnnie(ConfigureNode curNode){
        Annie ner;
        String gateHome, annotationTypes;

        try{
            gateHome = curNode.getString("gatehome", null);
            annotationTypes = curNode.getString("entitytypes", "Person;Location;Organization");

            if(gateHome==null)
                ner=new Annie();
            else
                ner = new Annie(gateHome);
            ner.setAnnotationTypes(annotationTypes.split(";"));
            return ner;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
