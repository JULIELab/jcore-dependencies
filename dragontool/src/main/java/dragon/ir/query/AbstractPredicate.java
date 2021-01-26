package dragon.ir.query;

/**
 * <p>Abstract class of predicate</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

abstract public class AbstractPredicate extends AbstractIRQuery implements Predicate{
    protected double weight;
    protected int predicateType, expressionType;
    protected Expression constraint;

    public AbstractPredicate(){
        weight=1.0;
        predicateType=0;
        expressionType=0;
        constraint=null;
    }

    public boolean parse(String predicate){
        parse(getTokenList(predicate));
        return true;
    }

    public Operator getOperator(){
        return constraint.getOperator();
    }

    public boolean isPredicate(){
        return true;
    }

    public boolean isCompoundQuery(){
        return false;
    }

    public IRQuery getChild(int index){
        return null;
    }

    public int getChildNum(){
        return 0;
    }

    public double getSelectivity(){
        return 0;
    }

    public double getWeight(){
        return weight;
    }

    public void setWeight(double weight){
        this.weight=weight;
    }

    public Expression getConstraint(){
        return constraint;
    }

    public boolean isSimplePredicate(){
        return expressionType==PREDICATE_SIMPLE;
    }

    public boolean isBoolPredicate(){
        return expressionType==PREDICATE_BOOL;
    }

    public boolean isTermPredicate(){
        return predicateType==PREDICATE_TERM;
    }

    public boolean isRelationPredicate(){
        return predicateType==PREDICATE_RELATION;
    }

    public boolean isQualifierPredicate(){
        return predicateType==PREDICATE_QUALIFIER;
    }

    abstract protected void parse(String[] predicate);
}