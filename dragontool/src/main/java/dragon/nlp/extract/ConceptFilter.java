package dragon.nlp.extract;

import dragon.nlp.Concept;
/**
 * <p>Interface of concept filter </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface ConceptFilter {
    /**
     * Tests if the specified concept object should be kept
     * @param concept the concept
     * @return true if the concept object should be kept
     */
    public boolean keep(Concept concept);

    /**
     * Tests if the specified concept should be kept
     * @param concept the concept name
     * @return true if the concept should be kept
     */
    public boolean keep(String concept);
}