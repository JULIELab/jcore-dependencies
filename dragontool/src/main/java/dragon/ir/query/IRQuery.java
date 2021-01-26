package dragon.ir.query;

/**
 * <p>Interface of IR Query</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IRQuery {
    /**
     * @return the unique id of the query
     */
    public int getQueryKey();
    public void setQueryKey(int key);

    /**
     * A predicate always begins with a ltter T (term predicate), R(relation predicate), or M(modifier predicate)
     * @return true if the the current query denotes a simple predicate, otherwise false.
     */
    public boolean isPredicate();

    /**
     * If the current query denotes a simple predicate, this method returns false, otherwise true.
     * @return false if the the current query denotes a simple predicate, otherwise true.
     */
    public boolean isCompoundQuery();
    public boolean isRelSimpleQuery();
    public boolean isRelBoolQuery();
    public IRQuery getChild(int index);
    public int getChildNum();
    public double getSelectivity();
    public Operator getOperator();
    public String toString();

    /**
     * @param query the query string
     * @return a structured query
     */
    public boolean parse(String query);
}