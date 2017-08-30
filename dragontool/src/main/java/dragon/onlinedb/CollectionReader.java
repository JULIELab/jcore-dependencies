package dragon.onlinedb;

/**
 * <p>Interface of Collection Reader which read out articles from a collection one by one</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface CollectionReader {
    /**
     * Gets the article parser of the collection reader
     * @return the article parser
     */
    public ArticleParser getArticleParser();

    /**
     * Sets the article parse for the collection reader
     * @param parser the article parser
     */
    public void setArticleParser(ArticleParser parser);

    /**
     * Collection readers only support forward-only read mode. When one call this method after calling restart method or creating the collection
     * reader, it actually return the first article of the collection.
     * @return the next article
     */
    public Article getNextArticle();

    /**
     * Reads out the article according to its key
     * @param key the unique entry number of the article
     * @return an article if exists
     */
    public Article getArticleByKey(String key);

    /**
     * if the query supports this retrieval mode, one can get articles by calling getArticleByKey method.
     * @return true if the query support the article retrieval by key.
     */
    public boolean supportArticleKeyRetrieval();

    /**
     * Closes the collection reader and releases all occupied resources.
     */
    public void close();

    /**
     * The collection reader supports forward-only read mode. If one wants to scan the collection for the second time, it is required to call
     * this method. Otherwise when one getNextArticle method, it always return null.
     */
    public void restart();
    
    /**
     * The size of the collection. If the returned size is -1, it means the exact size is unknown.
     * @return the size of the collection.
     */
    public int size();
}