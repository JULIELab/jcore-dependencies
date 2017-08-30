package dragon.ir.query;

import dragon.onlinedb.Article;
/**
 * <p>Interface of query generator which converts natural language into structured query</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface QueryGenerator {
    /**
     * @param topic query topic
     * @return structured query
     */
    public IRQuery generate(Article topic);

    /**
     * @param topic topic description in natural language
     * @return structured query
     */
    public IRQuery generate(String topic);
}