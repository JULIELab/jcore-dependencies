package dragon.ir.search;

import dragon.ir.query.IRQuery;
import dragon.ir.search.Searcher;
import dragon.ir.search.expand.QueryExpansion;

/**
 * <p>Query expansion searcher</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class QueryExpansionSearcher extends AbstractSearcher{
    private Searcher searcher;
    private QueryExpansion qe;;

    public QueryExpansionSearcher(Searcher searcher, QueryExpansion qe) {
        super(searcher.getIndexReader(),searcher.getSmoother());
        this.searcher =searcher;
        this.qe =qe;
    }

    public int search(IRQuery query){
        this.query=qe.expand(query);
        searcher.search(this.query);
        hitlist=searcher.getRankedDocumentList();
        return hitlist.size();
    }

    public QueryExpansion getQueryExpansion(){
        return qe;
    }
}
