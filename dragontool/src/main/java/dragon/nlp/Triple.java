package dragon.nlp;

import dragon.nlp.compare.SortedElement;
/**
 * <p>Triple is data structure for binary relation extracted from text</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Triple implements Comparable, SortedElement{
    private Concept first, second;
    private String rel;
    private String[] can_rel;
    private int freq;
    private int index;
    private double weight;
    private Object memo;

    public Triple(Concept first, Concept second) {
        this.first=first;
        this.second=second;
        rel=null;
        can_rel=null;
        freq=1;
        weight=0;
        memo=null;
    }

    public int compareTo(Object obj) {
        int objIndex;

        objIndex = ( (Triple) obj).getFirstConcept().getIndex();
        if(first.getIndex()==objIndex){
            objIndex=( (Triple) obj).getSecondConcept().getIndex();
            if(second.getIndex()==objIndex)
                return 0;
            else if(second.getIndex()>objIndex)
                return 1;
            else
                return -1;
        }
        else if(first.getIndex()>objIndex)
            return 1;
        else
            return -1;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Object getMemo(){
        return memo;
    }

    public void setMemo(Object memo){
        this.memo=memo;
    }


    public Concept getFirstConcept(){
        return first;
    }

    public Concept getSecondConcept(){
        return second;
    }

    public String getTUI(){
        return rel;
    }

    public void setTUI(String rel){
        this.rel=rel;
    }

    public String[] getCandidateTUI(){
        return can_rel;
    }

    public String getCandidateTUI(int index){
        return can_rel[index];
    }

    public int getCandidateTUINum() {
        if (can_rel == null) {
            return 0;
        }
        else {
            return can_rel.length;
        }
    }

    public void setCandidateTUI(String[] can_rel){
        this.can_rel=can_rel;
    }

    public double getWeight(){
        return weight;
    }

    public void setWeight(double weight){
        this.weight=weight;
    }

    public int getFrequency(){
        return freq;
    }

    public void setFrequency(int freq){
        this.freq=freq;
    }

    public void addFrequency(int inc){
        this.freq=this.freq+inc;
    }

    public boolean equalTo(Triple triple){
        if(first.equalTo(triple.getFirstConcept()) && second.equalTo(triple.getSecondConcept()) ||
           first.equalTo(triple.getSecondConcept()) && second.equalTo(triple.getFirstConcept()))
            return true;
        else
            return false;
    }
}