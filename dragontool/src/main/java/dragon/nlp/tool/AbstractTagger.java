package dragon.nlp.tool;

import dragon.nlp.Word;
/**
 * <p>The class provides basic methods for tagging a text</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AbstractTagger {
    private int conjNum;
    private String[] arrConj;

    public AbstractTagger() {
        conjNum = 9;
        arrConj = new String[conjNum];
        arrConj[0] = "although";
        arrConj[1] = "because";
        arrConj[2] = "but";
        arrConj[3] = "if";
        arrConj[4] = "that";
        arrConj[5] = "though";
        arrConj[6] = "when";
        arrConj[7] = "whether";
        arrConj[8] = "while";
    }

    public boolean isConjunction(String content) {
        int low, middle, high;
        int result;

        low = 0;
        high = conjNum - 1;
        while (low <= high) {
            middle = (low + high) / 2;
            result = arrConj[middle].compareToIgnoreCase(content);
            if (result == 0) {
                return true;
            }
            else if (result > 0) {
                high = middle - 1;
            }
            else {
                low = middle + 1;
            }
        }
        return false;
    }

    protected int getPOSIndex(String pos, Word word) {
        int posIndex;

        if (word.isPunctuation()) {
            pos = word.getContent();
            posIndex = 0;
        }
        else if (pos.startsWith("N")) {
            posIndex = HeppleTagger.POS_NOUN;
        }
        else if (pos.startsWith("VB")) {
            posIndex = Tagger.POS_VERB;
        }
        else if (pos.startsWith("JJ")) {
            posIndex = Tagger.POS_ADJECTIVE;
        }
        else if (pos.startsWith("RB")) {
            posIndex = Tagger.POS_ADVERB;
        }
        else if (pos.startsWith("CC")) {
            posIndex = Tagger.POS_CC;
        }
        else if (pos.startsWith("DT")) {
            posIndex = Tagger.POS_DT;
        }
        else if (pos.startsWith("PRP")) {
            posIndex = Tagger.POS_PRONOUN;
        }
        else if (pos.startsWith("IN")) {
            posIndex = Tagger.POS_IN;
        }
        else if (pos.startsWith("CD")) {
            posIndex = Tagger.POS_NUM;
        }
        else if (pos.startsWith("W")) {
            posIndex = Tagger.POS_CC;
        }
        else {
            posIndex = 0;
        }
        return posIndex;
    }
}