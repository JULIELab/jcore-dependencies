package dragon.ir.search.feedback;

import dragon.ir.query.IRQuery;
import dragon.ir.search.Searcher;

/**
 * <p>Interface of pseudo-relevance feedback method</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Feedback {
    /**
     * @param oldQuery the iniital query
     * @return the updated query
     */
    public IRQuery updateQueryModel(IRQuery oldQuery);
    public int getFeedbackDocNum();

    /**
     * @param docNum the number of top documents for feedback
     */
    public void setFeedbackDocNum(int docNum);

    /**
     * @return the searcher for initial search
     */
    public Searcher getSearcher();
    public void setSearcher(Searcher searcher);
}
