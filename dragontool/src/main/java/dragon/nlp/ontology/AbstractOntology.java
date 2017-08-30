package dragon.nlp.ontology;

import dragon.nlp.*;
import dragon.nlp.tool.*;
/**
 * <p>The class implements all the basic functions related with ontology </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractOntology implements Ontology{
    private Lemmatiser lemmatiser;
    private boolean enable_tsd_option;
    private boolean enable_npp_option;
    private boolean enable_coordinate_option;
    private boolean enable_adjterm_option;
    private boolean enable_lemma_option;
    private String nonboundaryPunctuations;

    public AbstractOntology(Lemmatiser lemmatiser) {
        this.lemmatiser=lemmatiser;
        enable_tsd_option=false;
        enable_npp_option=false;
        enable_coordinate_option=false;
        enable_adjterm_option=false;
        enable_lemma_option=true;
        nonboundaryPunctuations=".-\'";
    }

    public SemanticNet getSemanticNet(){
        return null;
    }

    public SimilarityMetric getSimilarityMetric(){
        return null;
    }

    public void setNonBoundaryPunctuation(String punctuations){
        nonboundaryPunctuations=punctuations;
    }

    public String getNonBoundaryPunctuation(){
        return nonboundaryPunctuations;
    }

    public void setLemmaOption(boolean enabled){
        enable_lemma_option=enabled;
    }

    public boolean getLemmaOption(){
        return enable_lemma_option;
    }

    public void setSenseDisambiguationOption(boolean enabled){
        enable_tsd_option=enabled;
    }

    public boolean getSenseDisambiguationOption(){
        return enable_tsd_option;
    }

    public void setAdjectiveTermOption(boolean enabled){
        enable_adjterm_option=enabled;
    }

    public boolean getAdjectiveTermOption(){
        return enable_adjterm_option;
    }

    public void setNPPOption(boolean enabled){
        enable_npp_option=enabled;
    }

    public boolean getNPPOption(){
        return enable_npp_option;
    }

    public void setCoordinateOption(boolean enabled){
        enable_coordinate_option=enabled;
    }

    public boolean getCoordinateOption(){
        return enable_coordinate_option;
    }

    public boolean isStartingWord(Word cur) {
        int posIndex;

        posIndex = cur.getPOSIndex();
        if (posIndex == Tagger.POS_NOUN || posIndex == Tagger.POS_ADJECTIVE) {
            return true;
        }

        if (posIndex == Tagger.POS_NUM && cur.next != null && cur.next.getContent().equals("-")) {
            return true;
        }
        return false;
    }

    protected boolean isBoundaryWord(Word curWord){
        int posIndex;

        if (curWord.isPunctuation() && nonboundaryPunctuations.indexOf(curWord.getContent()) < 0)
            return true;

        if(curWord.prev!=null && curWord.prev.getType()==Word.TYPE_PUNC)
            return false;

        posIndex = curWord.getPOSIndex();
        if (posIndex == Tagger.POS_VERB)
            return true;

        if(posIndex==Tagger.POS_IN){
            if(!enable_npp_option)
                return true;
            else if("in for of".indexOf(curWord.getContent()) < 0)
                return true;
        }

        if(posIndex==Tagger.POS_CC){
            if(!enable_coordinate_option)
                return true;
            else if("and or".indexOf(curWord.getContent())<0)
                return true;
        }

        return false;
    }

    protected String getLemma(Word word){
        String lemma;

        lemma = word.getLemma();
        if (lemma == null) {
            if (word.getPOSIndex() >= 0)
                lemma = lemmatiser.lemmatize(word.getContent(), word.getPOSIndex());
            else
                lemma = lemmatiser.lemmatize(word.getContent());
            word.setLemma(lemma);
        }
        return lemma;
    }

    protected String buildString(Word start, Word end, boolean useLemma){
        Word next;
        String term;

        next = start;
        if(useLemma){
            term = getLemma(next);
            while (!next.equals(end)) {
                next = next.next;
                if(isUsefulForTerm(next))
                    term = term + " " +getLemma(next);
            }
        }
        else{
            term = next.getContent().toLowerCase();
            while (!next.equals(end)) {
                next = next.next;
                if(isUsefulForTerm(next))
                    term = term + " " + next.getContent().toLowerCase();
            }
        }
        return term;
    }

    protected  boolean isUsefulForTerm(Word word){
        int posIndex;

        if(word.prev!=null && word.prev.isPunctuation()){
            if(word.prev.getContent().charAt(0)=='\'')
            {
                word.setIgnore(true);
                return false;
            }
            else{
                if(word.isPunctuation()){
                    word.setIgnore(true);
                    return false;
                }
                else{
                    word.setIgnore(false);
                    return true;
                }
            }
        }

        posIndex=word.getPOSIndex();
        if(posIndex==Tagger.POS_ADJECTIVE || posIndex==Tagger.POS_NOUN || posIndex==Tagger.POS_NUM)
        {
            word.setIgnore(false);
            return true;
        }
        else{
            word.setIgnore(true);
            return false;
        }
    }

}