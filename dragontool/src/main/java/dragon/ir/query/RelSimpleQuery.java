package dragon.ir.query;

import java.util.ArrayList;
/**
 * <p>Simple relation Query</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class RelSimpleQuery extends AbstractIRQuery {
    public RelSimpleQuery(){
        children=null;
        optr=null;
    }

    public RelSimpleQuery(String query) {
        parse(query);
    }

    public static void main(String[] args) {
        RelSimpleQuery query;
        String expression;

        expression="R(1.0,TERM1=C0007114 AND TERM2=C0012899) T(0.3,TERM=Y0000004)";
        query=new RelSimpleQuery(expression);
        for(int i=0;i<query.getChildNum();i++)
            System.out.println(query.toString());
    }

    public boolean isRelSimpleQuery(){
        return true;
    }

    public boolean add(Predicate predicate){
        if(!predicate.isSimplePredicate())
            return false;
        if(children==null)
            children=new ArrayList();
        children.add(predicate);
        return true;
    }

    public String toString(){
        StringBuffer sb;
        int i;

        if(getChildNum()==0)
            return null;
        else if(getChildNum()==1)
            return getChild(0).toString();
        else{
            sb = new StringBuffer();
            for (i = 0; i < getChildNum(); i++) {
                if (i > 0)
                    sb.append(' ');
                sb.append(getChild(i).toString());
            }
            return sb.toString();
        }
    }

    protected void parse(String[] arrToken){
        int i, start, flag, leftParenthesis, rightParenthesis;
        String curToken;

        optr = new Operator("OR");
        children = new ArrayList();
        i=0;
        flag=0;
        leftParenthesis=0;
        rightParenthesis=0;
        start=0;

        while(i<arrToken.length)
        {
            curToken=arrToken[i];
            if(curToken.equals("(")){
                if(i>=1 && "TMR".indexOf(arrToken[i-1])>=0){
                    start=i-1;
                    leftParenthesis=1;
                    rightParenthesis=0;
                    flag=1;
                }
                else
                    leftParenthesis++;
            }
            else if(curToken.equals(")")){
                rightParenthesis++;
                if(flag==1 && leftParenthesis==rightParenthesis) //find a predicate
                {
                    flag=0;
                    children.add(getPredicate(arrToken,start,i-start));
                }
            }
            i++;
        }
    }

    private Predicate getPredicate(String[] expression,int start, int length){
        String[] part;
        double weight;
        Predicate predicate;

        if(expression[start+3].equalsIgnoreCase(",")){
            weight=Double.parseDouble(expression[start+2]);
            part=new String[length-4];
            System.arraycopy(expression, start+4,part,0,part.length);
        }
        else{
            weight=1.0;
            part=new String[length-2];
            System.arraycopy(expression, start+2,part,0,part.length);
        }

        if(expression[start].equalsIgnoreCase("T"))
            predicate=new SimpleTermPredicate(part);
        else if(expression[start].equalsIgnoreCase("R"))
            predicate=new SimpleRelationPredicate(part);
        else
            return null;
        predicate.setWeight(weight);
        return predicate;
    }
}