package dragon.nlp.extract;

import dragon.nlp.ontology.Vocabulary;
import dragon.nlp.tool.Tagger;

/**
 * <p>Interface of Mutliword Phrase Extractor</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface PhraseExtractor extends ConceptExtractor {
    /**
     * Gets the part of speech tagger used for the phrase extractor
     * @return the part of speech tagger
     */
    public Tagger getPOSTagger();

    /**
     * Gets the phrase dictionary
     * @return the phrase dictionary
     */
    public Vocabulary getVocabulary();

    /**
     * Sets the option of extracting single-word nouns.
     * @param option the option of extracting single-word nouns
     */
    public void setSingleNounOption(boolean option);

    /**
     * Tests if the single-word nouns will be extracted.
     * @return true if the single-word nouns will be extracted.
     */
    public boolean getSingleNounOption();

    /**
     * Sets the option of extracting single-word verbs.
     * @param option the option of extracting single-word verbs
     */
    public void setSingleVerbOption(boolean option);

    /**
     * Tests if the single-word verbs will be extracted.
     * @return true if the single-word verbs will be extracted.
     */
    public boolean getSingleVerbOption();

    /**
     * Sets the option of extracting single-word adjectives.
     * @param option the option of extracting single-word adjectives
     */
    public void setSingleAdjectiveOption(boolean option);

    /**
     * Tests if the single-word adjectives will be extracted.
     * @return true if the single-word adjectives will be extracted.
     */
    public boolean getSingleAdjectiveOption();
}