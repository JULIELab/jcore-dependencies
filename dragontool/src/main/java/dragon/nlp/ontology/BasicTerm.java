package dragon.nlp.ontology;

/**
 * <p>A light data structure for term of a given ontology </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicTerm implements Comparable{
    private String term;
    private String[] arrCUI;
    private int index;

    public BasicTerm(int index, String term, String[] cuis) {
        this.term=term;
        this.arrCUI=cuis;
        this.index=index;
    }

    public int compareTo(Object obj) {
        String objValue;

        objValue = ( (BasicTerm) obj).getTerm();
        return term.compareToIgnoreCase(objValue);
    }

    public int getIndex(){
        return index;
    }

    public String getTerm(){
        return term;
    }

    public int getCUINum(){
        if(arrCUI==null)
            return 0;
        else
            return arrCUI.length;
    }

    public String getCUI(int index){
        return arrCUI[index];
    }

    public String[] getAllCUI(){
        return arrCUI;
    }

    public String toString(){
        return term;
    }
}