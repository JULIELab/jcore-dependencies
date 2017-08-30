package dragon.ir.search;

import dragon.ir.index.*;
import dragon.ir.query.IRQuery;
import dragon.ir.search.smooth.Smoother;
import java.util.ArrayList;

/**
 * <p>Interface of Text Searcher</p>
 * <p>The toolkit provides a well-defined framework for text retrieval. The first step is to generate a query according to the topic
 * descriptions (such as TREC Topic files). Please refer to the package of dragon.ir.query for query generation. The second step is
 * to create a searcher. Since there are so many different retrieval models, the toolkit creates an interface called Smoother to
 * hide the implementation details of different models. Thus, the routine for searching is the same for different models. One can
 * simply call a full rank searcher or a partial rank searcher. </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Searcher {
    /**
     * @param query the structured query
     * @return the number of matched documents
     */
    public int search(IRQuery query);

    /**
     * Before calling this method, one should call the search function.
     * @param ranking  the position of the ranked documents. The position starts from zero.
     * @return the retrieved document in the given position
     */
    public IRDoc getIRDoc(int ranking);

    /**
     * @return all retrieved document in ranked order
     */
    public ArrayList getRankedDocumentList();

    /**
     * @return the number of retrieved documents
     */
    public int getRetrievedDocNum();

    /**
     * @return the index reader the current searcher is working on
     */
    public IndexReader getIndexReader();

    /**
     * @return the smoother used for this searcher.
     */
    public Smoother getSmoother();
    public IRQuery getQuery();

    /**
     * If the weighting option is set to false, the searcher ignores the weight of searching terms.
     * @param option the weighting option
     */
    public void setQueryWeightingOption(boolean option);
    public boolean getQueryWeightingOption();
}