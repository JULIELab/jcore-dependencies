package dragon.ir.query;

/**
 * <p>Simple relation predicate</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleRelationPredicate extends AbstractPredicate{
    private SimpleTermPredicate first, second;

    public SimpleRelationPredicate(String[] predicate) {
        parse(predicate);
    }

    protected void parse(String[] predicate){
        SimpleExpression simple;

        predicateType=PREDICATE_RELATION;
        expressionType=PREDICATE_SIMPLE;
        constraint=new BoolExpression(predicate);

        simple=(SimpleExpression)constraint.getChild(0).getChild(0);
        first=new SimpleTermPredicate( new SimpleExpression("TERM",simple.getOperator(),simple.getTestValue()));

        simple=(SimpleExpression)constraint.getChild(1).getChild(0);
        second=new SimpleTermPredicate( new SimpleExpression("TERM",simple.getOperator(),simple.getTestValue()));
     }

    public String toSQLExpression(){
        return constraint.toSQLExpression();
    }

    public String toString(){
        if(weight!=1.0)
            return "R("+String.valueOf(weight)+", "+constraint.toString()+")";
        else
            return "R("+constraint.toString()+")";

    }

    public SimpleTermPredicate getFirstTermPredicate(){
        return first;
    }

    public SimpleTermPredicate getSecondTermPredicate(){
        return second;
    }
}