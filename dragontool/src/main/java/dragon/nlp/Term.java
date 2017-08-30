package dragon.nlp;

import dragon.util.SortedArray;
import dragon.nlp.compare.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>Term is designed for Universal Medical Langauge System (UMLS) medical concept
 * that can stores n-grams and its semantic and statistical information</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Term implements Concept, Comparable, SortedElement{
    public static final int  NAME_ASIS=0;
    public static final int  NAME_LEMMA=1;
    public static final int  NAME_NORM=2;
    private static int nameMode=NAME_NORM;

    private Word starting, ending;
    private Term referral;
    private Object memo;
    private SortedArray attributes;
    private String normalizedStr;
    private String[] can_cui;
    private String cui;
    private String[] can_tui;
    private String tui;
    private int freq;
    private double weight;
    private int len;
    private int index;
    private boolean predicted;
    private boolean expired;
    private boolean subterm;

    public Term(Word starting, Word ending) {
        this.starting = starting;
        this.ending = ending;
        attributes = null;
        tui = null;
        cui = null;
        weight = 0;
        predicted = false;
        freq = 1;
        index = 0;
        len = 1;
        memo = null;
        expired = false;
        normalizedStr=null;
        referral=null;
        subterm=false;
        Word next = starting;
        while (!next.equals(ending)) {
            len++;
            next = next.next;
        }
    }

    public Term(Word word) {
        this.starting = word;
        this.ending = word;
        attributes = null;
        tui = null;
        cui = null;
        predicted = false;
        freq = 1;
        index = 0;
        memo = null;
        expired = false;
        weight = 0;
        normalizedStr=null;
        referral=null;
        len = 1;
    }

    public Concept copy(){
        Term newTerm;

        newTerm=new Term(getStartingWord(),getEndingWord());
        newTerm.setReferral(getReferral());
        newTerm.setFrequency(getFrequency());
        newTerm.setCUI(getCUI());
        newTerm.setCandidateCUI(getCandidateCUI());
        newTerm.setTUI(getTUI());
        newTerm.setCandidateTUI(getCandidateTUI());
        newTerm.setSubConcept(isSubConcept());
        return newTerm;
    }

    public int getConceptType(){
        return Concept.TYPE_TERM;
    }

    public static void setNameMode(int mode){
        if(mode<0 || mode>NAME_NORM)
            return;
        nameMode =mode;
    }

    public int compareTo(Object obj) {
        String objValue;

        objValue = ( (Term) obj).toString();
        return toString().compareToIgnoreCase(objValue);
    }

    public int compareTo(Term term) {
        return toString().compareToIgnoreCase(term.toString());
    }

    public boolean isSubConcept(){
        return subterm;
    }

    public void setSubConcept(boolean subterm){
        this.subterm=subterm;
    }

    public String getName(){
        if(nameMode==Term.NAME_ASIS)
            return toString();
        else if(nameMode==Term.NAME_LEMMA)
            return toLemmaString();
        else
            return toNormalizedString();
    }

    public String getEntryID(){
        if(cui!=null)
            return cui;
        else if(can_cui!=null)
            return can_cui[0];
        else
            return null;
    }

    public String getSemanticType(){
        if(tui!=null)
            return tui;
        else if(can_tui!=null)
            return can_tui[0];
        else
            return null;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public boolean isPredicted() {
        return predicted;
    }

    public void setPredictedTerm(boolean predicted) {
        this.predicted = predicted;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public String[] getCandidateCUI() {
        return can_cui;
    }

    public String getCandidateCUI(int index) {
        return can_cui[index];
    }

    public int getCandidateCUINum() {
        if (can_cui == null) {
            return 0;
        }
        else {
            return can_cui.length;
        }
    }

    public void setCandidateCUI(String[] can_cui) {
        this.can_cui = can_cui;
    }

    public String getCUI() {
        return cui;
    }

    public void setCUI(String cui) {
        this.cui = cui;
    }

    public int getSenseNum() {
        if (can_cui == null) {
            return 0;
        }
        else {
            return can_cui.length;
        }
    }

    public String[] getCandidateTUI() {
        return can_tui;
    }

    public String getCandidateTUI(int index) {
        return can_tui[index];
    }

    public int getCandidateTUINum() {
        if (can_tui == null) {
            return 0;
        }
        else {
            return can_tui.length;
        }
    }

    public void setCandidateTUI(String[] can_tui) {
        this.can_tui = can_tui;
    }

    public String getTUI() {
        return tui;
    }

    public void setTUI(String tui) {
        this.tui = tui;
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

    public Term getReferral() {
        return referral;
    }

    public void setReferral(Term referral) {
        this.referral = referral;
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

        if(referral!=null)
            return referral.toLemmaString();
        next = starting;
        term = next.getLemma();
        while (!next.equals(ending)) {
            next = next.next;
            term = term + " " + next.getLemma();
        }
        return term;
    }

    public void setNormalizedString(String normalizedStr){
        this.normalizedStr=normalizedStr;
    }

    public String toNormalizedString() {
        Word next;
        StringBuffer term;
        ArrayList list;
        int i;

        if(referral!=null)
            return referral.toNormalizedString();
        if(normalizedStr!=null){
            return normalizedStr;
        }
        if (starting.equals(ending)) {
            return starting.getLemma();
        }

        list = new ArrayList(4);
        next = starting;
        while (next != null) {
            if (!next.canIgnore()) {
                if(next.getLemma()!=null)
                    list.add(next.getLemma());
                else
                    list.add(next.getContent().toLowerCase());
            }
            if (!next.equals(ending)) {
                next = next.next;
            }
            else {
                break;
            }
        }
        Collections.sort(list);

        term = new StringBuffer( (String) list.get(0));
        for (i = 1; i < list.size(); i++) {
            term.append(' ');
            term.append( (String) list.get(i));
        }
        normalizedStr=term.toString();
        return normalizedStr;
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
        String objFirst, objSecond;

        objFirst=this.getEntryID();
        objSecond=concept.getEntryID();
        if(objFirst!=null && objSecond!=null)
            return objFirst.equalsIgnoreCase(objSecond);
        else
            return this.getName().equalsIgnoreCase(concept.getName());
    }

    public int getAttributeNum() {
        if (attributes == null) {
            return 0;
        }
        else {
            return attributes.size();
        }
    }

    public int getAttributeOccurrence() {
        int count, i;

        if (getAttributeNum() == 0) {
            return 0;
        }
        count = 0;
        for (i = 0; i < getAttributeNum(); i++) {
            count = count + getAttribute(i).getFrequency();
        }
        return count;
    }

    public Term getAttribute(int index) {
        return (Term) attributes.get(index);
    }

    public void addAttribute(Term attr) {
        if (attributes == null) {
            attributes = new SortedArray(2, new TermLemmaComparator());
            attributes.add(attr);
        }
        else {
            if (!attributes.add(attr)) {
                Term cur = (Term) attributes.get(attributes.insertedPos());
                cur.addFrequency(attr.getFrequency());
            }
        }
    }
    
    /**
     * Search the first occurrence of the current term in the sentence
     * @param sent the sentence to be searched
     * @return the starting word of the term in the sentence
     */
    public Word searchIn(Sentence sent){
		return searchIn(sent,null);
	}
    
    /**
     * Search the first occurrence of the current term in the sentence after the word specified
     * @param sent the sentence to be searched
     * @param start the word from which the search start
     * @return the starting word of the term in the sentence
     */
    public Word searchIn(Sentence sent, Word start){
		Word s, sCur, cCur;
		int count;
		
		if(start==null)
			s=sent.getFirstWord();
		else
			s=start;
		while(s!=null && s.getPosInSentence()+getWordNum()<=sent.getWordNum()){
			sCur=s;
			cCur=getStartingWord();
			count=0;
			while(count<getWordNum() && sCur.getContent().equalsIgnoreCase(cCur.getContent())){
				count++;
				sCur=sCur.next;
				cCur=cCur.next;
			}
			if(count==getWordNum()){
				return s;
			}
			s=s.next;
		}
		return null;
	}

    public int hashCode(){
        if (index >= 0)
            return index;
        else
            return super.hashCode();
    }
}