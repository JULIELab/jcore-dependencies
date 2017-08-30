package dragon.nlp.ontology;

import dragon.nlp.*;
import dragon.nlp.tool.*;
import dragon.util.*;
import java.io.*;

/**
 * <p>The class implements all the basic functions related with vocabulary </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractVocabulary implements Vocabulary{
    protected Lemmatiser lemmatiser;
    protected boolean enable_npp_option;
    protected boolean enable_coordinate_option;
    protected boolean enable_adjterm_option;
    protected boolean enable_lemma_option;
    protected String nonboundaryPunctuations;
    protected SimpleElementList list;
    protected int maxPhraseLength, minPhraseLength;

    public AbstractVocabulary(String termFilename) {
        this.lemmatiser=null;
        enable_npp_option=false;
        enable_coordinate_option=false;
        enable_adjterm_option=false;
        enable_lemma_option=false;
        nonboundaryPunctuations="-";
        list=new SimpleElementList(termFilename,false);
        readVocabularyMeta(termFilename);
    }

    public AbstractVocabulary(String termFilename, Lemmatiser lemmatiser) {
        this.lemmatiser=lemmatiser;
        enable_npp_option=false;
        enable_coordinate_option=false;
        enable_adjterm_option=false;
        enable_lemma_option=false;
        nonboundaryPunctuations="-";
        list=new SimpleElementList(termFilename,false);
        readVocabularyMeta(termFilename);
    }

    public int getPhraseNum(){
        return list.size();
    }

    public String getPhrase(int index){
        return list.search(index);
    }

    public int maxPhraseLength(){
        return this.maxPhraseLength;
    }

    public int minPhraseLength(){
        return this.minPhraseLength;
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

    public void setAdjectivePhraseOption(boolean enabled){
        enable_adjterm_option=enabled;
    }

    public boolean getAdjectivePhraseOption(){
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
            else if("of".indexOf(curWord.getContent()) < 0)
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
            term = next.getLemma();
            while (!next.equals(end)) {
                next = next.next;
                if(isUsefulForPhrase(next))
                    term = term + " " + next.getLemma();
            }
        }
        else{
            term = next.getContent().toLowerCase();
            while (!next.equals(end)) {
                next = next.next;
                if(isUsefulForPhrase(next))
                    term = term + " " + next.getContent().toLowerCase();
            }
        }
        return term;
    }

    protected boolean isUsefulForPhrase(Word word){
        int posIndex;

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

    protected void readVocabularyMeta(String termFilename){
        BufferedReader br;
        String[] arrField;

        try{
            br=FileUtil.getTextReader(termFilename);
            arrField=br.readLine().split("\t");
            minPhraseLength=Integer.parseInt(arrField[1]);
            maxPhraseLength=Integer.parseInt(arrField[2]);
            br.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}