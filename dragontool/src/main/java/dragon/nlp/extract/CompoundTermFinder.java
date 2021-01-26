package dragon.nlp.extract;

import dragon.nlp.Term;
import dragon.nlp.Word;
import dragon.nlp.tool.Tagger;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * <p>Finding compond terms</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CompoundTermFinder {
    private SortedArray suffixList;
    private boolean subterm_enabled;

    public CompoundTermFinder() {
        suffixList=null;
        subterm_enabled=false;
    }

    public CompoundTermFinder(String suffixFile) {
        suffixList=loadlist(suffixFile);
        subterm_enabled=false;
    }

    public void setSubTermOption(boolean option){
        subterm_enabled=option;
    }

    public boolean getSubTermOption(){
        return subterm_enabled;
    }

    protected ArrayList predict(ArrayList termList) {
        ArrayList newList;
        Word startingWord, endingWord;
        int start, end, i;
        Term term;

        if (termList.size()<=1) return termList;

        newList=new ArrayList(termList.size());
        start=0;
        while(start<termList.size()){
            end=searchEndTerm(termList,start);

            if(suffixList!=null && end>start){
                term=(Term)termList.get(end);
                while(end>start && term.getWordNum()==1 && !suffixList.contains(term.toLemmaString())){
                    end = end - 1;
                    term=(Term)termList.get(end);
                }
            }

            if(end==start){
                newList.add(termList.get(start));
            }
            else
            {
                startingWord=((Term)termList.get(start)).getStartingWord();
                endingWord=((Term)termList.get(end)).getEndingWord();
                term = new Term(startingWord, endingWord);
                term.setPredictedTerm(true);
                term.getStartingWord().setAssociatedConcept(term);
                newList.add(term);

                for(i=start;i<=end;i++){
                    term=(Term)termList.get(i);
                    if(i>start) term.getStartingWord().setAssociatedConcept(null);
                    term.setSubConcept(true);
                    if(subterm_enabled) newList.add(term);
                }
            }
            start=getNextNonSubTerm(termList,end+1);
        }
        return newList;
    }

    private  int searchEndTerm(ArrayList termList, int start){
        Term curTerm, nextTerm;
        Word nextWord;
        int posIndex, end, skippedWords;

        curTerm = (Term) termList.get(start);
        end = start;
        start=getNextNonSubTerm(termList,start+1);
        nextWord = curTerm.getEndingWord().next;
        skippedWords=0;

        while(start<termList.size() && nextWord!=null){
            nextTerm = (Term) termList.get(start);

            //Term1 Term2
            if (nextTerm.getStartingWord().equals(nextWord)) {
                end = start;
                curTerm=nextTerm;
                start=getNextNonSubTerm(termList,start+1);
                skippedWords=0;
                nextWord=curTerm.getEndingWord().next;
            }
            else  //Term1 Word (ADJ or NN) Term2
            {
                if(skippedWords>=1) return end;

                posIndex = nextWord.getPOSIndex();
                if (posIndex == Tagger.POS_ADJECTIVE || posIndex == Tagger.POS_NOUN) {
                    skippedWords++;
                    nextWord=nextWord.next;
                }
                else if(posIndex==0 && ".-".indexOf(nextWord.getContent())>=0){
                    nextWord=nextWord.next;
                }
                else
                    return end;
            }
        }
        return end;
    }

    private int getNextNonSubTerm(ArrayList list, int start){
        int i;

        i=start;
        while(i<list.size()){
            if(((Term)list.get(i)).isSubConcept())
                i++;
            else
                break;
        }
        return i;
    }

    private SortedArray loadlist(String filename) {
        SortedArray list;
        BufferedReader br;
        String line;
        int i, total, pos;

        try {
            if (filename == null || filename.trim().length() == 0) {
                return null;
            }
            br = FileUtil.getTextReader(filename);
            line = br.readLine();
            total = Integer.parseInt(line);
            list = new SortedArray(total);

            for (i = 0; i < total; i++) {
                line = br.readLine();
                pos=line.indexOf('\t');
                if(pos>0)
                    line=line.substring(0,pos);
                list.add(line.trim());
            }
            br.close();
            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}