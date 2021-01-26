package dragon.nlp.tool.lemmatiser;

/**
 * <p>Interface of lemmatising operations</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Operation {
    /**
     * Executes the operation to get the base form of the given word
     * @param derivation the word for lemmatising
     * @return the lemmatised word
     */
    String execute(String derivation);

    /**
     * Gets the option of checking the existence of the lemmatised word in the dictionary. If this option is enabled and the lemmatised
     * word does not exist, the execute method will return the input word.
     * @return true if it is required to check the existence of the lemmatised word in the dictionary.
     */
    boolean getIndexLookupOption();
}