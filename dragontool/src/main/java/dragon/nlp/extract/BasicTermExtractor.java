package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.nlp.ontology.*;
import dragon.nlp.tool.*;
import java.util.*;

/**
 * <p>Ontological term extraction</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class BasicTermExtractor extends AbstractTermExtractor{

    public BasicTermExtractor(Ontology ontology, Lemmatiser lemmatiser, Tagger tagger) {
        super(ontology, tagger,lemmatiser);
    }

    public void clearAllCaches() {
        if (abbrChecker != null) {
            abbrChecker.clearCachedAbbr();
        }
    }

    public ArrayList extractFromSentence(Sentence sent) {
        ArrayList termList;
        Word cur; //start and end are the maixnum boundary of the term
        Term curTerm, prevTerm;

        cur = sent.getFirstWord();
        if(cur!=null && cur.getPOSIndex()<0) tagger.tag(sent);
        prevTerm = null;
        termList = new ArrayList(10);

        while (cur != null) {
            //search the word which could be the opening of a term.
            if (!ontology.isStartingWord(cur)) {
                cur = cur.next;
                continue;
            }

            curTerm = lookup(cur, prevTerm,termList);
            if (curTerm == null) {
                cur = cur.next;
            }
            else {
                cur = curTerm.getEndingWord().next;
                prevTerm = curTerm;
            }
        }

        if (coordinatingCheck_enabled) {
            paraChecker.identifyParaElements(sent);
        }

        if (attributeCheck_enabled) {
            attrChecker.identifyAttributes(termList);
        }

        if (coordinatingTermPredict_enabled && coordinatingCheck_enabled) {
            termList = paraChecker.parallelTermPredict(termList);
        }

        if (compoundTermPredict_enabled) {
            termList = compTermFinder.predict(termList);
        }

        if (conceptFilter_enabled) {
            termList = filter(termList);
        }

        return termList;
    }

    protected Term lookup(Word starting, Term prevTerm, ArrayList termList) {
        Term referredTerm, term;
        ArrayList list;
        try {
            if (abbreviation_enabled && starting.getContent().length() <= 6) {
                if (abbrChecker.contains(starting.getContent())) {
                    referredTerm = abbrChecker.get();
                }
                else if (abbrChecker.isAbbrOfLastTerm(starting, prevTerm)) {
                   abbrChecker.put(starting.getContent(), prevTerm);
                   referredTerm = prevTerm;
                }
                else
                    referredTerm=null;

                if (referredTerm!=null) {
                    starting.setLemma(starting.getContent().toLowerCase());
                    term = new Term(starting);
                    term.setCandidateCUI(referredTerm.getCandidateCUI());
                    term.setCUI(referredTerm.getCUI());
                    term.setCandidateTUI(referredTerm.getCandidateTUI());
                    term.setTUI(referredTerm.getTUI());
                    term.setReferral(referredTerm);
                    starting.setAssociatedConcept(term);
                    termList.add(term);
                    return term;
                }
            }

            if(subconcept_enabled){
                list=ontology.findAllTerms(starting);
                if(list!=null && list.size()>0){
                    termList.addAll(list);
                    term = (Term) list.get(0);
                }
                else
                    term=null;
            }
            else{
                term = ontology.findTerm(starting);
                if(term!=null) termList.add(term);
            }
            return term;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}