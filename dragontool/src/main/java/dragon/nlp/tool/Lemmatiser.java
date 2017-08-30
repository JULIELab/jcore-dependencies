package dragon.nlp.tool;

/**
 * <p>Interface of lemmtaiser which gets the base form of a word</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Lemmatiser {
    static final int FIRSTPOS=1;
    static final int LASTPOS=4;

    /**
     * Gets the base form of the word.
     * @param word the word for lemmatising
     * @return the base form
     */
    String lemmatize(String word);

    /**
     * Gets the base form of the word
     * @param word the word for lemmatising
     * @param POS the part of speech of the word
     * @return the base form
     */
    String lemmatize(String word, int POS);

    /**
     * Gets the stem of the word. The result of stemming may be different from that of lemmasting. It is up to the implementations.
     * @param word the word for processing
     * @return the stem of a word
     */
    String stem(String word);
}