package dragon.ir.query;

/**
 * <p>Bool term predicate </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BoolTermPredicate extends AbstractPredicate{
    public BoolTermPredicate(String[] predicate) {
        parse(predicate);
    }

    protected void parse(String[] predicate){
        weight=0.3;
        predicateType=Predicate.PREDICATE_TERM;
        expressionType=PREDICATE_BOOL;
        constraint=new BoolExpression(predicate);
    }

    public String toSQLExpression(){
        return constraint.toSQLExpression();
    }

    public String toString(){
        return "T("+constraint.toString()+")";
    }

}