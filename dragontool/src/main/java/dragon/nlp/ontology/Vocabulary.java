package dragon.nlp.ontology;

import dragon.nlp.Word;
/**
 * <p>Inerface of Phrase Dictionary</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Vocabulary {
    /**
     * Tests if the term is a phrase
     * @param term the term for testing
     * @return true if the term is a phrase defined in the vocabulary
     */
    public boolean isPhrase(String term);

    /**
     * Tests if the term specified by the starting word and the ending word is a prhase.
     * @param start the starting word of the phrase
     * @param end the ending word of the phrase
     * @return true if the term is a phrase defined in the vocabulary
     */
    public boolean isPhrase(Word start, Word end);

    /**
     * Tests if the specified word could be the starting a word of a phrase.
     * @param cur the current word for testing
     * @return true if the current word could be the starting a word of a phrase
     */
    public boolean isStartingWord(Word cur);

    /**
     * Finds out the phrase beging with the given word.
     * @param start the starting word
     * @return the ending word if a phrase is matched; otherwise null.
     */
    public Word findPhrase(Word start);

    /**
     * Gets the number of phrases in the vocabulary.
     * @return the number of phrases in the vocabulary.
     */
    public int getPhraseNum();

    /**
     * Gets the index-th phrase in the vocabulary.
     * @param index the position of the phrase in the vocabulary
     * @return the phrase in the given position of the vocabulary.
     */
    public String getPhrase(int index);

    /**
     * Gets the maximum number of words a phrase can contain.
     * @return the maximum number of words a phrase can contain.
     */
    public int maxPhraseLength();

    /**
     * Gets the minimum number of words a phrase can contain.
     * @return the minimum number of words a phrase can contain.
     */
    public int minPhraseLength();

    /**
     * Sets the option whether adjective phrase is allowed.
     * @param enabled whether adjective phrase is allowed.
     */
    public void setAdjectivePhraseOption(boolean enabled);

    /**
     * Gets the option whether adjective phrase is allowed.
     * @return true if adjective phrase is allowed.
     */
    public boolean getAdjectivePhraseOption();

    /**
     * Sets the option whether NPP phrase is allowed. For example, "bank of america" is a NPP phrase
     * @param enabled the option whether NPP phrase is allowed.
     */
    public void setNPPOption(boolean enabled);

    /**
     * Gets the option whether NPP phrase is allowed.
     * @return true if NPP phrase is allowed.
     */
    public boolean getNPPOption();

    /**
     * Sets the option whether a phrase can contain a conjunction. For example, the phrase "the cancer of neck and hand" contains a conjunction.
     * @param enabled the option whether a phrase can contain a conjunction
     */
    public void setCoordinateOption(boolean enabled);

    /**
     * Gets the option whether a phrase can contain a conjunction.
     * @return true if a phrase can contain a conjunction.
     */
    public boolean getCoordinateOption();

    /**
     * Sets the option of using the base form of the word when matching a phrase.
     * @param enabled the option of using the base form of the word when matching a phrase.
     */
    public void setLemmaOption(boolean enabled);

    /**
     * Gets the option of using the base form of the word when matching a phrase.
     * @return true if the base form of words is used when matching a phrase.
     */
    public boolean getLemmaOption();
}