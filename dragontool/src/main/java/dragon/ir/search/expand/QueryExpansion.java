package dragon.ir.search.expand;

import dragon.ir.query.IRQuery;
/**
 * <p>Interface of query expansion method</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface QueryExpansion {
    /**
     * @param initQuery the initial query
     * @return the expanded query
     */
    public IRQuery expand(IRQuery initQuery);
}