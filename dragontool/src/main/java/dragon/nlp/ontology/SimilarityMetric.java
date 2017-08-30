package dragon.nlp.ontology;

import dragon.nlp.Term;

/**
 * <p>Interface of Similarity Metrics </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface SimilarityMetric {
    /**
     * Computes the similarity between two onotlogical terms.
     * @param a the first term
     * @param b the second term
     * @return the similarity between two onotlogical terms.
     */
    public double getSimilarity(Term a, Term b);
}