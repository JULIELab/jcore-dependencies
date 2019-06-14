package dragon.nlp.extract;

import dragon.nlp.Term;
import dragon.nlp.Word;
import dragon.nlp.tool.Tagger;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.util.ArrayList;

/**
 * <p>Checking whetherterms are of attribute type</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AttributeChecker {
    private SortedArray attributes;

    public AttributeChecker(String attributeFile) {
        loadAttributes(attributeFile);
    }

    public boolean loadAttributes(String filename){
        String content;
        String[] arrTerms;
        int i, termNum;

        try {
            content = FileUtil.readTextFile(filename);
            arrTerms = content.split("\r\n");
            termNum = arrTerms.length;
            while (termNum > 0 && arrTerms[termNum - 1].trim().length() == 0)
                termNum = termNum - 1;
            attributes=new SortedArray(termNum);
            for (i = 0; i < termNum; i++)
                attributes.add(i, arrTerms[i].toLowerCase());

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean isAttribute(Term attr){
        return attributes.contains(attr.toLemmaString());
    }

    public boolean isAttribute(String attr){
        return attributes.contains(attr.toLowerCase());
    }

    public int identifyAttributes(ArrayList termList){
        Term curTerm, nextTerm,term;
        Word word;
        int i,j, groupNo;
        int count;

        if(termList==null) return 0;
        if(termList.size()<2) return 0;

        count=0;
        curTerm=(Term)termList.get(0);
        for(i=1;i<termList.size();i++){
            word=curTerm.getEndingWord().next ;
            nextTerm=(Term)termList.get(i);
            //if(word.getContent().equalsIgnoreCase("of"))
            if(word.getPOSIndex()==Tagger.POS_IN && "of for".indexOf(word.getContent().toLowerCase())>=0) //modified on 9/5/2005
            {
                if(nextTerm.getStartingWord().getPosInSentence()-curTerm.getEndingWord().getPosInSentence()<=4 && isAttribute(curTerm));
                {
                    curTerm.getStartingWord().setAssociatedConcept(null);
                    nextTerm.addAttribute(curTerm);
                    count=count+1;

                    //remove the attribute from term list
                    termList.remove(i-1);
                    i=i-1;

                    //find parallel structures:
                    //the risks of hypertension, obesity, and diabetes.
                    //the height, width, and the color of the building (this pattern is not implemented in this version)
                    groupNo=nextTerm.getEndingWord().getParallelGroup();
                    if(groupNo>curTerm.getEndingWord().getParallelGroup()){
                        for(j=i+1;j<termList.size();j++)
                        {
                            term=(Term)termList.get(j);
                            if(term.getEndingWord().getParallelGroup()!=groupNo)
                                break;
                            else
                            {
                                term.addAttribute(curTerm);
                            }
                        }
                        i=j-1;
                    }
                    curTerm=(Term)termList.get(i);
                }
            }
            else
            {
                curTerm = nextTerm;
            }
        }
        return count;
    }
}