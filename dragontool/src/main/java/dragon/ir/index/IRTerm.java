package dragon.ir.index;

import dragon.nlp.compare.*;

/**
 * <p>This is basic indexing unit which can be sorted and compared by index and frequency </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IRTerm implements IRSignature, IndexSortable, FrequencySortable, Comparable, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private String key;
    private int docFrequency;
    private int frequency;
    private int index;

    public IRTerm(String key) {
        this.key=key;
        this.docFrequency =0;
        this.frequency =0;
        this.index=-1;
    }

    public IRTerm(int index, int frequency) {
        this.key=null;
        this.docFrequency =0;
        this.frequency =frequency;
        this.index=index;
    }

    public IRTerm(String key, int index, int frequency) {
        this.key=key;
        this.docFrequency =0;
        this.frequency =frequency;
        this.index=index;
    }

    public IRTerm(int index, int frequency, int docFrequency) {
        this.key=null;
        this.docFrequency =docFrequency;
        this.frequency =frequency;
        this.index=index;
    }

    public IRTerm copy(){
        IRTerm cur;

        cur=new IRTerm(index,frequency,docFrequency);
        //cur.setKey(key==null?null:new String(key));
        cur.setKey(key);
        return cur;
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key =key;
    }

    public void setFrequency(int freq){
        this.frequency=freq;
    }

    public void addFrequency(int inc){
        this.frequency+=inc;
    }

    public int getFrequency(){
        return frequency;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index =index;
    }

    public int getDocFrequency(){
        return docFrequency;
    }

    public void addDocFrequency(int inc){
        docFrequency+=inc;
    }

    public void setDocFrequency(int freq){
        this.docFrequency=freq;
    }

    public int compareTo(Object obj){
        return key.compareToIgnoreCase(((IRTerm)obj).getKey());
    }
}