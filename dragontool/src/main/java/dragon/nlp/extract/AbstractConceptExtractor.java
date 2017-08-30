package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.nlp.compare.*;
import dragon.util.SortedArray;
import java.io.PrintWriter;
import java.util.*;

/**
 * <p>Abstract class for concept extraction which is the super class of AbstractPhraseExtractor,
 * AbstractTermExtractor, AbstractTokenExtrator, and AbstractTripleExtractor </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractConceptExtractor implements ConceptExtractor{
    protected ArrayList conceptList;
    protected boolean conceptFilter_enabled;
    protected boolean subconcept_enabled;
    protected ConceptFilter cf;
    protected DocumentParser parser;

    public AbstractConceptExtractor() {
        this.conceptFilter_enabled =false;
        this.subconcept_enabled =false;
        this.cf=null;
        this.conceptList=null;
        this.parser=new EngDocumentParser();
    }

    public void setSubConceptOption(boolean option){
        subconcept_enabled=option;
    }

    public boolean getSubConceptOption(){
        return subconcept_enabled;
    }

    public boolean getFilteringOption() {
        return conceptFilter_enabled;
    }

    public void setFilteringOption(boolean option) {
        conceptFilter_enabled = option;
    }

    public void setConceptFilter(ConceptFilter cf) {
        this.cf = cf;
        conceptFilter_enabled = (cf!=null);
    }

    public ConceptFilter getConceptFilter(){
        return cf;
    }

    public ArrayList getConceptList(){
        return conceptList;
    }


    public void print(PrintWriter out){
        if(conceptList==null || conceptList.size()==0)
            return;

        print(out,conceptList);
    }

    public void print(PrintWriter out, ArrayList conceptList){
        Concept t;
        int i;

        try {
            out.write(conceptList.size() + "\n");
            for (i = 0; i < conceptList.size(); i++) {
                t = (Concept) conceptList.get(i);
                out.write(t.getName() + "\t" + t.getFrequency() + "\n");
                out.flush();
            }
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public  SortedArray mergeConceptByEntryID(ArrayList termList) {
        return mergeTerms(termList, new ConceptEntryIDComparator());
    }

    public  SortedArray mergeConceptByName(ArrayList termList) {
        return mergeTerms(termList, new ConceptNameComparator());
    }

    private SortedArray mergeTerms(ArrayList termList, Comparator comparator) {
        SortedArray newList;
        Concept oldTerm, newTerm;
        int i, pos;

        newList=new SortedArray(comparator);
        for(i=0;i<termList.size();i++){
            oldTerm=(Concept)termList.get(i);
            pos=newList.binarySearch(oldTerm);
            if(pos>=0){
                newTerm=(Concept)newList.get(pos);
                newTerm.addFrequency(oldTerm.getFrequency());
            }
            else
            {
                pos=pos*(-1)-1;
                newTerm=oldTerm.copy();
                newTerm.setIndex(newList.size());
                newList.add(pos,newTerm);
            }
        }
        return newList;
    }

    public synchronized ArrayList extractFromDoc(String doc){
        return extractFromDoc(parser.parse(doc));
    }

    public synchronized ArrayList extractFromDoc(Document doc) {
        Sentence sent;
        Paragraph pg;
        ArrayList curTermList;
        try {
            conceptList = new ArrayList(20);
            if(doc==null)
                return conceptList;
            pg = doc.getFirstParagraph();
            while (pg != null) {
                sent = pg.getFirstSentence();
                while (sent != null) {
                    //make sure it is a non-empty sentence
                    if(sent.getFirstWord()!=null){
                        curTermList = extractFromSentence(sent);
                        if (curTermList != null) {
                            conceptList.addAll(curTermList);
                            curTermList.clear();
                        }
                    }
                    sent = sent.next;
                }
                pg = pg.next;
            }
            return conceptList;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DocumentParser getDocumentParser(){
        return parser;
    }

    public void setDocumentParser(DocumentParser parser){
        this.parser = parser;
    }
}