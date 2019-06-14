package dragon.nlp.ontology;

import dragon.nlp.Term;
import dragon.nlp.Word;

import java.util.ArrayList;
/**
 * <p>Interface of Ontology</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface Ontology {
    /**
     * Gets the semantic network of the ontology
     * @return the semantic network
     */
    public SemanticNet getSemanticNet();

    /**
     * Get the similarity metric of the ontology
     * @return the term smilarity metric
     */
    public SimilarityMetric getSimilarityMetric();

    /**
     * Gets all possible semantic types of the given concepts
     * @param cuis the set of concepts
     * @return all possible semantic types of the given concepts
     */
    public String[] getSemanticType(String[] cuis);

    /**
     * Gets all possible semantic types of the given concept
     * @param cui the concept id
     * @return all possible semantic types of the given concept.
     */
    public String[] getSemanticType(String cui);

    /**
     * Gets all possible concept ids the specified term has
     * @param term the term for testing
     * @return all possible concept ids if the specified term exists in the ontology, otherwise null
     */
    public String[] getCUI(String term);

    /**
     * Gets all possible concept ids the specified term has
     * @param starting the starting word of the term.
     * @param ending the ending word of the term.
     * @return all possible concept ids if the specified term exists in the ontology, otherwise null
     */
    public String[] getCUI(Word starting, Word ending);

    /**
     * Tests if the given term is defined in the ontology.
     * @param term the term for testing
     * @return true if the given term is defined in the ontology.
     */
    public boolean isTerm(String term);

    /**
     * Tests if the term specified by the starting word and the ending word is a term in the ontology.
     * @param starting the starting word of the term.
     * @param ending the ending word of the term.
     * @return true if the term is defined in the ontology.
     */
    public boolean isTerm(Word starting, Word ending);

    /**
     * Tests if the current word could be a starting word of a term.
     * @param cur the current word
     * @return true f the current word could be a starting word of a term.
     */
    public boolean isStartingWord(Word cur);

    /**
     * Finds the longest term starting with the given word.
     * @param starting the starting word
     * @return the term if exists, otherwise null
     */
    public Term findTerm(Word starting);

    /**
     * Finds the longest term starting with given word and ending no later than the given word.
     * @param starting the starting word
     * @param ending the ending word
     * @return the term if exists, otherwise null
     */
    public Term findTerm(Word starting, Word ending);

    /**
     * Finds out all terms beginning with the given word
     * @param starting the starting word
     * @return a list of terms
     */
    public ArrayList findAllTerms(Word starting);

    /**
     * Finds out all terms between the starting word and the ending word.
     * @param starting the starting word
     * @param ending the ending word
     * @return a list of terms
     */
    public ArrayList findAllTerms(Word starting, Word ending);

    /**
     * Sets the option disambiguating the sense of extracted terms.
     * @param enabled the option disambiguating the sense of extracted terms.
     */
    public void setSenseDisambiguationOption(boolean enabled);

    /**
     * Gets the option disambiguating the sense of extracted terms.
     * @return true if the ontology disambiguates the sense of extracted terms.
     */
    public boolean getSenseDisambiguationOption();

    /**
     * Sets the option whether adjective term is allowed.
     * @param enabled whether adjective term is allowed.
     */
    public void setAdjectiveTermOption(boolean enabled);

    /**
     * Gets the option whether adjective term is allowed.
     * @return true if adjective term is allowed.
     */
    public boolean getAdjectiveTermOption();

    /**
     * Sets the option whether NPP term is allowed. For example, "bank of america" is a NPP term
     * @param enabled the option whether NPP term is allowed.
     */
    public void setNPPOption(boolean enabled);

    /**
     * Gets the option whether NPP term is allowed.
     * @return true if NPP term is allowed.
     */
    public boolean getNPPOption();

    /**
     * Sets the option whether a term can contain a conjunction. For example, the term "the cancer of neck and hand" contains a conjunction.
     * @param enabled the option whether a term can contain a conjunction
     */
    public void setCoordinateOption(boolean enabled);

    /**
     * Gets the option whether a term can contain a conjunction.
     * @return true if a term can contain a conjunction.
     */
    public boolean getCoordinateOption();

    /**
     * Sets the option of using the base form of the word when matching a term.
     * @param enabled the option of using the base form of the word when matching a term.
     */
    public void setLemmaOption(boolean enabled);

    /**
     * Gets the option of using the base form of the word when matching a term.
     * @return true if the base form of words is used when matching a term.
     */
    public boolean getLemmaOption();
}