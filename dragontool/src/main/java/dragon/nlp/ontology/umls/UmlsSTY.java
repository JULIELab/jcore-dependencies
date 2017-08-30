package dragon.nlp.ontology.umls;

/**
 * <p>UMLS semantic type </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsSTY implements Comparable{
    private int index;
    private String sty, hier,desc;
    private boolean isRelation;

    public UmlsSTY(int index, String sty,  String desc, String hier, boolean isRelation) {
        this.index=index;
        this.sty=sty;
        this.hier=hier;
        this.desc=desc;
        this.isRelation=isRelation;
    }

    public int compareTo(Object obj) {
        String objValue;

        objValue = ( (UmlsSTY) obj).toString();
        return toString().compareTo(objValue);
    }

    public int getIndex(){
        return index;
    }

    public String getSTY(){
        return sty;
    }

    public String getHier(){
        return hier;
    }

    public boolean isRelation(){
        return isRelation;
    }

    public boolean isSemanticType(){
        return !isRelation;
    }

    public String getDescription(){
        return desc;
    }

    public String toString(){
        return sty;
    }

}