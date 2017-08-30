package dragon.ir.query;

/**
 * <p>Simple expression</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleExpression extends AbstractExpression{
    private String field;
    private Object testValue;

    public SimpleExpression(String field, Operator optr, Object testValue) {
        this.field=field;
        this.optr=optr;
        this.testValue=testValue;
        this.expressionType=EXPRESSION_SIMPLE;
    }

    public SimpleExpression(String[] expression) {
        StringBuffer testValue;
        int k;

        testValue=new StringBuffer(expression[2]);
        for(k=3;k<expression.length;k++){
            testValue.append(' ');
            testValue.append(expression[k]);
        }

        this.field=expression[0];
        this.optr=new Operator(expression[1]);
        this.testValue=testValue.toString();
        this.expressionType=EXPRESSION_SIMPLE;
    }

    public int getChildNum(){
        return 0;
    }

    public Expression getChild(int index){
        return null;
    }

    public String toSQLExpression(){
        StringBuffer sb;

        sb=new StringBuffer(field);
        sb.append(optr.toString());
        sb.append("\'");
        sb.append(testValue.toString());
        sb.append("\'");
        return sb.toString();
    }

    public String toString(){
        StringBuffer sb;

        sb=new StringBuffer(field);
        sb.append(optr.toString());
        sb.append(testValue.toString());
        return sb.toString();
    }

    public String getField(){
        return field;
    }

    public Object getTestValue(){
        return testValue;
    }

    public Operator getOperator(){
        return optr;
    }
}