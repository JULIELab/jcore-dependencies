package dragon.nlp.extract;

import dragon.nlp.tool.*;
import dragon.nlp.ontology.*;

/**
 * <p>Abstract class for phrase extraction </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractPhraseExtractor extends AbstractConceptExtractor implements PhraseExtractor{
    protected Lemmatiser lemmatiser;
    protected Tagger tagger;
    protected Vocabulary vocabulary;
    protected boolean useNoun, useAdj, useVerb;

    public AbstractPhraseExtractor(Vocabulary vocabulary, Tagger tagger, Lemmatiser lemmatiser) {
        this.lemmatiser=lemmatiser;
        this.tagger =tagger;
        this.vocabulary =vocabulary;
        this.useNoun=false;
        this.useAdj=false;
        this.useVerb=false;
    }

    public void setSingleNounOption(boolean option){
        this.useNoun =option;
    }

    public boolean getSingleNounOption(){
        return useNoun;
    }

    public void setSingleVerbOption(boolean option){
        this.useVerb =option;
    }

    public boolean getSingleVerbOption(){
        return useVerb;
    }

    public void setSingleAdjectiveOption(boolean option){
        this.useAdj =option;
    }

    public boolean getSingleAdjectiveOption(){
        return useAdj;
    }

    public boolean supportConceptName(){
        return true;
    }

    public boolean supportConceptEntry(){
        return false;
    }

    public Lemmatiser getLemmatiser(){
        return lemmatiser;
    }

    public void setLemmatiser(Lemmatiser lemmatiser) {
        this.lemmatiser = lemmatiser;
    }

    public Tagger getPOSTagger(){
        return tagger;
    }

    public Vocabulary getVocabulary(){
        return vocabulary;
    }

    public void initDocExtraction(){
    }
}