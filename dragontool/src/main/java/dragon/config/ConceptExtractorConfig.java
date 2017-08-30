package dragon.config;

import dragon.nlp.*;
import dragon.nlp.extract.*;
import dragon.nlp.tool.*;
import dragon.nlp.ontology.*;


/**
 * <p>Concept extractor configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ConceptExtractorConfig extends ConfigUtil{
    public ConceptExtractorConfig() {
       super();
    }

    public ConceptExtractorConfig(ConfigureNode root){
       super(root);
    }

    public ConceptExtractorConfig(String configFile){
        super(configFile);
    }

    public ConceptExtractor getConceptExtractor(int extractorID){
        return loadConceptExtractor(root,extractorID);
    }

    public ConceptExtractor getConceptExtractor(ConfigureNode node, int extractorID){
        return loadConceptExtractor(node,extractorID);
    }

    private ConceptExtractor loadConceptExtractor(ConfigureNode node, int extractorID){
        ConfigureNode extractorNode;
        String extractorName;

        extractorNode=getConfigureNode(node,"conceptextractor",extractorID);
        if(extractorNode==null)
            return null;
        extractorName=extractorNode.getNodeName();
        return loadConceptExtractor(extractorName,extractorNode);
    }

    protected ConceptExtractor loadConceptExtractor(String extractorName,ConfigureNode extractorNode){
        if(extractorName.equalsIgnoreCase("BasicTokenExtractor"))
            return loadBasicTokenExtractor(extractorNode);
        else if(extractorName.equalsIgnoreCase("BasicPhraseExtractor"))
            return loadBasicPhraseExtractor(extractorNode);
        else if(extractorName.equalsIgnoreCase("BasicTermExtractor"))
            return loadBasicTermExtractor(extractorNode);
        else
            return (ConceptExtractor)loadResource(extractorNode);
    }

    private ConceptExtractor loadBasicTokenExtractor(ConfigureNode curNode){
        BasicTokenExtractor extractor;
        LemmatiserConfig lemmaConfig;
        Lemmatiser lemmatiser;
        ConceptFilterConfig filterConfig;
        ConceptFilter filter;
        DocumentParser parser;
        String wordDelimitor;
        int lemmatiserID, filterID, parserID;
        boolean subtermOption, filterOption;

        parserID=curNode.getInt("documentparser",0);
        lemmatiserID=curNode.getInt("lemmatiser",0);
        filterID=curNode.getInt("conceptfilter",0);
        filterOption=curNode.getBoolean("filteroption",true);
        subtermOption=curNode.getBoolean("subconceptoption",false);
        wordDelimitor=getWordDelimitor(curNode.getString("notworddelimitor",""));

        if(parserID>0)
            parser=(new DocumentParserConfig()).getDocumentParser(curNode,parserID);
        else
            parser=new EngDocumentParser(wordDelimitor);
        lemmaConfig=new LemmatiserConfig();
        lemmatiser=lemmaConfig.getLemmatiser(curNode,lemmatiserID);
        filterConfig=new ConceptFilterConfig();
        filter=filterConfig.getConceptFilter(curNode,filterID);
        extractor=new BasicTokenExtractor(lemmatiser);
        extractor.setConceptFilter(filter);
        extractor.setSubConceptOption(subtermOption);
        extractor.setFilteringOption(filterOption);
        extractor.setDocumentParser(parser);
        return extractor;
    }

    private ConceptExtractor loadBasicPhraseExtractor(ConfigureNode curNode){
        BasicPhraseExtractor extractor;
        LemmatiserConfig lemmaConfig;
        Lemmatiser lemmatiser;
        TaggerConfig taggerConfig;
        Tagger tagger;
        VocabularyConfig vobConfig;
        Vocabulary vob;
        ConceptFilterConfig filterConfig;
        ConceptFilter filter;
        DocumentParser parser;
        String wordDelimitor, nameMode;
        int lemmatiserID, filterID, vobID, taggerID, parserID;
        boolean overlappedPhrase, subtermOption, filterOption, singleNoun, singleVerb, singleAdj;

        parserID=curNode.getInt("documentparser",0);
        lemmatiserID=curNode.getInt("lemmatiser",0);
        filterID=curNode.getInt("conceptfilter",0);
        vobID=curNode.getInt("vocabulary",0);
        taggerID=curNode.getInt("tagger",0);
        filterOption=curNode.getBoolean("filteroption",true);
        subtermOption=curNode.getBoolean("subconceptoption",false);
        overlappedPhrase=curNode.getBoolean("overlappedphrase",false);
        wordDelimitor=getWordDelimitor(curNode.getString("notworddelimitor",""));
        nameMode=curNode.getString("conceptnamemode","asis");
        singleNoun=curNode.getBoolean("singlenounoption",true);
        singleAdj=curNode.getBoolean("singleadjoption",true);
        singleVerb=curNode.getBoolean("singleverboption",false);

        if(parserID>0)
            parser=(new DocumentParserConfig()).getDocumentParser(curNode,parserID);
        else
            parser=new EngDocumentParser(wordDelimitor);
        lemmaConfig=new LemmatiserConfig();
        lemmatiser=lemmaConfig.getLemmatiser(curNode,lemmatiserID);
        vobConfig=new VocabularyConfig();
        vob=vobConfig.getVocabulary(curNode,vobID);
        taggerConfig=new TaggerConfig();
        tagger=taggerConfig.getTagger(curNode,taggerID);
        filterConfig=new ConceptFilterConfig();
        filter=filterConfig.getConceptFilter(curNode,filterID);
        extractor=new BasicPhraseExtractor(vob,lemmatiser,tagger, overlappedPhrase);
        extractor.setDocumentParser(parser);
        if(nameMode.equalsIgnoreCase("lemma"))
            Phrase.setNameMode(Phrase.NAME_LEMMA);
        else
            Phrase.setNameMode(Phrase.NAME_ASIS);
        extractor.setConceptFilter(filter);
        extractor.setSubConceptOption(subtermOption);
        extractor.setFilteringOption(filterOption);
        extractor.setSingleAdjectiveOption(singleAdj);
        extractor.setSingleNounOption(singleNoun);
        extractor.setSingleVerbOption(singleVerb);
        return extractor;
    }

    private ConceptExtractor loadBasicTermExtractor(ConfigureNode curNode){
        BasicTermExtractor extractor;
        LemmatiserConfig lemmaConfig;
        Lemmatiser lemmatiser;
        TaggerConfig taggerConfig;
        Tagger tagger;
        OntologyConfig ontologyConfig;
        Ontology ontology;
        ConceptFilterConfig filterConfig;
        ConceptFilter filter;
        DocumentParser parser;
        String wordDelimitor, nameMode;
        int lemmatiserID, filterID, ontologyID, taggerID, parserID;
        boolean subtermOption, filterOption, abbr, semanticCheck, attributeCheck;
        boolean coordinatingCheck, compoundTermPrediction, coordinatingTermPrediction;

        parserID=curNode.getInt("documentparser",0);
        lemmatiserID=curNode.getInt("lemmatiser",0);
        filterID=curNode.getInt("conceptfilter",0);
        ontologyID=curNode.getInt("ontology",0);
        taggerID=curNode.getInt("tagger",0);
        filterOption=curNode.getBoolean("filteroption",true);
        subtermOption=curNode.getBoolean("subconceptoption",false);
        wordDelimitor=getWordDelimitor(curNode.getString("notworddelimitor",""));
        nameMode=curNode.getString("conceptnamemode","asis");
        abbr=curNode.getBoolean("abbreviation",true);
        semanticCheck=curNode.getBoolean("semanticcheck",true);
        attributeCheck=curNode.getBoolean("attributecheck",false);
        coordinatingCheck=curNode.getBoolean("coordinatingcheck",false);
        compoundTermPrediction=curNode.getBoolean("compoundtermprediction",false);
        coordinatingTermPrediction=curNode.getBoolean("coordinatingtermprediction",false);

        if(parserID>0)
            parser=(new DocumentParserConfig()).getDocumentParser(curNode,parserID);
        else
            parser=new EngDocumentParser(wordDelimitor);
        lemmaConfig=new LemmatiserConfig();
        lemmatiser=lemmaConfig.getLemmatiser(curNode,lemmatiserID);
        ontologyConfig=new OntologyConfig();
        ontology=ontologyConfig.getOntology(curNode,ontologyID);
        taggerConfig=new TaggerConfig();
        tagger=taggerConfig.getTagger(curNode,taggerID);
        filterConfig=new ConceptFilterConfig();
        filter=filterConfig.getConceptFilter(curNode,filterID);
        extractor=new BasicTermExtractor(ontology,lemmatiser,tagger);
        extractor.setDocumentParser(parser);
        if(nameMode.equalsIgnoreCase("norm"))
            Term.setNameMode(Term.NAME_NORM);
        else if(nameMode.equalsIgnoreCase("lemma"))
            Term.setNameMode(Term.NAME_LEMMA);
        else
            Term.setNameMode(Term.NAME_ASIS);
        extractor.setConceptFilter(filter);
        extractor.setSubConceptOption(subtermOption);
        extractor.setFilteringOption(filterOption);
        extractor.setAbbreviationOption(abbr);
        extractor.setSemanticCheckOption(semanticCheck);
        extractor.setAttributeCheckOption(attributeCheck);
        extractor.setCoordinatingCheckOption(coordinatingCheck);
        extractor.setCompoundTermPredictOption(compoundTermPrediction);
        extractor.setCoordinatingTermPredictOption(coordinatingTermPrediction);
        return extractor;
    }

    private String getWordDelimitor(String notWordDelimitor){
        String delimitors;
        StringBuffer sb;
        int i;

        sb=new StringBuffer();
        delimitors=EngDocumentParser.defWordDelimitor;
        if(notWordDelimitor==null && notWordDelimitor.length()==0)
            return delimitors;
        for(i=0;i<delimitors.length();i++){
            if(notWordDelimitor.indexOf(delimitors.charAt(i))<0)
                sb.append(delimitors.charAt(i));
        }
        return sb.toString();
    }
}