package dragon.nlp.ontology;

import dragon.nlp.*;
import dragon.nlp.tool.*;
import dragon.util.*;
import java.util.ArrayList;

/**
 * <p>Basic Ontology can be called directly for basic operations of a given ontology</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicOntology extends AbstractOntology{
    private BasicTermList list;

    public BasicOntology(String termFilename, Lemmatiser lemmatiser) {
        super(lemmatiser);
        if(!FileUtil.exist(termFilename) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+termFilename))
                termFilename=EnvVariable.getDragonHome()+"/"+termFilename;
        setNonBoundaryPunctuation(".-");
        list=new BasicTermList(termFilename);
    }

    public String[] getSemanticType(String[] cuis){
        return null;
    }

    public String[] getSemanticType(String cui){
        return null;
    }

    public String[] getCUI(String term){
        BasicTerm cur;

        cur=list.lookup(term);
        if(cur==null)
            return null;
        else
            return cur.getAllCUI();
    }

    public String[] getCUI(Word starting, Word ending){
        return getCUI(buildString(starting,ending,getLemmaOption()));
    }

    public boolean isTerm(String term){
        return true;
    }

    public boolean isTerm(Word starting, Word ending){
        return isTerm(buildString(starting,ending,getLemmaOption()));
    }

    public Term findTerm(Word starting){
        return findTerm(starting,null);
    }

    public Term findTerm(Word start, Word end) {
        Sentence sent;
        Word curWord;
        String[] arrCandidateCUI;
        Term curTerm;
        int j, posIndex;

        sent = start.getParent();
        //set the right bounary of the possible term.
        curWord = start.next;
        if (end == null) {
            j = 0;
            while (j < 4 && curWord != null && end == null) {
                if (isBoundaryWord(curWord))
                    end = curWord.prev;
                j++;
                curWord = curWord.next;
            }
            if (curWord == null)
                curWord = sent.getLastWord();
            if (end == null)
                end = curWord;
        }

        curWord = end;
        arrCandidateCUI=null;
        while (curWord!=null && curWord.getPosInSentence()>=start.getPosInSentence()) {
            posIndex = curWord.getPOSIndex();
            if ( (posIndex == Tagger.POS_NOUN || posIndex == Tagger.POS_NUM && curWord.getPosInSentence() > start.getPosInSentence())) {
                arrCandidateCUI = getCUI(start, curWord);
                if (arrCandidateCUI != null)
                    break;
            }
            curWord=curWord.prev;
        }

        if(arrCandidateCUI==null) return null;
        curTerm = new Term(start, curWord);
        start.setAssociatedConcept(curTerm);
        curTerm.setCandidateCUI(arrCandidateCUI);
        if(arrCandidateCUI.length==1)
            curTerm.setCUI(arrCandidateCUI[0]);
        return curTerm;
    }

    public ArrayList findAllTerms(Word starting){
        return findAllTerms(starting, null);
    }

    public ArrayList findAllTerms(Word starting, Word ending){
        Term cur;
        ArrayList termList;

        cur=findTerm(starting,ending);
        if(cur==null)
            return null;
        else{
            termList=new ArrayList(1);
            termList.add(cur);
            return termList;
        }
    }
 }