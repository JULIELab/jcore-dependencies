package dragon.ir.index;

/**
 * <p>Interface of index writer</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IndexWriter {
    /**
     * It is required to call this method before calling the write method
     */
    public void initialize();
    public void close();

    /**
     * For disk-based indexing, this method delete all files under the indexing folder.
     */
    public void clean();

    /**
     * All fields (doc key, doc index, term count, term number, relation count, and relation number) of the IRDoc object should be set.
     * Fields of IRTerm including term index and term frequency should be set. Fields of IRRelation including relation index, first term,
     * second term, and relation frequency should be set. No duplicated IRTerm or IRRelation is allowed.
     * @param curDoc the current document
     * @param arrTerms terms in the current document
     * @param arrRelations relations in the current document
     * @return true if written successfully
     */
    public boolean write(IRDoc curDoc, IRTerm[] arrTerms, IRRelation[] arrRelations);

    /**
     * All fields (doc key, doc index, term count, term number, relation count, and relation number) of the IRDoc object should be set.
     * Fields of IRTerm including term index and term frequency should be set. No duplicated IRTerm is allowed.
     * @param curDoc the current document
     * @param arrTerms terms in the current document
     * @return true if written successfully
     */
    public boolean write(IRDoc curDoc, IRTerm[] arrTerms);

    /**
     * @return the number of documents indexed.
     */
    public int size();

    /**
     * For disk-based indexing, this method dump all information in memory to the files on disk.
     */
    public void flush();
}