package dragon.config;

import dragon.nlp.tool.HeppleTagger;
import dragon.nlp.tool.MedPostTagger;
import dragon.nlp.tool.Tagger;

/**
 * <p>Tagger configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TaggerConfig extends ConfigUtil{
    public TaggerConfig() {
       super();
    }

    public TaggerConfig(ConfigureNode root){
       super(root);
    }

    public TaggerConfig(String configFile){
        super(configFile);
    }

    public Tagger getTagger(int taggerID){
        return getTagger(root,taggerID);
    }

    public Tagger getTagger(ConfigureNode node, int taggerID){
        return loadTagger(node,taggerID);
    }

    private Tagger loadTagger(ConfigureNode node, int taggerID){
        ConfigureNode taggerNode;
        String taggerName;

        taggerNode=getConfigureNode(node,"tagger",taggerID);
        if(taggerNode==null)
            return null;
        taggerName=taggerNode.getNodeName();
        return loadTagger(taggerName,taggerNode);
    }

    protected Tagger loadTagger(String taggerName, ConfigureNode taggerNode){
        String dataDir;

        dataDir=taggerNode.getString("directory",null);
        if(taggerName.equalsIgnoreCase("MedPostTagger")){
            if(dataDir==null)
                return new MedPostTagger();
            else
                return new MedPostTagger(dataDir);
        }
        else if(taggerName.equalsIgnoreCase("HeppleTagger")){
            if(dataDir==null)
                return new HeppleTagger();
            else
                return new HeppleTagger(dataDir);
        }
        else
            return (Tagger)loadResource(taggerNode);
    }
}
