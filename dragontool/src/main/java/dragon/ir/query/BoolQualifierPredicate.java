package dragon.ir.query;

/**
 * <p>Bool qualifier predicate</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BoolQualifierPredicate extends AbstractPredicate{
    public BoolQualifierPredicate(String[] predicate) {
        parse(predicate);
    }

    protected void parse(String[] predicate){
        weight=0.8;
        predicateType=PREDICATE_QUALIFIER;
        expressionType=PREDICATE_BOOL;
        constraint=new BoolExpression(predicate);
    }

    public String toSQLExpression(){
        return constraint.toSQLExpression();
    }

    public String toString(){
        return "M("+constraint.toString()+")";
    }
}