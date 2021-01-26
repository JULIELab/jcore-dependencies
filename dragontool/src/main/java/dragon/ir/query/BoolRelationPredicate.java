package dragon.ir.query;

/**
 * <p>Bool relation predicate</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class  BoolRelationPredicate extends AbstractPredicate{
    public BoolRelationPredicate(String[] predicate) {
        parse(predicate);
    }

    protected void parse(String[] predicate){
        weight=1;
        predicateType=Predicate.PREDICATE_RELATION;
        expressionType=PREDICATE_BOOL;
        constraint=new BoolExpression(predicate);
    }

    public String toSQLExpression(){
        return constraint.toSQLExpression();
    }

    public String toString(){
        return "R("+constraint.toString()+")";
    }



}