package dragon.nlp.ontology;

/**
 * <p>Interface of Semantic Network</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface SemanticNet {
    /**
     * Gets the ontology the current semantic network belongs to
     * @return the ontology the current semantic network belongs to
     */
    public Ontology getOntology();

    /**
     * Gets the description of the given semantic type
     * @param id the entry id of the semantic type
     * @return the description of the semantic type
     */
    public String getSemanticTypeDesc(String id);

    /**
     * Gets the description of the given relation
     * @param id the entry id of the relation
     * @return the description of the relation
     */
    public String getRelationDesc(String id);

    /**
     * Gete the hierarchy description fo the given semantic type or relation
     * @param id the entry if of the semantic type or the relation
     * @return the hierarchy description
     */
    public String getHierarchy(String id);

    /**
     * Gets all possible relations between two sets of semantic types.
     * @param firstSemanticTypes the first set of semantic types
     * @param secondSemanticTypes the second set of semantic types
     * @return all possible relations between two sets of semantic types.
     */
    public String[] getRelations(String[] firstSemanticTypes, String[] secondSemanticTypes);

    /**
     * Gets all possible relations between two semantic types.
     * @param firstSemanticType the first semantic type
     * @param secondSemanticType the second semantic type
     * @return all possible relations between two semantic types.
     */
    public String[] getRelations(String firstSemanticType,String secondSemanticType);

    /**
     * Tests if any one semantic type in the first set is related to any one semantic type in the second set.
     * @param firstSemanticTypes the first set of semantic types
     * @param secondSemanticTypes the second set of semantic types
     * @return true if at least one semantic type in the first set is related to one semantic type in the second set.
     */
    public boolean isSemanticRelated(String[] firstSemanticTypes, String[] secondSemanticTypes);

    /**
     * Tests if there exists a relationship between the two given semantic types
     * @param firstSemanticType the first semantic type
     * @param secondSemanticType the second semantic type
     * @return true if there exists at least one relationship between the two given semantic types
     */
    public boolean isSemanticRelated(String firstSemanticType,String secondSemanticType);
}
