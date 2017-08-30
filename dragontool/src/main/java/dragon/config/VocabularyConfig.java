package dragon.config;

import dragon.nlp.tool.*;
import dragon.nlp.ontology.*;
/**
 * <p>Vocabulary configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class VocabularyConfig extends ConfigUtil{
    public VocabularyConfig() {
       super();
    }

    public VocabularyConfig(ConfigureNode root){
       super(root);
    }

    public VocabularyConfig(String configFile){
        super(configFile);
    }

    public Vocabulary getVocabulary(int vocabularyID){
        return getVocabulary(root,vocabularyID);
    }

    public Vocabulary getVocabulary(ConfigureNode node, int vocabularyID){
        return loadVocabulary(node,vocabularyID);
    }

    private Vocabulary loadVocabulary(ConfigureNode node, int vocabularyID){
        ConfigureNode vocabularyNode;
        String vocabularyName;
        vocabularyNode=getConfigureNode(node,"vocabulary",vocabularyID);
        if(vocabularyNode==null)
            return null;
        vocabularyName=vocabularyNode.getNodeName();
        return loadVocabulary(vocabularyName,vocabularyNode);
    }

    protected Vocabulary loadVocabulary(String vocabularyName, ConfigureNode vocabularyNode){
        if(vocabularyName.equalsIgnoreCase("BasicVocabulary"))
            return loadBasicVocabulary(vocabularyNode);
        else
            return (Vocabulary)loadResource(vocabularyNode);
    }

    private Vocabulary loadBasicVocabulary(ConfigureNode curNode){
        LemmatiserConfig lemmaConfig;
        Lemmatiser lemmatiser;
        BasicVocabulary vocabulary;
        String vobFile, nonBoundaryPunc;
        int lemmatiserID;
        boolean lemmaOption, nppOption, coordinateOption, adjtermOption;

        vobFile=curNode.getString("vobfile");
        lemmatiserID=curNode.getInt("lemmatiser",0);
        lemmaOption=curNode.getBoolean("lemmaoption",false);
        nppOption=curNode.getBoolean("nppoption",false);
        coordinateOption=curNode.getBoolean("coordinateoption",false);
        adjtermOption=curNode.getBoolean("adjtermoption",false);
        nonBoundaryPunc=curNode.getString("nonboundarypunctuation","");

        lemmaConfig=new LemmatiserConfig();
        lemmatiser=lemmaConfig.getLemmatiser(curNode,lemmatiserID);
        vocabulary=new BasicVocabulary(vobFile,lemmatiser);
        vocabulary.setLemmaOption(lemmaOption);
        vocabulary.setAdjectivePhraseOption(adjtermOption);
        vocabulary.setCoordinateOption(coordinateOption);
        vocabulary.setNPPOption(nppOption);
        vocabulary.setNonBoundaryPunctuation(nonBoundaryPunc);
        return vocabulary;
    }
}
