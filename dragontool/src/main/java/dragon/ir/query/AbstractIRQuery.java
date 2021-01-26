package dragon.ir.query;

import java.util.ArrayList;
/**
 * <p>Abstract class of IR query </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractIRQuery implements IRQuery{
    protected  Operator optr;
    protected  ArrayList children;
    private int queryKey;

    public AbstractIRQuery() {
    }

    public int getQueryKey(){
        return queryKey;
    }

    public void setQueryKey(int key){
        this.queryKey=key;
    }

    public boolean isPredicate(){
        return false;
    }

    public boolean isCompoundQuery(){
        return true;
    }

    public boolean isRelSimpleQuery(){
        return false;
    }

    public boolean isRelBoolQuery(){
        return false;
    }

    public boolean parse(String query){
        parse(getTokenList(query));
        return true;
    }

    public int getChildNum(){
        if(children==null)
            return 0;
        else
            return children.size();
    }

    public IRQuery getChild(int index){
        if(index>=getChildNum() || index<0)
            return null;
        return (IRQuery)children.get(index);
    }

    public double getSelectivity(){
        return 0;
    }

    public Operator getOperator(){
        return optr;
    }

    public String toString(){
        if(getChildNum()==0)
            return null;
        else if(getChildNum()==1)
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

    protected String[] getTokenList(String query){
        int i;
        String[] arrToken;
        StringBuffer sb;
        boolean lastSpace;

        lastSpace=false;
        sb=new StringBuffer(query.length());
        for(i=0;i<query.length();i++)
        {
            if("()=,".indexOf(query.charAt(i))>=0)
            {
                if(!lastSpace)
                    sb.append(' ');
                sb.append(query.charAt(i));
                sb.append(' ');
                lastSpace=true;
            }
            else if(query.charAt(i)!=' ')
            {
                sb.append(query.charAt(i));
                lastSpace=false;
            }
            else
            {
                if(lastSpace)
                    continue;
                else
                {
                    sb.append(' ');
                    lastSpace=true;
                }
            }
        }
        query=sb.toString().trim();
        arrToken=query.split(" ");
        return arrToken;
    }

    protected abstract void parse(String[] expression);
}