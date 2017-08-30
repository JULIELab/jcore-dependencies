package dragon.ir.search;

import dragon.ir.query.IRQuery;
import dragon.ir.search.Searcher;
import dragon.ir.search.feedback.Feedback;

/**
 * <p>Feedback searcher </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FeedbackSearcher extends AbstractSearcher{
    private Searcher searcher;
    private Feedback feedback;

    public FeedbackSearcher(Searcher searcher, Feedback feedback) {
        super(searcher.getIndexReader(),searcher.getSmoother());
        this.searcher =searcher;
        this.feedback =feedback;
    }

    public int search(IRQuery query){
        this.query=feedback.updateQueryModel(query);
        searcher.search(this.query);
        hitlist=searcher.getRankedDocumentList();
        return hitlist.size();
    }

    public Feedback getFeedback(){
        return feedback;
    }
}