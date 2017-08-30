package dragon.ir.index;

import dragon.onlinedb.Article;
/**
 * <p>Interface of document indexing agent</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Indexer {
    /**
     * It is required to call this method before calling index method to index articles.
     */
    public void initialize();

    /**
     * This method releases occupied resources.
     */
    public void close();

    /**
     * An article has many sections such as title, abstract and body. The selection of sections for indexing is subject to the implemented indexers.
     * @param article the article for indexing
     * @return true if indexed successfully
     */
    public boolean index(Article article);

    /**
     * @param docKey the unique document entry number
     * @return true if this document has been indexed
     */
    public boolean indexed(String docKey);
    public void setLog(String logFile);
}