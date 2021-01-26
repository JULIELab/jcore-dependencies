package dragon.ir.query;

import java.util.ArrayList;
/**
 * <p>Bool realtion query </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class RelBoolQuery extends AbstractIRQuery {
    public RelBoolQuery(){
        optr=null;
        children=null;
    }

    public RelBoolQuery(String[] expression) {
        parse(expression);
    }

    public RelBoolQuery(String expression) {
        parse(expression);
    }

    public static void main(String[] args){
        String expression;
        RelBoolQuery query;

        expression = "T((tui=T116 or tui=T028) and string like %aaa%)";
        query=new RelBoolQuery(expression);
        System.out.println(query.toString());
    }

    public boolean isRelBoolQuery(){
        return true;
    }

    protected void parse(String[] expression){
        ArrayList andList;
        String[] part;
        IRQuery curQuery;
        int start, end, i, j, lastOR, lastAND;
        int leftParenthesis, rightParenthesis;
        boolean found;

        optr=null;
        children=new ArrayList();

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
                        curQuery=new RelBoolQuery(part);
                        children.add(curQuery);
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
                curQuery=new RelBoolQuery(part);
                children.add(curQuery);
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
                        curQuery=new RelBoolQuery(part);
                        children.add(curQuery);
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
                    found=true;
                    optr=new Operator("AND");
                    children.add(getPredicate(expression,start,end-start));
                }
            }
        }

    }

    private Predicate getPredicate(String[] expression,int start, int length){
        String[] part;
        double weight;
        Predicate curPredicate;

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

        if (expression[start].equalsIgnoreCase("T"))
            curPredicate=new BoolTermPredicate(part);
        else if (expression[start].equalsIgnoreCase("R"))
            curPredicate=new BoolRelationPredicate(part);
        else if (expression[start].equalsIgnoreCase("M"))
            curPredicate=new BoolQualifierPredicate(part);
        else
            return null;

        curPredicate.setWeight(weight);
        return curPredicate;
    }

}