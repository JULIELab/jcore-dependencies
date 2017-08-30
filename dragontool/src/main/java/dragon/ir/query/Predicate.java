package dragon.ir.query;

/**
 * <p>Interface of predicate which is the least unit of IR query</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Predicate extends IRQuery{
    static final int PREDICATE_TERM=1;
    static final int PREDICATE_RELATION=2;
    static final int PREDICATE_QUALIFIER=3;
    static final int PREDICATE_SIMPLE=1;
    static final int PREDICATE_BOOL=2;

    /**
     * @return a double socre indicating how specific the query predicate is.
     */
    public double getSelectivity();
    public double getWeight();
    public void setWeight(double weight);
    public boolean isSimplePredicate();
    public boolean isBoolPredicate();
    public boolean isTermPredicate();
    public boolean isRelationPredicate();
    public boolean isQualifierPredicate();

    /**
     * @return the underlying expression of the current predicate
     */
    public Expression getConstraint();
    public String toSQLExpression();
    public String toString();
}