package dragon.nlp.ontology.umls;

import dragon.nlp.Sentence;
import dragon.nlp.Term;
import dragon.nlp.Word;
import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.Tagger;
import dragon.util.SortedArray;

import java.util.ArrayList;
import java.util.Collections;
/**
 * <p>UMLS ontology for extracting UMLS defined medical concepts (CUIs) from text </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class UmlsExactOntology extends UmlsOntology{
    public UmlsExactOntology(Lemmatiser lemmatiser) {
        super(lemmatiser);
    }

    public String[] getSemanticType(String[] cuis){
        SortedArray typeList;
        String[] arrTypes;
        int i,j;

        typeList=new SortedArray(3);
        for(i=0;i<cuis.length;i++)
        {
            arrTypes=getSemanticType(cuis[i]);
            if(arrTypes!=null){
                for(j=0;j<arrTypes.length;j++)
                    typeList.add(arrTypes[j]);
            }
        }
        if(typeList.size()>0){
            arrTypes=new String[typeList.size()];
            for(i=0;i<typeList.size();i++)
                arrTypes[i]=(String)typeList.get(i);
            return arrTypes;
        }
        else
            return null;
    }

    public abstract String[] getSemanticType(String cui);

    public abstract String[] getCUI(String term);

    public String[] getCUI(Word starting, Word ending){
        return getCUI(buildNormalizedTerm(starting,ending));
    }

    public abstract boolean isTerm(String term);

    public boolean isTerm(Word starting, Word ending){
        return isTerm(buildNormalizedTerm(starting, ending));
    }

    public ArrayList findAllTerms(Word start){
        return findAllTerms(start,null);
    }

    public ArrayList findAllTerms(Word start, Word end){
        return null;
    }

    public Term findTerm(Word start){
        return findTerm(start,null);
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
                if (!curWord.isPunctuation())
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
        curTerm = new Term(start, end);
        start.setAssociatedConcept(curTerm);
        curTerm.setCandidateCUI(arrCandidateCUI);
        if(arrCandidateCUI.length==1)
            curTerm.setCUI(arrCandidateCUI[0]);
        if (curTerm.getCUI() == null) {
            curTerm.setCandidateTUI(getSemanticType(curTerm.getCandidateCUI()));
        }
        else {
            curTerm.setCandidateTUI(getSemanticType(curTerm.getCUI()));
        }
        if (curTerm.getCandidateTUINum() == 1) {
            curTerm.setTUI(curTerm.getCandidateTUI(0));
        }
        return curTerm;
    }

    public String buildNormalizedTerm(Word start,Word end)
    {
        Word next;
        ArrayList list;
        StringBuffer term;
        int i;

        if (start.equals(end)) return getLemma(start);

        list = new ArrayList(6);
        next = start;
        while(next!=null) {
            if(isUsefulForTerm(next)){
                list.add(getLemma(next));
            }
            if(!next.equals(end))
                next = next.next;
            else
                break;
        }
        Collections.sort(list);

        term=new StringBuffer((String)list.get(0));
        for(i=1;i<list.size();i++) {
            term.append(' ');
            term.append((String)list.get(i));
        }
        return term.toString();
    }

}