package dragon.ir.index;

import dragon.nlp.compare.FrequencySortable;
import dragon.nlp.compare.IndexSortable;

/**
 * <p>IRRelation is the basic data structure for binary relation extracted from document which can be sorted and compared by index and frequency. </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IRRelation implements IRSignature, IndexSortable, FrequencySortable, Comparable, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int first, second;
    private int docFrequency;
    private int frequency;
    private int index;


    public IRRelation(int firstTermIndex, int secondTermIndex, int frequency) {
        this.first=firstTermIndex;
        this.second=secondTermIndex;
        this.index=-1;
        this.frequency=frequency;
        this.docFrequency =0;
    }

    public IRRelation(int index, int firstTermIndex, int secondTermIndex, int frequency, int docFrequency) {
        this.first=firstTermIndex;
        this.second=secondTermIndex;
        this.index=index;
        this.frequency=frequency;
        this.docFrequency =docFrequency;
    }

    public IRRelation copy(){
        return new IRRelation(index,first,second,frequency,docFrequency);
    }

    public int getFirstTerm(){
        return first;
    }

    public void setFirstTerm(int first){
        this.first =first;
    }

    public int getSecondTerm(){
        return second;
    }

    public void setSecondTerm(int second){
        this.second =second;
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
        this.docFrequency =freq;
    }

    public int compareTo(Object obj){
        int indexObj;

        indexObj=((IRRelation)obj).getFirstTerm();
        if(first==indexObj){
            indexObj=((IRRelation)obj).getSecondTerm();
            if(second==indexObj)
                return 0;
            else if(second>indexObj)
                return 1;
            else
                return -1;
        }
        else if(first>indexObj)
            return 1;
        else
            return -1;
    }
}