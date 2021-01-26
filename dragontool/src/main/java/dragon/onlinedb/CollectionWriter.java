package dragon.onlinedb;

/**
 * <p>Interface of Collection Writer</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface CollectionWriter {
    /**
     * Adds an article to the collection
     * @param article an article
     * @return true if the article was added to the collection successfully.
     */
    public boolean add(Article article);

    /**
     * Gets the article parser of the collection writer
     * @return the article parser used for this collection writer
     */
    public ArticleParser getArticleParser();

    /**
     * Sets the article parser for the collection writer
     * @param parser the article parser
     */
    public void setArticleParser(ArticleParser parser);

    /**
     * Closes the collection writer and releases all resources.
     */
    public void close();
}