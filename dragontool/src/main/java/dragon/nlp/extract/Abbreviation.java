package dragon.nlp.extract;


import dragon.nlp.*;
import java.lang.Character;
import java.util.TreeMap;

/**
 * <p>Checking abbreviation for associated terms </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Abbreviation {
    private TreeMap abbrList;
    private Term referringTerm;
    private char firstUpperChar, lastUpperChar;

    public Abbreviation() {
        abbrList=new TreeMap();
    }

    public static void main(String[] args) {
        Abbreviation abbr = new Abbreviation();
        Sentence sent;
        Term term;

        sent=(new EngDocumentParser()).parseSentence("divalent metal transporter (DMT1).");
        term=new Term(sent.getFirstWord(),sent.getWord(2));
        abbr.isAbbrOfLastTerm(sent.getWord(4),term);
    }

    public void clearCachedAbbr(){
        abbrList.clear();
    }

    public boolean contains(String abbr){
        if(abbr.charAt(abbr.length()-1)=='s')
            abbr=abbr.substring(0,abbr.length()-1);
        referringTerm=(Term)abbrList.get(abbr.toUpperCase());
        if(referringTerm!=null)
            return true;
        else
            return false;
     }

    public boolean isAbbrOfLastTerm(Word word, Term lastTerm){
        String abbr;
        int upperCharNum;

        if(lastTerm==null) return false;
        if(word.prev==null) return false;
        if(word.next==null) return false;
        if(!word.prev.getContent().equalsIgnoreCase("(")) return false;
        if(!word.next.getContent().equalsIgnoreCase(")")) return false;
        if(!word.prev.prev.equals(lastTerm.getEndingWord())) return false;

        abbr=word.getContent();
        upperCharNum=analyzeUpperLetter(abbr);
        if(upperCharNum<2 || upperCharNum>lastTerm.getWordNum()) return false;

        if(!equalCharIgnoreCase(lastTerm.getStartingWord().getContent().charAt(0),firstUpperChar)) return false;
        if(!equalCharIgnoreCase(lastTerm.getEndingWord().getContent().charAt(0),lastUpperChar)) return false;
        return true;
    }

    public void put(String abbr, Term associatedTerm){
        if(abbr.charAt(abbr.length()-1)=='s')
            abbr=abbr.substring(0,abbr.length()-1);
        if(!abbrList.containsKey(abbr.toUpperCase())){
            abbrList.put(abbr.toUpperCase(),associatedTerm);
        }
    }

    public Term get(){
        return referringTerm;
    }

    public Term get(String abbr){
        Term cur;

        cur=(Term)abbrList.get(abbr.toUpperCase());
        return cur;
    }

    private boolean equalCharIgnoreCase(char a, char b){
        if(a==b || Math.abs(a-b)==32)
            return true;
        else
            return false;
    }

    private int analyzeUpperLetter(String abbr){
        int i,count;

        count=0;
        firstUpperChar=0;
        lastUpperChar=0;
        for(i=0;i<abbr.length();i++)
        {
            if(Character.isUpperCase(abbr.charAt(i)))
            {
                count++;
                if(firstUpperChar==0) firstUpperChar=abbr.charAt(i);
                lastUpperChar=abbr.charAt(i);
            }
        }
        return count;
    }

}