package dragon.nlp.tool;

import dragon.nlp.Sentence;
/**
 * <p>Interface of part speech of tagger</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Tagger {
    static final int POS_NOUN=1;
    static final int POS_VERB=2;
    static final int POS_ADJECTIVE=3;
    static final int POS_ADVERB=4;
    static final int POS_IN=5;
    static final int POS_PRONOUN=6;
    static final int POS_DT=7;
    static final int POS_CC=8;
    static final int POS_NUM=9;

    /**
     * This method will tag the part of speech of all words in the sentence. One can call the method getPOSIndex and getPOSLabel to get the
     * part of speech of a word. Different taggers may use different tag names. For this reason, it is required to translate the tag names
     * to a numerical POS label called POSIndex.
     * @param sent the sentence for tagging
     */
    void tag(Sentence sent);
}