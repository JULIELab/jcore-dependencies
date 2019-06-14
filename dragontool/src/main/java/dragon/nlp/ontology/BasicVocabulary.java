package dragon.nlp.ontology;

import dragon.nlp.Sentence;
import dragon.nlp.Word;
import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.Tagger;

/**
 * <p>The class can be called directly to support vocabulary operations </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicVocabulary extends AbstractVocabulary{

    public BasicVocabulary(String termFilename) {
        super(termFilename);
    }

    public BasicVocabulary(String termFilename, Lemmatiser lemmatiser) {
        super(termFilename,lemmatiser);
    }

    public boolean isPhrase(String term){
        return list.contains(term);
    }

    public boolean isPhrase(Word start, Word end){
        return isPhrase(buildString(start,end,getLemmaOption()));
    }

    public Word findPhrase(Word start){
        Sentence sent;
        Word curWord, end;
        boolean found;
        int j, posIndex;

        sent = start.getParent();
        //set the right bounary of the possible term.
        curWord = start.next;
        end=null;
        j = 0;
        while (j < maxPhraseLength-1 && curWord != null && end == null) {
            if (isBoundaryWord(curWord))
                end = curWord.prev;
            j++;
            curWord = curWord.next;
        }

        if (curWord == null)
            curWord = sent.getLastWord();
        if (end == null){
            end = curWord;
            j=end.getPosInSentence()-start.getPosInSentence()+1;
        }
        if(j<minPhraseLength)
            return null;

        curWord = end;
        found=false;
        while (curWord!=null && curWord.getPosInSentence()>=start.getPosInSentence() && j>=minPhraseLength) {
            posIndex = curWord.getPOSIndex();
            if ( (posIndex == Tagger.POS_NOUN || posIndex == Tagger.POS_NUM && curWord.getPosInSentence() > start.getPosInSentence())) {
                if(isPhrase(start, curWord)) {
                    found=true;
                    break;
                }
            }
            curWord=curWord.prev;
            j--;
        }

        if(found)
            return curWord;
        else
            return null;
    }
}