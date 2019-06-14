package dragon.config;

import dragon.nlp.ontology.BasicOntology;
import dragon.nlp.ontology.Ontology;
import dragon.nlp.ontology.umls.UmlsAmbiguityOntology;
import dragon.nlp.ontology.umls.UmlsFileBackedOntology;
import dragon.nlp.tool.Lemmatiser;

/**
 * <p>Ontology application configuration</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OntologyConfig extends ConfigUtil{
    public OntologyConfig() {
       super();
    }

    public OntologyConfig(ConfigureNode root){
       super(root);
    }

    public OntologyConfig(String configFile){
        super(configFile);
    }

    public Ontology getOntology(int taggerID){
        return getOntology(root,taggerID);
    }

    public Ontology getOntology(ConfigureNode node, int ontologyID){
        return loadOntology(node,ontologyID);
    }

    private Ontology loadOntology(ConfigureNode node, int ontologyID){
        ConfigureNode ontologyNode;
        String ontologyName;
        ontologyNode=getConfigureNode(node,"ontology",ontologyID);
        if(ontologyNode==null)
            return null;
        ontologyName=ontologyNode.getNodeName();
        return loadOntology(ontologyName,node,ontologyNode);
    }

    protected Ontology loadOntology(String ontologyName, ConfigureNode node, ConfigureNode ontologyNode){
        if(ontologyName.equalsIgnoreCase("BasicOntology"))
            return loadBasicOntology(ontologyNode);
        else if(ontologyName.equalsIgnoreCase("UmlsExactOntology"))
            return loadUmlsExactOntology(ontologyNode);
        else if(ontologyName.equalsIgnoreCase("UmlsAmbiguityOntology"))
            return loadUmlsAmbiguityOntology(ontologyNode);
        else
            return (Ontology)loadResource(ontologyNode);
    }

    private Ontology loadBasicOntology(ConfigureNode curNode){
        LemmatiserConfig lemmaConfig;
        Lemmatiser lemmatiser;
        BasicOntology ontology;
        String termFile, nonBoundaryPunc;
        int lemmatiserID;
        boolean lemmaOption, nppOption, coordinateOption, adjtermOption, senseOption;

        termFile=curNode.getString("termfile");
        lemmatiserID=curNode.getInt("lemmatiser",0);
        lemmaOption=curNode.getBoolean("lemmaoption",false);
        nppOption=curNode.getBoolean("nppoption",false);
        coordinateOption=curNode.getBoolean("coordinateoption",false);
        adjtermOption=curNode.getBoolean("adjtermoption",false);
        senseOption=curNode.getBoolean("sensedisambiguation",false);
        nonBoundaryPunc=curNode.getString("nonboundarypunctuation","");

        lemmaConfig=new LemmatiserConfig();
        lemmatiser=lemmaConfig.getLemmatiser(curNode,lemmatiserID);
        ontology=new BasicOntology(termFile,lemmatiser);
        ontology.setLemmaOption(lemmaOption);
        ontology.setAdjectiveTermOption(adjtermOption);
        ontology.setCoordinateOption(coordinateOption);
        ontology.setNPPOption(nppOption);
        ontology.setSenseDisambiguationOption(senseOption);
        ontology.setNonBoundaryPunctuation(nonBoundaryPunc);
        return ontology;
    }

    private Ontology loadUmlsExactOntology(ConfigureNode vobNode){
        LemmatiserConfig lemmaConfig;
        Lemmatiser lemmatiser;
        UmlsFileBackedOntology ontology;
        String directory, nonBoundaryPunc;
        int lemmatiserID;
        boolean lemmaOption, nppOption, coordinateOption, adjtermOption, senseOption;

        directory=vobNode.getString("directory",null);
        lemmatiserID=vobNode.getInt("lemmatiser",0);
        lemmaOption=vobNode.getBoolean("lemmaoption",false);
        nppOption=vobNode.getBoolean("nppoption",false);
        coordinateOption=vobNode.getBoolean("coordinateoption",false);
        adjtermOption=vobNode.getBoolean("adjtermoption",false);
        senseOption=vobNode.getBoolean("sensedisambiguation",false);
        nonBoundaryPunc=vobNode.getString("nonboundarypunctuation","");

        lemmaConfig=new LemmatiserConfig();
        lemmatiser=lemmaConfig.getLemmatiser(vobNode,lemmatiserID);
        if(directory==null)
            ontology=new UmlsFileBackedOntology(lemmatiser);
        else
            ontology=new UmlsFileBackedOntology(directory,lemmatiser);
        ontology.setLemmaOption(lemmaOption);
        ontology.setAdjectiveTermOption(adjtermOption);
        ontology.setCoordinateOption(coordinateOption);
        ontology.setNPPOption(nppOption);
        ontology.setSenseDisambiguationOption(senseOption);
        ontology.setNonBoundaryPunctuation(nonBoundaryPunc);
        return ontology;
    }

    private Ontology loadUmlsAmbiguityOntology(ConfigureNode vobNode){
        LemmatiserConfig lemmaConfig;
        Lemmatiser lemmatiser;
        UmlsAmbiguityOntology ontology;
        String directory, nonBoundaryPunc;
        int lemmatiserID;
        boolean lemmaOption, nppOption, coordinateOption, adjtermOption, senseOption;
        double minScore, minSelectivity;
        int maxSkippedWord;

        directory=vobNode.getString("directory",null);
        lemmatiserID=vobNode.getInt("lemmatiser",0);
        lemmaOption=vobNode.getBoolean("lemmaoption",false);
        nppOption=vobNode.getBoolean("nppoption",false);
        coordinateOption=vobNode.getBoolean("coordinateoption",false);
        adjtermOption=vobNode.getBoolean("adjtermoption",false);
        senseOption=vobNode.getBoolean("sensedisambiguation",false);
        nonBoundaryPunc=vobNode.getString("nonboundarypunctuation","");
        minScore=vobNode.getDouble("minscore",0.95);
        minSelectivity=vobNode.getDouble("minselectivity",0);
        maxSkippedWord=vobNode.getInt("maxskippedword",1);

        lemmaConfig=new LemmatiserConfig();
        lemmatiser=lemmaConfig.getLemmatiser(vobNode,lemmatiserID);
        if(directory==null)
            ontology=new UmlsAmbiguityOntology(lemmatiser);
        else
            ontology=new UmlsAmbiguityOntology(directory,lemmatiser);
        ontology.setLemmaOption(lemmaOption);
        ontology.setAdjectiveTermOption(adjtermOption);
        ontology.setCoordinateOption(coordinateOption);
        ontology.setNPPOption(nppOption);
        ontology.setSenseDisambiguationOption(senseOption);
        ontology.setNonBoundaryPunctuation(nonBoundaryPunc);
        ontology.setMinScore(minScore);
        ontology.setMinSelectivity(minSelectivity);
        ontology.setMaxSkippedWords(maxSkippedWord);
        return ontology;
    }
}
