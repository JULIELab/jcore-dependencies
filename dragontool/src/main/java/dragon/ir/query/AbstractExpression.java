package dragon.ir.query;

/**
 * <p>Abstract class for query expression </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractExpression implements Expression{
    protected Operator optr;
    protected int expressionType;

    public AbstractExpression() {
        optr=null;
    }

    public Operator getOperator(){
        return optr;
    }

    public boolean isBoolExpression(){
        return expressionType==EXPRESSION_BOOL;
    }

    public boolean isSimpleExpression(){
        return expressionType==EXPRESSION_SIMPLE;
    }
}