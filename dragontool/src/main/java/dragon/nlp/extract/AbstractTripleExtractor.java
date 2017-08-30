package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.util.*;
import java.io.*;
import java.util.ArrayList;

/**
 * <p>Abstract class for triple extraction</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractTripleExtractor implements TripleExtractor{
    protected ConceptExtractor conceptExtractor;
    protected ConceptFilter cf;
    protected CoReference coReference;
    protected ClauseFinder clauseFinder;
    protected CoordinatingChecker coordinatingChecker;
    protected ArrayList conceptList, tripleList;
    protected boolean coReference_enabled;
    protected boolean semanticCheck_enabled;
    protected boolean relationCheck_enabled;
    protected boolean clauseIdentify_enabled;
    protected boolean coordinatingCheck_enabled;
    protected boolean conceptFilter_enabled;

    public AbstractTripleExtractor(ConceptExtractor te) {
        conceptExtractor=te;
        coReference_enabled=false;
        relationCheck_enabled=false;
        clauseIdentify_enabled=false;
        semanticCheck_enabled=true;
        coReference=new CoReference();
        clauseFinder=new ClauseFinder();
        coordinatingChecker=new CoordinatingChecker();
    }

    public void initDocExtraction(){
        conceptExtractor.initDocExtraction();
    }

    public ArrayList getConceptList(){
        return conceptList;
    }

    public ArrayList getTripleList(){
        return tripleList;
    }

    public ConceptExtractor getConceptExtractor(){
        return conceptExtractor;
    }

    public boolean getFilteringOption() {
        return conceptFilter_enabled;
    }

    public void setFilteringOption(boolean option) {
        conceptFilter_enabled = option;
    }

    public void setConceptFilter(ConceptFilter cf) {
        this.cf = cf;
        conceptFilter_enabled = (cf != null);
    }

    public ConceptFilter getConceptFilter() {
        return cf;
    }

    public void setCoordinatingCheckOption(boolean option){
        coordinatingCheck_enabled = option;
    }

    public boolean getCoordinatingCheckOption() {
        return coordinatingCheck_enabled;
    }

    public void setCoReferenceOption(boolean option){
        coReference_enabled = option;
    }

    public boolean getCoReferenceOption() {
        return coReference_enabled;
    }

    public boolean getSemanticCheckOption(){
        return semanticCheck_enabled;
    }

    public void setSemanticCheckOption(boolean option) {
        semanticCheck_enabled=option;
    }

    public boolean getRelationCheckOption() {
        return relationCheck_enabled;
    }

    public void setRelationCheckOption(boolean option){
        relationCheck_enabled = option;
    }

    public boolean getClauseIdentifyOption() {
        return clauseIdentify_enabled;
    }

    public void setClauseIdentifyOption(boolean option){
        clauseIdentify_enabled = option;
    }

    public void print(PrintWriter out){
        print(out, conceptList, tripleList);
    }

   public void print(PrintWriter out,ArrayList conceptList,ArrayList tripleList){
       Concept concept;
       Triple triple;
       int i;

       try {
           for (i = 0; i < conceptList.size(); i++) {
               concept=(Concept)conceptList.get(i);
               out.write(concept.getName()+": ");
               out.write(String.valueOf(concept.getFrequency()));
               if(concept.getEntryID()!=null){
                   out.write(", "+concept.getEntryID());
               }
               if(concept.getSemanticType()!=null){
                   out.write(", "+concept.getSemanticType());
               }
               out.write("\n");
           }

           for (i = 0; i < tripleList.size(); i++) {
               triple = (Triple) tripleList.get(i);
               out.write(triple.getFirstConcept().getName());
               out.write("<->");
               out.write(triple.getSecondConcept().getName());
               out.write('(');
               out.write(String.valueOf(triple.getFrequency()));
               out.write(')');
               out.write("\n");
           }
           out.flush();
       }
       catch (Exception e) {
           e.printStackTrace();
       }
   }

   public boolean extractFromDoc(String doc){
       return extractFromDoc(conceptExtractor.getDocumentParser().parse(doc));
   }

   public boolean extractFromDoc(Document doc){
        Sentence sent;
        Paragraph pg;
        ArrayList curTermList, curTripleList;

        conceptList=new ArrayList();
        tripleList=new ArrayList();

        //conceptExtractor.initDocExtraction();
        pg=doc.getFirstParagraph();
        while(pg!=null)
        {
            sent = pg.getFirstSentence();
            while (sent != null)
            {
                //make sure it is a non-empty sentence
                if(sent.getFirstWord()!=null){
                    curTermList = conceptExtractor.extractFromSentence(sent);
                    if (curTermList != null) {
                        conceptList.addAll(curTermList);
                        curTripleList = extractFromSentence(sent);
                        if (curTripleList != null) {
                            tripleList.addAll(curTripleList);
                            curTripleList.clear();
                        }
                        curTermList.clear();
                    }
                }
                sent = sent.next;
            }
            pg=pg.next;
        }
        return true;
    }

    public SortedArray mergeTriples(SortedArray mergedConceptList, ArrayList tripleList) {
        SortedArray newList;
        Triple triple, newTriple;
        int left, right, i;

        newList = new SortedArray();
        for (i = 0; i < tripleList.size(); i++) {
            triple = (Triple) tripleList.get(i);
            left = mergedConceptList.binarySearch(triple.getFirstConcept());
            if (left >= 0) {
                right = mergedConceptList.binarySearch(triple.getSecondConcept());
                if (right >= 0) {
                    if (mergedConceptList.getComparator().compare(mergedConceptList.get(left), mergedConceptList.get(right)) > 0)
                        newTriple = new Triple( (Concept) mergedConceptList.get(right), (Concept) mergedConceptList.get(left));
                    else
                        newTriple = new Triple( (Concept) mergedConceptList.get(left), (Concept) mergedConceptList.get(right));
                    if (!newList.add(newTriple)) {
                        ( (Triple) newList.get(newList.insertedPos())).addFrequency(triple.getFrequency());
                    }
                    else {
                        newTriple.setFrequency(triple.getFrequency());
                        newTriple.setTUI(triple.getTUI());
                        newTriple.setCandidateTUI(triple.getCandidateTUI());
                        newTriple.setIndex(newList.size() - 1);
                    }
                }
            }
        }
        return newList;
    }

    protected boolean checkCoordinateTerms(Concept first, Concept second){
        Word cur;
        int gap;

        cur=first.getEndingWord().next;
        if(cur==null || !cur.getContent().equalsIgnoreCase("and")) return false;
        gap=second.getStartingWord().getPosInSentence()-cur.getPosInSentence();
        if(gap<1 || gap>2) return false;
        cur=first.getStartingWord().prev;
        if(cur!=null)
        {
            if(cur.getContent().equalsIgnoreCase("of")||cur.getContent().equalsIgnoreCase("between"))
                return false;
            else{
                cur=cur.prev;
                if(cur!=null && (cur.getContent().equalsIgnoreCase("of")||cur.getContent().equalsIgnoreCase("between")))
                    return false;
            }
        }
        return true;
    }
}