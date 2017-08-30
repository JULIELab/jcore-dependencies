package dragon.ir.query;

import dragon.nlp.compare.SortedElement;
/**
 * <p>Simple term predicate </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleTermPredicate extends AbstractPredicate implements SortedElement{
    private int termFrequency;
    private int index;
    private int docFrequency;

    public SimpleTermPredicate(String[] predicate) {
        parse(predicate);
    }

    public SimpleTermPredicate(SimpleExpression constraint){
        predicateType=PREDICATE_TERM;
        expressionType=PREDICATE_SIMPLE;
        this.constraint=constraint;
    }

    protected void parse(String[] predicate){
        predicateType=PREDICATE_TERM;
        expressionType=PREDICATE_SIMPLE;
        constraint=new SimpleExpression(predicate);
    }

    public SimpleTermPredicate copy(){
        SimpleTermPredicate cur;

        cur=new SimpleTermPredicate((SimpleExpression)constraint);
        cur.setWeight(weight);
        cur.setIndex(index);
        cur.setDocFrequency(docFrequency);
        cur.setFrequency(termFrequency);
        return cur;
    }

    public String getKey(){
        return getTestValue();
    }

    public String getField(){
        return ((SimpleExpression)constraint).getField();
    }

    public String getTestValue(){
        return (String)((SimpleExpression)constraint).getTestValue();
    }

    public String toSQLExpression(){
        return constraint.toSQLExpression();
    }

    public String toString(){
        if(weight!=1.0)
            return "T("+String.valueOf(weight)+", "+constraint.toString()+")";
        else
            return "T("+constraint.toString()+")";
    }

    public int getDocFrequency(){
        return docFrequency;
    }

    public void setDocFrequency(int freq){
        this.docFrequency =freq;
    }

    public int getFrequency(){
        return termFrequency;
    }

    public void setFrequency(int freq){
        this.termFrequency =freq;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index =index;
    }
}