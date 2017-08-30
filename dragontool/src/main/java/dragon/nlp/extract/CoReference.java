package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.nlp.tool.*;
/**
 * <p>Find out co-reference within one sentence or consecutive sentences</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CoReference {
    public CoReference() {
    }

    public int execute(Sentence sent){
        Word cur;
        int count;
        boolean flag;
        String word;

        count=0;
        cur=sent.getFirstWord();
        while(cur!=null)
        {
            flag=false;
            if(cur.getPOSIndex()==Tagger.POS_PRONOUN || cur.getContent().equalsIgnoreCase("such")){
                word=cur.getContent();
                if(word.equalsIgnoreCase("it"))
                    flag=solveIt(cur,sent);
                else if(word.equalsIgnoreCase("such"))
                    flag=solveIt(cur,sent);
            }
            if(flag) count=count+1;
            cur=cur.next;
        }
        return count;
    }

    private boolean solveIt(Word target, Sentence sent)
    {
        return false;
    }
}