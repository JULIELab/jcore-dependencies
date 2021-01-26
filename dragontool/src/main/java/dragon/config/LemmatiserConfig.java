package dragon.config;

import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.PorterStemmer;
import dragon.nlp.tool.WordNetDidion;
import dragon.nlp.tool.lemmatiser.EngLemmatiser;

/**
 * <p>Lemmatiser configuration </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LemmatiserConfig extends ConfigUtil{
    public LemmatiserConfig() {
       super();
    }

    public LemmatiserConfig(ConfigureNode root){
       super(root);
    }

    public LemmatiserConfig(String configFile){
        super(configFile);
    }

    public Lemmatiser getLemmatiser(int lemmatiserID){
        return getLemmatiser(root,lemmatiserID);
    }

    public Lemmatiser getLemmatiser(ConfigureNode node, int lemmatiserID){
        return loadLemmatiser(node,lemmatiserID);
    }

    private Lemmatiser loadLemmatiser(ConfigureNode node, int lemmatiserID){
        ConfigureNode lemmatiserNode;
        String lemmatiserName;

        lemmatiserNode=getConfigureNode(node,"lemmatiser",lemmatiserID);
        if(lemmatiserNode==null)
            return null;
        lemmatiserName=lemmatiserNode.getNodeName();
        return loadLemmatiser(lemmatiserName,lemmatiserNode);
    }

    protected Lemmatiser loadLemmatiser(String lemmatiserName,ConfigureNode lemmatiserNode){
        if(lemmatiserName.equalsIgnoreCase("PorterStemmer"))
            return new PorterStemmer();
        else if(lemmatiserName.equalsIgnoreCase("WordNetDidion"))
            return loadWordNetDidion(lemmatiserNode);
        else if(lemmatiserName.equalsIgnoreCase("EngLemmatiser"))
            return loadEngLemmatiser(lemmatiserNode);
        else
            return (Lemmatiser)loadResource(lemmatiserNode);
    }

    private Lemmatiser loadEngLemmatiser(ConfigureNode node){
        String directory;
        boolean indexLookupOption;
        boolean disableVerbAdjective;

        indexLookupOption=false;
        disableVerbAdjective=true;
        directory=node.getString("directory",null);
        indexLookupOption=node.getBoolean("indexlookupoption",indexLookupOption);
        disableVerbAdjective=node.getBoolean("disableverbadjective",disableVerbAdjective);
        if(directory==null)
            return new EngLemmatiser(indexLookupOption,disableVerbAdjective);
        else
            return new EngLemmatiser(directory,indexLookupOption,disableVerbAdjective);
    }

    private Lemmatiser loadWordNetDidion(ConfigureNode node){
        String directory;

        directory=node.getString("directory",null);
        if(directory==null)
            return new WordNetDidion();
        else
            return new WordNetDidion(directory);
    }
}