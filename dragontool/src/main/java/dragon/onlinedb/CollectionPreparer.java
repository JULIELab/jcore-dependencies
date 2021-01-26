package dragon.onlinedb;

/**
 * <p>Interface of Collection Preparer</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface CollectionPreparer {
    /**
     * Adds a set of articles whose keys are listed in the given file.
     * @param articleKeyFile the text file containing all keys of wanted articles
     * @return true if added successfully.
     */
    public boolean addListedArticles(String articleKeyFile);

    /**
     * Adds all articles represented by the given query.
     * @param query the query
     * @return true if added successfully.
     */
    public boolean addArticles(ArticleQuery query);

    /**
     * Adds all articles represented by all queries listed in the given file.
     * @param queryFile the text file contaning all queries.
     * @return true if added successfully.
     */
    public boolean addArticles(String queryFile);

    /**
     * Adds all articles represented by all queries in the given array.
     * @param queries an array of query terms
     * @return true if added successfully.
     */
    public boolean addArticles(String[] queries);
}