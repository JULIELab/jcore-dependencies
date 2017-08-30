package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.nlp.ontology.*;
import dragon.nlp.tool.*;
import dragon.util.*;
import java.util.*;
import java.io.PrintWriter;

/**
 * <p>Triple Extraction</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicTripleExtractor extends AbstractTripleExtractor{
    private SemanticNet snNet;

    public BasicTripleExtractor(TermExtractor te) {
        super(te);
        snNet=te.getOntology().getSemanticNet();
    }

    public void extractTripleFromFile(String filename){
        String workDir;
        PrintWriter out, mergedOut;
        SortedArray mergedTermList;

        try {
            workDir = System.getProperty("user.dir");
            extractFromDoc(FileUtil.readTextFile(workDir + "/inout/" + filename));
            out = FileUtil.getPrintWriter(workDir + "/inout/" + filename + ".triple");
            print(out,conceptList,tripleList);
            out.close();
            mergedOut = FileUtil.getPrintWriter(workDir + "/inout/" + filename + ".mergedtriple");
            mergedTermList=conceptExtractor.mergeConceptByEntryID(conceptList);
            print(mergedOut,mergedTermList,mergeTriples(mergedTermList,tripleList));
            mergedOut.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(PrintWriter out,ArrayList termList,ArrayList tripleList){
       Term term;
       Triple triple;
       int i, j;
       String[] arrStr;

       try {
           for (i = 0; i < termList.size(); i++) {
               term=(Term)termList.get(i);
               out.write(term.toString());
               for(j=0;j<term.getAttributeNum();j++)
               {
                   out.write('/');
                   out.write(term.getAttribute(j).toString());
               }
               out.write('(');
               out.write(String.valueOf(term.getFrequency()));
               out.write(')');
               arrStr=term.getCandidateTUI();
               if(arrStr!=null){
                   out.write(": ");
                   for (j = 0; j < arrStr.length; j++) {
                       out.write(arrStr[j]);
                       if (j == arrStr.length - 1)
                           out.write(" (");
                       else
                           out.write(';');
                   }
                   arrStr = term.getCandidateCUI();
                   for (j = 0; j < arrStr.length; j++) {
                       out.write(arrStr[j]);
                       if(term.getCUI()!=null && term.getCUI().equalsIgnoreCase(arrStr[j]))
                           out.write("*");
                       if (j == arrStr.length - 1)
                           out.write(')');
                       else
                           out.write(',');
                   }
               }
               if(term.isPredicted()) //predicted term
               {
                   out.write("(Predicted)");
               }
               out.write("\r\n");
           }

           for (i = 0; i < tripleList.size(); i++) {
               triple = (Triple) tripleList.get(i);
               arrStr=triple.getCandidateTUI();
               out.write(triple.getFirstConcept().toString());
               out.write("<->");
               out.write(triple.getSecondConcept().toString());
               out.write('(');
               out.write(String.valueOf(triple.getFrequency()));
               out.write(')');
               if(arrStr!=null){
                   out.write(": ");
                   for (j = 0; j < arrStr.length; j++) {
                       out.write(arrStr[j]);
                       //out.write(arrStr[j]);
                       if (j < arrStr.length - 1)
                           out.write(';');
                   }
               }
               out.write("\r\n");
           }
           out.flush();
       }
       catch (Exception e) {
           e.printStackTrace();
       }
   }


    /*The following function extracts triples stored in a arraylist.
    Before function call, the inputing sentence should be POS-tagged, lemmatised,
    and term-annotated. */
    public ArrayList extractFromSentence(Sentence sent){
        Word first, second, next;
        Term term;
        int firstGroup, secondGroup;
        Triple triple;
        ArrayList list;
        Boolean trueObj, falseObj;

        //clause identification
        if(clauseIdentify_enabled) clauseFinder.clauseIdentify(sent);

        //identify terms that should be filtered out for relation building
        trueObj=new Boolean(true);
        falseObj=new Boolean(false);
        next = sent.getFirstWord();
        while (next != null) {
            term=(Term)next.getAssociatedConcept();
            if(term!=null){
                if(conceptFilter_enabled && cf!=null){
                    if (cf.keep(term))
                        term.setMemo(trueObj);
                    else
                        term.setMemo(falseObj);
                }
                else
                    term.setMemo(trueObj);
                //adj term is excluded for relation building
                if(term.getWordNum()==1 && term.getStartingWord().getPOSIndex()==Tagger.POS_ADJECTIVE)
                    term.setMemo(falseObj);
            }
            next=next.next;
        }

        list=new ArrayList();
        first=sent.getFirstWord();
        while(first!=null)
        {
            if(first.getAssociatedConcept()==null || first.getAssociatedConcept().getMemo().equals(falseObj)){
                first=first.next;
                continue;
            }
            firstGroup=first.getParallelGroup();
            second=first.next;
            while (second != null ) {
                if (second.getAssociatedConcept() == null || second.getAssociatedConcept().getMemo().equals(falseObj)){
                    second=second.next;
                    continue;
                }

                if (second.getAssociatedConcept().equalTo(first.getAssociatedConcept()) ){
                    break;
                }
                secondGroup=second.getParallelGroup();
                if (coordinatingCheck_enabled){
                    if(secondGroup != -1 && firstGroup==secondGroup){
                        second = second.next;
                        continue;
                    }
                    else if(checkCoordinateTerms((Term)first.getAssociatedConcept(),(Term)second.getAssociatedConcept()))
                    {
                        second = second.next;
                        continue;
                    }
                }
                if(clauseIdentify_enabled && first.getClauseID()!=second.getClauseID()){
                    second=second.next;
                    continue;
                }

                triple = lookup((Term)first.getAssociatedConcept(), (Term)second.getAssociatedConcept());
                if (triple != null)
                    list.add(triple);
                second = second.next;
            }
            first=first.next;
        }
        return list;
    }

    private Triple lookup(Term first,Term second){
        String[] rel;
        boolean found;
        Triple triple;

        try {
            if(!semanticCheck_enabled)
            {
                return new Triple(first,second);
            }

            if(first.getCandidateTUI()==null || second.getCandidateTUI()==null)
            {
                return new Triple(first,second);
            }

            if(relationCheck_enabled){
                if(first.getTUI()!=null && second.getTUI()!=null)
                    rel=snNet.getRelations(first.getTUI(), second.getTUI());
                else
                    rel =snNet.getRelations(first.getCandidateTUI(), second.getCandidateTUI());
                if (rel != null) {
                    triple = new Triple(first, second);
                    triple.setCandidateTUI(rel);
                    return triple;
                }
                else
                    return null;
            }
            else
            {
                if(first.getTUI()!=null && second.getTUI()!=null)
                    found=snNet.isSemanticRelated(first.getTUI(), second.getTUI());
                else
                    found=snNet.isSemanticRelated(first.getCandidateTUI(), second.getCandidateTUI());
                if (found) {
                    triple = new Triple(first, second);
                    return triple;
                }
                else
                    return null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
   }
}