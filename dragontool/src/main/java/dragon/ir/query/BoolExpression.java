package dragon.ir.query;

import java.util.ArrayList;
/**
 * <p>Bool query expression</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BoolExpression extends AbstractExpression{
    private ArrayList children;

    public BoolExpression(String[] expression) {
        children=new ArrayList();
        expressionType=EXPRESSION_BOOL;
        parse(expression);
    }

    public int getChildNum(){
        if(children==null)
            return 0;
        else
            return children.size();
    }

    public Expression getChild(int index){
        if(index>=getChildNum() || index<0)
            return null;
        return (Expression)children.get(index);
    }

    public Operator getOperator(){
        return optr;
    }

    public String toSQLExpression(){
        if(getChildNum()==0)
            return null;
        else if(getChildNum()==1)
            return getChild(0).toSQLExpression();
        else
        {
            StringBuffer buf=new StringBuffer();
            if(optr.toString().equalsIgnoreCase("OR")) buf.append('(');
            for(int i=0;i<getChildNum();i++)
            {
                buf.append(getChild(i).toSQLExpression());
                if(i<getChildNum()-1){
                    buf.append(' ');
                    buf.append(getOperator().toString());
                    buf.append(' ');
                }
            }
            if(optr.toString().equalsIgnoreCase("OR")) buf.append(')');
            return buf.toString();
        }
    }

    public String toString(){
        if(children.size()==1)
            return getChild(0).toString();
        else
        {
            StringBuffer buf=new StringBuffer();
            if(optr.toString().equalsIgnoreCase("OR")) buf.append('(');
            for(int i=0;i<getChildNum();i++)
            {
                buf.append(getChild(i).toString());
                if(i<getChildNum()-1){
                    buf.append(' ');
                    buf.append(getOperator().toString());
                    buf.append(' ');
                }
            }
            if(optr.toString().equalsIgnoreCase("OR")) buf.append(')');
            return buf.toString();
        }
    }

    private void parse(String[] expression){
        ArrayList andList;
        String[] part;
        BoolExpression curBoolExpression;
        SimpleExpression curSimpleExpression;
        int start, end, i, j, lastOR, lastAND;
        int leftParenthesis, rightParenthesis;
        boolean found;

        andList=new ArrayList();
        start=0;
        end=expression.length-1;
        found=false;

        while(!found){
            lastOR=start-1;
            leftParenthesis=0;
            rightParenthesis=0;
            for (i = start; i <=end; i++) {
                if(expression[i].equalsIgnoreCase("("))
                    leftParenthesis++;
                else if(expression[i].equalsIgnoreCase(")"))
                    rightParenthesis++;
                else if(expression[i].equalsIgnoreCase("OR"))
                {
                    if(leftParenthesis==rightParenthesis){
                        found=true;
                        part=new String[i-lastOR-1];
                        System.arraycopy(expression, lastOR+1,part,0, part.length);
                        curBoolExpression=new BoolExpression(part);
                        children.add(curBoolExpression);
                        lastOR=i;
                    }
                }
                else if(expression[i].equalsIgnoreCase("AND"))
                {
                    if(leftParenthesis==rightParenthesis)
                        andList.add(new Integer(i));
                }
            }

            if(found){
                optr=new Operator("OR");
                part=new String[end-lastOR];
                System.arraycopy(expression, lastOR+1,part,0, part.length);
                curBoolExpression=new BoolExpression(part);
                children.add(curBoolExpression);
            }
            else
            {
                if(andList.size()>=1)
                {
                    found=true;
                    optr=new Operator("AND");
                    lastAND=start-1;
                    andList.add(new Integer(end+1));

                    for(i=0;i<andList.size();i++){
                        j=((Integer)andList.get(i)).intValue();
                        part=new String[j-lastAND-1];
                        System.arraycopy(expression, lastAND+1,part,0, part.length);
                        curBoolExpression=new BoolExpression(part);
                        children.add(curBoolExpression);
                        lastAND=j;
                    }
                }
                else if(expression[start].equalsIgnoreCase("(") && expression[end].equalsIgnoreCase(")"))
                {
                    start=start+1;
                    end=end-1;
                }
                else
                {
                    optr=new Operator("AND");
                    found=true;
                    curSimpleExpression=new SimpleExpression(expression);
                    children.add(curSimpleExpression);
                }
            }
        }

    }
}