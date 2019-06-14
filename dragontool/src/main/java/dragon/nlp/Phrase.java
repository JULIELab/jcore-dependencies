package dragon.nlp;

import dragon.nlp.compare.SortedElement;

/**
 * <p>Data structure for phrase</p>
 * <p>: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Phrase implements Concept, Comparable, SortedElement{
    public static final int  NAME_ASIS=0;
    public static final int  NAME_LEMMA=1;
    private static int nameMode=NAME_ASIS;

    private Word starting, ending;
    private Object memo;
    private int freq;
    private double weight;
    private int len;
    private int index;
    private boolean subterm;

    public Phrase(Word starting, Word ending) {
        this.starting = starting;
        this.ending = ending;
        weight = 0;
        freq = 1;
        index = 0;
        len = 1;
        memo = null;
        subterm=false;
        Word next = starting;
        while (!next.equals(ending)) {
            len++;
            next = next.next;
        }
    }

    public Phrase(Word word) {
        this.starting = word;
        this.ending = word;
        freq = 1;
        index = 0;
        memo = null;
        weight = 0;
        len = 1;
        subterm=false;
    }

    public Concept copy(){
        Phrase newTerm;

        newTerm=new Phrase(getStartingWord(),getEndingWord());
        newTerm.setFrequency(getFrequency());
        newTerm.setSubConcept(isSubConcept());
        newTerm.setIndex(getIndex());
        newTerm.setWeight(getWeight());
        return newTerm;
    }

    public int getConceptType(){
        return Concept.TYPE_PHRASE;
    }

    public static void setNameMode(int mode){
        if(mode<0 || mode>NAME_LEMMA)
            return;
        nameMode =mode;
    }

    public int compareTo(Object obj) {
        String objValue;

        objValue = ( (Phrase) obj).toString();
        return toString().compareToIgnoreCase(objValue);
    }

    public int compareTo(Phrase term) {
        return toString().compareToIgnoreCase(term.toString());
    }

    public boolean isSubConcept(){
        return subterm;
    }

    public void setSubConcept(boolean subterm){
        this.subterm=subterm;
    }

    public String getName(){
        if(nameMode==Phrase.NAME_ASIS)
            return toString();
        else
            return toLemmaString();
    }

    public String getEntryID(){
        return getName();
    }

    public String getSemanticType(){
         return null;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public Word getStartingWord() {
        return starting;
    }

    public Word getEndingWord() {
        return ending;
    }

    public Object getMemo() {
        return memo;
    }

    public void setMemo(Object memo) {
        this.memo = memo;
    }

    public String toString() {
        Word next;
        StringBuffer term;
        boolean lastPunctuation;

        term=new StringBuffer(starting.getContent());
        next = starting;
        lastPunctuation=false;
        while (!next.equals(ending)) {
            next = next.next;
            if(next.isPunctuation())
            {
                term.append(next.getContent());
                lastPunctuation=true;
            }
            else
            {
                if(lastPunctuation)
                    term.append(next.getContent());
                else
                {
                    term.append(' ');
                    term.append(next.getContent());
                }
                lastPunctuation=false;
            }
        }
        return term.toString();
    }

    public String toLemmaString() {
        Word next;
        String term;

        next = starting;
        term = next.getLemma();
        while (!next.equals(ending)) {
            next = next.next;
            term = term + " " + next.getLemma();
        }
        return term;
    }

    public int getWordNum() {
        return len;
    }

    public void addFrequency(int inc) {
        freq = freq + inc;
    }

    public int getFrequency() {
        return freq;
    }

    public void setFrequency(int freq) {
        this.freq = freq;
    }

    public boolean equalTo(Concept concept) {
        return this.getName().equalsIgnoreCase(concept.getName());
    }
}
