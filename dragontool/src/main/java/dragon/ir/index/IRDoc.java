package dragon.ir.index;

import dragon.nlp.compare.IndexSortable;
import dragon.nlp.compare.WeightSortable;
/**
 * <p>IRDoc is data structure for IR document indexing which can be sorted and thus compared by weight and index </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IRDoc implements WeightSortable, IndexSortable, Comparable, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int index;
    private int category;
    private String key;
    private int termNum, termCount;
    private int relationNum, relationCount;
    private double weight;

    public IRDoc(String key) {
        this.key=key;
        index =-1;
        category=-1;
        termNum=0;
        termCount=0;
        relationNum=0;
        relationCount=0;
    }

    public IRDoc(int index) {
        this.index =index;
        key=null;
        termNum=0;
        termCount=0;
        relationNum=0;
        relationCount=0;
    }

    public IRDoc copy(){
        IRDoc cur;

        //cur=new IRDoc(key==null?null:new String(key));
        cur=new IRDoc(key);
        cur.setIndex(index);
        cur.setTermCount(termCount);
        cur.setTermNum(termNum);
        cur.setRelationCount(relationCount);
        cur.setRelationNum(relationNum);
        cur.setWeight(weight);
        return cur;
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key =key;
    }

    public int getCategory(){
        return category;
    }

    public void setCategory(int category){
        this.category =category;
    }

    public int getTermNum(){
        return termNum;
    }

    public void setTermNum(int termNum){
        this.termNum=termNum;
    }

    public int getTermCount(){
        return termCount;
    }

    public void setTermCount(int termCount){
        this.termCount=termCount;
    }

    public int getRelationNum(){
        return relationNum;
    }

    public void setRelationNum(int relationNum){
        this.relationNum =relationNum;
    }

    public int getRelationCount(){
        return relationCount;
    }

    public void setRelationCount(int relationCount){
        this.relationCount =relationCount;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public void setWeight(double weight)
    {
        this.weight=weight;
    }

    public double getWeight(){
        return weight;
    }

    public int compareTo(Object obj){
        String objKey;

        objKey=((IRDoc)obj).getKey();
        return key.compareTo(objKey);
    }

    public int compareTo(IRDoc doc){
        return key.compareTo(doc.getKey());
    }
}