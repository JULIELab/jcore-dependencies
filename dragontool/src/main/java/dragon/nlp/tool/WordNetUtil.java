package dragon.nlp.tool;

/**
 * <p>Interface of WordNet (incomplete version) </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface WordNetUtil {
    static final int POS_NOUN=1;
    static final int POS_VERB=2;
    static final int POS_ADJECTIVE=3;
    static final int POS_ADVERB=4;
    static final int FIRSTPOS=1;
    static final int LASTPOS=4;

    String lemmatize(String word);
    String lemmatize(String word, int POS);
}