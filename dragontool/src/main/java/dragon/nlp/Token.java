package dragon.nlp;

import dragon.nlp.compare.SortedElement;
/**
 * <p>Token is a cute unit data structure for handling nlp task or other operations</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Token implements Concept, Comparable, SortedElement{
    private String value;
    private int index, freq;
    private double weight;
    private Object memo;

    public Token(String value) {
        freq=1;
        this.value=value;
        index=-1;
        weight=-1;
        memo=null;
    }

    public Token(int index, int frequency) {
        this.freq=frequency;
        this.index=index;
        weight=-1;
        value=null;
        memo=null;
    }

    public Token(String value, int index, int frequency) {
        this.freq=frequency;
        this.value=value;
        this.index=index;
        weight=-1;
        memo=null;
    }

    public Concept copy(){
        Token cur;

        cur=new Token(value);
        cur.setFrequency(freq);
        cur.setIndex(index);
        cur.setWeight(weight);
        return cur;
    }

    public int getConceptType(){
        return Concept.TYPE_TOKEN;
    }

    public String getName(){
        return value;
    }

    public String getEntryID(){
        return value;
    }

    public String getSemanticType(){
        return null;
    }

    public boolean isSubConcept(){
        return false;
    }

    public Word getStartingWord(){
        return null;
    }

    public Word getEndingWord(){
        return null;
    }

    public Object getMemo(){
        return memo;
    }

    public void setMemo(Object memo){
        this.memo=memo;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value =value;
    }

    public void setFrequency(int freq){
        this.freq=freq;
    }

    public void addFrequency(int inc){
        this.freq+=inc;
    }

    public int getFrequency(){
        return freq;
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
        String objValue;

        objValue=((Token)obj).getValue();
        return value.compareToIgnoreCase(objValue);
    }

    public int compareTo(Token token){
        return value.compareToIgnoreCase(token.getValue());
    }

    public boolean equals(Object obj){
        if(value==null){
            return index==((Concept)obj).getIndex();
        }
        else{
            return value.equalsIgnoreCase(((Concept)obj).getName());
        }
    }

    public boolean equalTo(Concept concept) {
        if(value==null){
            return index==concept.getIndex();
        }
        else{
            return value.equalsIgnoreCase(concept.getName());
        }
    }

    public String  toString(){
        return value;
    }

    public int hashCode(){
        if(index>=0)
            return index;
        else
            return super.hashCode();
    }
}