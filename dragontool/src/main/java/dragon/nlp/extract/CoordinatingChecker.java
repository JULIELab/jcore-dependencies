package dragon.nlp.extract;

import dragon.nlp.Sentence;
import dragon.nlp.Term;
import dragon.nlp.Word;
import dragon.nlp.tool.MedPostTagger;
import dragon.nlp.tool.Tagger;

import java.util.ArrayList;

/**
 * <p>Coordinating Component Identification</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel Univeristy</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CoordinatingChecker {
    private int threshold;
    private int minCommaNum;

    public CoordinatingChecker() {
        threshold=4;
        minCommaNum=2;
    }

    public static void main(String[] args){
        Sentence sent;
        Tagger tagger;
        CoordinatingChecker checker=new CoordinatingChecker();
        Word curWord;

        sent=(new EngDocumentParser()).parseSentence("Obesity and type 2 diabetes mellitus are associated with many metabolic disorders including insulin resistance, dyslipidemia, hypertension or atherosclerosis");
        tagger=new MedPostTagger(System.getProperty("user.dir"));
        tagger.tag(sent);
        checker.identifyParaElements(sent);
        curWord=sent.getFirstWord();
        while(curWord!=null)
        {
            System.out.print(curWord.getContent());
            System.out.print(" ");
            System.out.print(curWord.getParallelGroup());
            System.out.print("\r\n");
            curWord=curWord.next;
        }
    }

    public int identifyParaElements(Sentence sent){
        int groupNo,commaNum,offset;
        int firstComma,lastComma;
        Word cur;

        commaNum=0;
        groupNo=0;
        offset=0;
        firstComma=-1;
        lastComma=-1;
        cur=sent.getFirstWord();
        while(cur!=null){
            if(cur.isPunctuation() && cur.getContent().equalsIgnoreCase(","))
            {
                if(commaNum==0)
                {
                    commaNum=1;
                    firstComma=offset;
                    lastComma=offset;
                }
                else if(offset<=lastComma+threshold+1){
                    commaNum=commaNum+1;
                    lastComma=offset;
                }
                else if(commaNum<minCommaNum){
                    commaNum=1;
                    firstComma=offset;
                    lastComma=offset;
                }
                else //find a possible parallel group
                {
                    if(processParallelGroup(sent,groupNo,firstComma,lastComma,commaNum)){
                        groupNo=groupNo+1;
                        commaNum=1;
                        firstComma = offset;
                        lastComma = offset;
                    }
                }
            }
            offset=offset+1;
            cur=cur.next;
        }
        if(commaNum>=minCommaNum)
            if(processParallelGroup(sent,groupNo,firstComma,lastComma,commaNum))
                groupNo=groupNo+1;
        return groupNo;
    }

    private boolean processParallelGroup(Sentence sent, int groupNo,int firstComma, int lastComma, int commaNum){
        Word start, end, cur;
        int step,pos;

        //get starting word
        if(firstComma<=0) return false; //in some strange case, the first word may be comma.

        cur=sent.getWord(firstComma-1);
        step=0;
        while(cur!=null)
        {
            pos=cur.getPOSIndex();
            if(pos==Tagger.POS_VERB || pos==Tagger.POS_ADVERB || pos==Tagger.POS_IN || pos==0 || pos==Tagger.POS_CC ){
                break;
            }
            else{
                cur = cur.prev;
                step=step+1;
            }
        }
        if(step==0)
        {
            start = cur.next.next;
            commaNum=commaNum-1;
        }
        else if(cur==null)
            start=sent.getFirstWord();
        else
            start=cur.next;

        //get ending word
        cur=sent.getWord(lastComma+1);
        step=0;
        while(cur!=null)
        {
            pos=cur.getPOSIndex();
            if(pos==Tagger.POS_VERB || pos==Tagger.POS_ADVERB || pos==Tagger.POS_IN || pos==0 ||
               (pos==Tagger.POS_CC && !cur.getContent().equalsIgnoreCase("and")) && !cur.getContent().equalsIgnoreCase("or")){
                break;
            }
            else{
                cur = cur.next;
                step=step+1;
            }
        }
        if(step==0)
        {
            end = cur.prev.prev;
            commaNum=commaNum-1;
        }
        else if(cur==null)
            end=sent.getLastWord();
        else
            end=cur.prev;

        if(commaNum<minCommaNum) return false;

        cur=start;
        while(!cur.equals(end)){
            cur.setParallelGroup(groupNo);
            cur=cur.next;
        }
        end.setParallelGroup(groupNo);

        return true;
    }

    public ArrayList parallelTermPredict(ArrayList termList) {
        Term curTerm, newTerm;
        Word curWord, prevWord, endWord, startWord;
        int curParaGroup, insertPos;
        int i;

        for (i = 0; i < termList.size(); i++) {
            curTerm = (Term) termList.get(i);
            curParaGroup = curTerm.getStartingWord().getParallelGroup();
            if (curParaGroup < 0) {
                continue;
            }

            //go back
            curWord = curTerm.getStartingWord().prev;
            endWord = curWord;
            prevWord = curTerm.getStartingWord();
            insertPos = i;

            while (endWord != null && curWord != null && curWord.getParallelGroup() == curParaGroup &&
                   curWord.getAssociatedConcept() == null) {
                if (curWord.getContent().equalsIgnoreCase(",") || curWord.getContent().equalsIgnoreCase("and")) {
                    if (!curWord.equals(endWord)) { //find a new term
                        newTerm = new Term(prevWord, endWord);
                        newTerm.setPredictedTerm(true);
                        termList.add(insertPos, newTerm);
                        prevWord.setAssociatedConcept(newTerm);
                        i = i + 1;
                    }
                    endWord = curWord.prev;
                }
                prevWord = curWord;
                curWord = curWord.prev;
            }
            if (curWord == null || curWord.getParallelGroup() != curParaGroup) {
                if (endWord != null && prevWord.getPosInSentence() <= endWord.getPosInSentence()) {
                    newTerm = new Term(prevWord, endWord);
                    newTerm.setPredictedTerm(true);
                    termList.add(insertPos, newTerm);
                    prevWord.setAssociatedConcept(newTerm);
                    i = i + 1;
                }
            }

            //go forth
            curWord = curTerm.getStartingWord().next;
            startWord = curWord;
            prevWord = curTerm.getStartingWord();
            insertPos = i + 1;

            while (startWord != null && curWord != null && curWord.getParallelGroup() == curParaGroup &&
                   curWord.getAssociatedConcept() == null) {
                if (curWord.getContent().equalsIgnoreCase(",") || curWord.getContent().equalsIgnoreCase("and")) {
                    if (!curWord.equals(startWord)) { //find a new term
                        newTerm = new Term(startWord, prevWord);
                        newTerm.setPredictedTerm(true);
                        termList.add(insertPos, newTerm);
                        prevWord.setAssociatedConcept(newTerm);
                        i = i + 1;
                    }
                    startWord = curWord.next;
                }
                prevWord = curWord;
                curWord = curWord.next;
            }
            if (curWord == null || curWord.getParallelGroup() != curParaGroup) {
                if (startWord != null && prevWord.getPosInSentence() >= startWord.getPosInSentence()) {
                    newTerm = new Term(startWord, prevWord);
                    newTerm.setPredictedTerm(true);
                    termList.add(insertPos, newTerm);
                    prevWord.setAssociatedConcept(newTerm);
                    i = i + 1;
                }
            }
        }
        return termList;
    }
}