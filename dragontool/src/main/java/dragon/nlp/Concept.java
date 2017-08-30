package dragon.nlp;

/**
 * <p>Interface of Concept which could be a single word, a multiword phrase, or an ontological term</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Concept {
    public static final int TYPE_TERM=1;
    public static final int TYPE_PHRASE=2;
    public static final int TYPE_TOKEN=3;

    /**
     * Return 1 for ontological term, 2 for multiword phrase, and 3 for token
     * @return the concept type
     */
    public int getConceptType();
    public Concept copy();

    /**
     * @return the descriptive name of the concept
     */
    public String getName();

    /**
     * @return the unique entry id, usually for ontological terms.
     */
    public String getEntryID();

    /**
     * @return the semantic type such as proteins, genes, usually for ontological terms.
     */
    public String getSemanticType();
    public boolean isSubConcept();
    public int getIndex();
    public void setIndex(int index);
    public int getFrequency();
    public void setFrequency(int frequency);
    public void addFrequency(int inc);
    public void setWeight(double weight);
    public double getWeight();
    public Object getMemo();
    public void setMemo(Object memo);
    public boolean equalTo(Concept concept);

    /**
     * Gets the first word of the concept. Phrases and ontological terms could have multiple words.
     * @return the first word of the underlying concept
     */
    public Word getStartingWord();

    /**
     * Gets the last word of the concept. Phrases and ontological terms could have multiple words.
     * @return the last word of the underlying concept
     */
    public Word getEndingWord();
}