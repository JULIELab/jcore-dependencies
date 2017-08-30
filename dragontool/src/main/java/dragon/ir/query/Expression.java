package dragon.ir.query;

/**
 * <p>Interface of query expressions</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Expression {
    static final int EXPRESSION_BOOL=1;
    static final int EXPRESSION_SIMPLE=2;

    /**
     * @return a string for the expression
     */
    String toString();

    /**
     * @return a string for SQL-formatted expression
     */
    String toSQLExpression();

    Operator getOperator();

    /**
     * This method returns zero for simple expressions. It returns the number of conditions for bool expression.
     * @return the number of sub-expressions
     */
    int getChildNum();

    /**
     * This method returns null for simple expressions. It returns index-th conditions for bool expression.
     * @param index the index of the child expression
     * @return the specified child expression
     */
    Expression getChild(int index);
    boolean isSimpleExpression();
    boolean isBoolExpression();
}