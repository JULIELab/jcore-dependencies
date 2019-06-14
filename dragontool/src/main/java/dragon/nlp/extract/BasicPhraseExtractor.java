package dragon.nlp.extract;

import dragon.nlp.Phrase;
import dragon.nlp.Sentence;
import dragon.nlp.Word;
import dragon.nlp.ontology.Vocabulary;
import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.Tagger;

import java.util.ArrayList;

/**
 * <p>Phrase extraction</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicPhraseExtractor extends AbstractPhraseExtractor{
    protected boolean overlappedPhrase; //two phrases could be overlapped.

     public BasicPhraseExtractor(Vocabulary vocabulary, Lemmatiser lemmatiser, Tagger tagger){
         this(vocabulary,lemmatiser,tagger,false);
     }

    public BasicPhraseExtractor(Vocabulary vocabulary, Lemmatiser lemmatiser, Tagger tagger, boolean overlappedPhrase) {
        super(vocabulary,tagger, lemmatiser);
        this.overlappedPhrase=overlappedPhrase;
    }

    public ArrayList extractFromSentence(Sentence sent){
        ArrayList phraseList;
        Word cur, start, end; //start and end are the maximum boundary of the term
        Phrase curPhrase;
        int posIndex, lastPhraseEndPos;

        //tagging and lemmatising
        cur = sent.getFirstWord();
        if(cur!=null && cur.getPOSIndex()<0) tagger.tag(sent);
        while(cur!=null){
            posIndex=cur.getPOSIndex();
            if(posIndex==Tagger.POS_ADJECTIVE && useAdj || posIndex==Tagger.POS_NOUN || posIndex==Tagger.POS_VERB && useVerb)
                cur.setLemma(lemmatiser.lemmatize(cur.getContent(),posIndex));
            else
                cur.setLemma(cur.getContent().toLowerCase());
            cur=cur.next;
        }

        lastPhraseEndPos=-1;
        cur=sent.getFirstWord();
        phraseList = new ArrayList(30);
        while (cur != null) {
            //search the word which could be the opening of a term.
            if (!vocabulary.isStartingWord(cur)) {
                posIndex=cur.getPOSIndex();
                if(cur.getPosInSentence()>lastPhraseEndPos && (posIndex==Tagger.POS_NOUN && useNoun ||posIndex==Tagger.POS_ADJECTIVE && useAdj ||posIndex==Tagger.POS_VERB && useVerb))
                    addPhrase(cur,cur,false,false,phraseList);
                cur = cur.next;
                continue;
            }

            end = vocabulary.findPhrase(cur);
            if (end == null || (curPhrase=addPhrase(cur,end,true, false,phraseList))==null) {
                posIndex=cur.getPOSIndex();
                if(cur.getPosInSentence()>lastPhraseEndPos && (posIndex==Tagger.POS_NOUN && useNoun ||posIndex==Tagger.POS_ADJECTIVE && useAdj ||posIndex==Tagger.POS_VERB && useVerb))
                    addPhrase(cur,cur,false,false,phraseList);
                cur = cur.next;
            }
            else {
                start=cur;
                if(curPhrase.getWordNum()>=2 && getSubConceptOption()){
                    while(cur!=null && cur.getPosInSentence()<=end.getPosInSentence()){
                        posIndex=cur.getPOSIndex();
                        if(cur.getPosInSentence()>lastPhraseEndPos && (posIndex==Tagger.POS_NOUN && useNoun || posIndex==Tagger.POS_ADJECTIVE && useAdj))
                            addPhrase(cur, cur,false, true, phraseList);
                        cur=cur.next;
                    }
                }
                lastPhraseEndPos=end.getPosInSentence();
                if(overlappedPhrase)
                    cur=start.next;
                else
                    cur = end.next;
            }
        }
        return phraseList;
    }

    protected Phrase addPhrase(Word start, Word end, boolean forRelation, boolean subphrase, ArrayList phraseList){
        Phrase phrase;

        phrase=new Phrase(start,end);
        phrase.setSubConcept(subphrase);
        if(conceptFilter_enabled && !cf.keep(phrase.getName()))
            return null;
        if(forRelation)
            start.setAssociatedConcept(phrase);
        phrase.setFrequency(1);
        phraseList.add(phrase);
        return phrase;
    }
}
