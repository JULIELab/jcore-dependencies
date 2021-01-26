package dragon.ir.query;

/**
 * <p>Query operator </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Operator {
    private static final String optrs=" AND OR = LIKE ";
    String optr;

    public Operator(String optr) {
        if(isOperator(optr))
            this.optr=optr.toUpperCase();
        else
            optr=null;
    }

    public boolean test(Object firstValue, Object secondValue){
        return false;
    }

    public String toString(){
        return optr;
    }

    public boolean isOperator(String optr){
        return optrs.indexOf(" "+optr.toUpperCase()+" ")>=0;
    }

}