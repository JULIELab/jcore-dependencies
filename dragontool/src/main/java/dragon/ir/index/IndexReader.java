package dragon.ir.index;

import dragon.onlinedb.Article;
/**
 * <p>Interface of index reader</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IndexReader {
    /**
     * It is required to call this method before reading out any indexing information.
     */
    public void initialize();

    /**
     * This method releases all occupied resources.
     */
    public void close();

    /**
     * @return true if the indexing contains relationship information
     */
    public boolean isRelationSupported();

    /**
     * @return IRCollection object which contains the statistics of the indexed collection
     */
    public IRCollection getCollection();

    /**
     * @param index the index of the document
     * @return the IRDoc object
     */
    public IRDoc getDoc(int index);

    /**
     * @param key the unique etnry number of the document
     * @return the IRDoc object
     */
    public IRDoc getDoc(String key);

    /**
     * @param index the index of the document
     * @return the unique entry number of the index-th document
     */
    public String getDocKey(int index);

    /**
     * @param key the unique entry number of the document
     * @return the raw content of the indexed document
     */
    public Article getOriginalDoc(String key);

    /**
     * @param index the index of the document
     * @return the raw content of the indexed document
     */
    public Article getOriginalDoc(int index);

    /**
     * @param docIndex the index of the document
     * @return a list of unique terms in the given document
     */
    public IRTerm[] getTermList(int docIndex);

    /**
     * @param docIndex the index of the document
     * @return an interger array each elment of which is the index of unique terms in the given document
     */
    public int[] getTermIndexList(int docIndex);

    /**
     * To know what term the frequency corresponds to, call the method getTermIndexList.
     * @param docIndex the index of the document
     * @return an interger array each elment of which is the frequency of unique terms in the given document
     */
    public int[] getTermFrequencyList(int docIndex);

    /**
     * @param key the name of the term
     * @return the IRTerm object
     */
    public IRTerm getIRTerm(String key);

    /**
     * @param index the index of the term
     * @return the IRTerm object
     */
    public IRTerm getIRTerm(int index);

    /**
     * If the given document does not contain the given term, this method returns null.
     * @param termIndex the index of the term
     * @param docIndex the index of the document
     * @return the IRTerm object
     */
    public IRTerm getIRTerm(int termIndex, int docIndex);

    /**
     * @param index: the index of the wanted term
     * @return the name of the given term
     */
    public String getTermKey(int index);

    /**
     * @param termIndex the index of the term
     * @return a list of IRDoc objects which contain the specified term
     */
    public IRDoc[] getTermDocList(int termIndex);

    /**
     * @param termIndex the index of the term
     * @return a list of frequencies the given term occurs in the documents containing this term
     */
    public int[] getTermDocFrequencyList(int termIndex);

    /**
     * @param termIndex the index of the term
     * @return a list of indices of documents containing this term
     */
    public int[] getTermDocIndexList(int termIndex);

    /**
     * @param docIndex the index of the document
     * @return a list of unique relations in the given document
     */
    public IRRelation[] getRelationList(int docIndex);

    /** To know what relation the frequency corresponds to, call the method getRelationIndexList.
     * @param docIndex the index of the document
     * @return an interger array each elment of which is the frequency of unique relations in the given document
     */
    public int[] getRelationFrequencyList(int docIndex);

    /**
     * @param docIndex the index of the document
     * @return an interger array each elment of which is the index of unique relations in the given document
     */
    public int[] getRelationIndexList(int docIndex);

    /**
     * @param index the index of the relation
     * @return the IRRelation object
     */
    public IRRelation getIRRelation(int index);

    /**
     * If the given document does not contain the given relation, this method returns null.
     * @param relationIndex the index of the relation
     * @param docIndex the index of the document
     * @return the IRRelation object
     */
    public IRRelation getIRRelation(int relationIndex, int docIndex);

    /**
     * @param relationIndex the index of the relation
     * @return a list of IRDoc objects which contain the specified relation
     */
    public IRDoc[] getRelationDocList(int relationIndex);

    /**
     * @param relationIndex the index of the relation
     * @return a list of frequencies the given relation occurs in the documents containing this relation
     */
    public int[] getRelationDocFrequencyList(int relationIndex);

    /**
     * @param relationIndex the index of the relation
     * @return a list of indices of documents containing this relation
     */
    public int[] getRelationDocIndexList(int relationIndex);
}