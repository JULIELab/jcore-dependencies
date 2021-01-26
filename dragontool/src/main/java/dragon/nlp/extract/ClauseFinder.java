package dragon.nlp.extract;

import dragon.nlp.Sentence;
import dragon.nlp.Word;
import dragon.nlp.tool.HeppleTagger;
import dragon.nlp.tool.MedPostTagger;
import dragon.nlp.tool.Tagger;
/**
 * <p>Identify clauses in an English sentence</p>
 * <p>It is used to identify and mark all cluases in a sentence. this module should be executed after the
 * parallel structure module.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ClauseFinder {
    String[] arrConj;
    int conjNum;
    Sentence sent;

    public static void main(String[] args){
        Sentence sent;
        Tagger tagger;
        ClauseFinder finder=new ClauseFinder();
        Word curWord;

        sent=(new EngDocumentParser()).parseSentence("I like him");
        tagger=new MedPostTagger(System.getProperty("user.dir"));
        tagger.tag(sent);
        finder.clauseIdentify(sent);
        curWord=sent.getFirstWord();
        while(curWord!=null)
        {
            System.out.print(curWord.getContent());
            System.out.print(" ");
            System.out.print(curWord.getClauseID());
            System.out.print("\r\n");
            curWord=curWord.next;
        }
    }


    public ClauseFinder() {
        conjNum=9;
        arrConj=new String[conjNum];
        arrConj[0]="although";
        arrConj[1]="because";
        arrConj[2]="but";
        arrConj[3]="if";
        arrConj[4]="that";
        arrConj[5]="though";
        arrConj[6]="when";
        arrConj[7]="whether";
        arrConj[8]="while";
    }

    public int clauseIdentify(Sentence sent){
        int clauseID;
        int pos;
        Word openner,cur;
        boolean newClause;

        clauseID=0;
        openner=sent.getFirstWord();
        cur=sent.getFirstWord();
        while(cur!=null)
        {
            newClause=false;
            pos=cur.getPOSIndex();
            if(pos==Tagger.POS_CC) //conjunction
            {
                newClause = processConjunction(cur);
                if(newClause) openner=cur;
            }
            else if(cur.isPunctuation() && cur.getContent().equalsIgnoreCase(",")) //process comma
            {
                newClause = processComma(cur,openner);
                if(newClause) openner=null;
            }
            else if(pos==0 || pos>=HeppleTagger.POS_ADVERB){
                if(cur.getContent().equalsIgnoreCase("that"))
                    newClause=processThat(cur);
            }

            if(newClause)
                clauseID=clauseID+1;
            cur.setClauseID(clauseID);
            cur=cur.next;
        }
        return clauseID+1;
    }

    private boolean processConjunction(Word current){
        int index;
        if(current.prev==null || current.next==null) return false;

        index=isConjunctionMarker(current.getContent());
        if(index<0) return false;

        return true;
    }

    private boolean processComma(Word current,Word openner){
        int pos;

        if(current.prev==null || current.next==null) return false;
        // comma appears in a number
        if(current.prev.getPOSIndex()==Tagger.POS_NUM || current.next.getPOSIndex()==Tagger.POS_NUM)
            return false;
        //the comma is the marker of parallel structure instead of the marker of new clause
        if(current.getParallelGroup()>=0) return false;
        if(openner==null) return false;
        pos=openner.getPOSIndex();
        if(pos==Tagger.POS_IN || pos==Tagger.POS_CC || pos==Tagger.POS_ADVERB)
            return true;
        else
            return false;
    }

    private boolean processThat(Word current){
        int prevPos;

        if(current.prev==null || current.next==null) return false;

        prevPos=current.prev.getPOSIndex();
        if(prevPos==Tagger.POS_VERB || prevPos==Tagger.POS_CC || prevPos==Tagger.POS_ADVERB)
            return true;
        if(prevPos==Tagger.POS_NOUN) //relative clause
        {
            current.setAssociatedConcept(current.prev.getAssociatedConcept()); //Co-reference Resolution
            return true;
        }
        return false;
    }

    private int isConjunctionMarker(String word){
        int low, middle, high;
        int result;

        low=0;
        high=conjNum-1;
        while(low<=high)
        {
            middle=(low+high)/2;
            result=arrConj[middle].compareToIgnoreCase(word);
            if(result==0) return middle;
            else if(result>0)
                high=middle-1;
            else
                low=middle+1;
        }
        return -1;
    }

}