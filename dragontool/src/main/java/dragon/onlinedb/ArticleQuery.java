package dragon.onlinedb;

/**
 * <p>An interface for online document retrieval</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface ArticleQuery extends CollectionReader{
    /**
     * One should call this method before retrieval articles. After initialization, number of pages and number of papers are available.
     * @return a boolean value to indicate the operation is successful or failed.
     */
    public boolean initQuery();

    /**
     * Set the searching terms. How to interpret the terms specified in the string is subject to the implementations.
     * @param terms: the searching terms.
     */
    public void setSearchTerm(String terms);

    /**
     * Move to the next page and the pointer is set on the first article of the page.
     * @return false if it reached the last page already.
     */
    public boolean moveToNextPage();

    /**
     * Move to the given page and the pointer is set on the first article of the page.
     * @param pageNo the page #
     * @return true if the given page exists.
     */
    public boolean moveToPage(int pageNo);
    public int getPageNum();
    public int getPageWidth();
    /**
     * @return number of papers in the current page.
     */
    public int getCurPageWidth();
    public int getCurPageNo();

    /**
     * The pointer will be set on the next article. After call the method of initQuery, one should call this method before you call getArticle
     * to get the first article.
     * @return true if the current pointer is not on the last article.
     */
    public boolean moveToNextArticle();

    /**
     * Move the pointer to the give position of the current page.
     * @param indexInPage the position of the article in the current page.
     * @return true if the position is within the scope of the current page.
     */
    public boolean moveToArticle(int indexInPage);

    /**
     * In some cases, retrieval of an article is very expensive. Thus, one may check the key (unique document entry number) first. If the article
     * is of interest, one will retrieve this article.
     * @return the key of the article the current pointer points to.
     */
    public String getArticleKey();

    /**
     *read out the article the current pointer points to.
     * @return an article
     */
    public Article getArticle();
}