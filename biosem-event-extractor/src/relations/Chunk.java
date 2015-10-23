/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package relations;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chinh
 */
public class Chunk {
    public String type=null ; //NP ; VP ; PP ; SBAR ; CC ...
    public String txt=null;
    public List<Word> pros = new ArrayList<Word>(); // Protein list
    public List<Word> trigs = new ArrayList<Word>(); // Trigger list
    public int begin =0; // position of begin chunk
    public int end =0 ;// end chunk
    public boolean is_merged =false ; // if true then all trigger has the same role; can not used for in-chunk case
    public List<Word> cause = new ArrayList<Word>(); // Protein list
    public Chunk(String type){
        this.type = type ;
    }      
    public void addWord(String w){
        if(txt==null){
            txt =w;
        }else{
            txt=txt+" "+w;
        }
    }
   
/**
     * Add protein
     */       
    public void addPro(Word pr){
        pros.add(pr);
    }
    
    /**
     * Add trigger
     * @param trg 
     */
       
    public void addTrigger(Word trg){
        trigs.add(trg);
    }
    
    public String getType(){
        return type ;
    }
    
    public String getText(){
        return txt ;
    }
    
    /**
     * Merge two chunks: appending a chunk to the current chunk
     * @param c :chunk
     * 
     */
    public void merge(Chunk c){
        end = c.end ;
        pros.addAll(c.pros);
        trigs.addAll(c.trigs);
        txt += " "+c.txt ;
    }
    
    public void removeTRG(Word tg){
        trigs.remove(tg);
    }
    
    public void removePRO(Word pro){
        pros.remove(pro);
    }
    
    public void removePro(int pos){
        Word pr=null;
        for(Word w: pros){
            if(w.pos==pos){
                pr =w ;
                break;
            }
        }
        if(pr!=null){
            pros.remove(pr);
            cause.add(pr); // put to cause list
        }
    }
    
    
    public boolean isQualify(){
        return pros.size()>0 && trigs.size()>0;
    }
    public boolean contains(Word key){
        if(key.pos>= begin && key.pos<=end){
            return true ;
        }
        return false ;
    }
    
    /**
     * Check two trg in the same chunk, without any pro in between of them
     * @param tg1
     * @param tg2
     * @return : true if there is no pro between two triggers
     */
    public boolean inChunkTG(Word tg1, Word tg2){
        if(contains(tg1)&& contains(tg2)){
            int pos1 = Math.min(tg1.pos, tg2.pos);
            int pos2 = Math.max(tg1.pos, tg2.pos);
            for(Word pr: pros){
                if(pr.pos>pos1 && pr.pos < pos2){
                    return false ;
                }
            }
            if(pos1!=pos2){
                return true ;
            }
        }
        return false ;
    }
    
    /**
     * Check TG and Pro in the same chunk, for use with learning rules
     * @param trg
     * @param pro
     * @return 
     */
    public boolean inChunk(Word trg, Word pro) {
        if (contains(trg) && contains(pro) && pro.pos <= trg.pos) {
            if (!pro.combined && !trg.combined) { // TODO: check whether there is any trigger between pro and trg
                return true;
            } else if (pro.pos == trg.pos) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check TG and Pro in the same chunk, for use with learning rules
     * @param trg
     * @param pro
     * @return 
     */
    public boolean inChunk(Word trg, Word pro, String[] tokens){
        if (trg.pos == end && !trg.compound && !trg.combined) { // check whether this trg belongs to next chunk
            if (trg.pos < tokens.length - 1) {
                if (tokens[trg.pos + 1].equals("of") && !(end - begin <= 2 && pros.size() == 1)) {
                    return false; // prep
                }
            }
        }
        if (contains(trg) && contains(pro) && pro.pos <= trg.pos) {
            if(pros.size()==1){
                if (!hasConj(trg.pos, tokens)) {
                    if (!pro.combined && !trg.combined) { // TODO: check whether there is any trigger between pro and trg
                        return true;
                    } else if (pro.pos == trg.pos) {
                        return true;
                    }
                }
            }else {
                Word pr = pros.get(pros.size()-1);
                if(pr.pos<trg.pos && !hasConj(trg.pos,tokens)&& pro.pos<=pr.pos){
                    if (!pro.combined && !trg.combined) { // TODO: check whether there is any trigger between pro and trg
                        return true;
                    } else if (pro.pos == trg.pos) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean hasConj(int pos, String [] tokens){
        if(pos>0 && pos<tokens.length){
            if(BasicChunk.ccMap.contains(tokens[pos-1])){
                return true ;
            }
        }
        return false;
    }
    
    /**
     * Check whether a trg has in_chunk form
     * @param trg
     * @return 
     */
    public boolean  is_inChunk(Word trg, String[] tokens){
        if (contains(trg) && pros.size() > 0) {
            int idx = trigs.indexOf(trg);
            int pos = begin;
            int pcount = 0;
            if (trg.pos == end && !trg.compound && !trg.combined) { // check whether this trg belongs to next chunk
                if (trg.pos < tokens.length - 1) {
                    if (tokens[trg.pos + 1].equals("of") && !(pros.size()==1 && trg.pos - pros.get(pros.size()-1).pos <3)) {
                        return false; // prep
                    }
                }
            }
            if (trg.combined) { // combined case
                for (Word pr : pros) {
                    if (pr.pos == trg.pos) {
                        trg.inchunk = true;
                        return true;
                    }
                }
            }
            if (idx > 0) { // there is a trg preceeding this trg
                    pos = getStartPos(trg, tokens);// find start position, it can either be the begin of chunk or after a trigger that preceeds the given trg
            }
            for (Word pr : pros) {
                if (pr.pos <= trg.pos && pr.pos >= pos && !pr.combined) {
                    pcount++;
                }
            }
            if (pcount > 0 && !hasConj(trg.pos, tokens)) {
                trg.inchunk = true;
                return true;
            }
        }
        return false;
    }
    /**
     * Get starting position of PRO to check in-chunk form
     * @param tg: trg to check in-chunk form
     * @return: position to start checking PRO whether in has in-chunk form with the given trg
     */
    private int getStartPos(Word tg, String[] tokens){
        int pos = trigs.indexOf(tg);
        while(pos>0){
            Word tg1 = trigs.get(pos-1);
            if(!isSameRole(tg1,tg,tokens)){
                return tg1.pos+1;
            }else {
                pos--;
            }
        }
        return begin;
    }
    
    /**
     * Get end position of PRO to check in-chunk form
     * @param tg: trg to check in-chunk form
     * @return: position to start checking PRO whether in has in-chunk form with the given trg
     */
    private int getEndPos(Word tg, String[] tokens){
        int pos = trigs.indexOf(tg)+1;
        while(pos<trigs.size()){
            Word tg1 = trigs.get(pos);
            if(!isSameRole(tg1,tg,tokens)){
                return tg1.pos-1;
            }else {
                pos++;
            }
        }
        return end;
    }
    
    public boolean isSameRole(Word tg1, Word tg2, String[] tokens){
        if(tg1.pos_tag.equals(tg2.pos_tag)){
            int pos1 = Math.min(tg1.pos, tg2.pos);
            int pos2 = Math.max(tg1.pos, tg2.pos);
            for(int i= pos1+1 ; i< pos2; i++){
                if(BasicChunk.ccMap.contains(tokens[i])){
                    return true ;
                }
            }
        }
        return false;
    }
    /**
     * Get in_chunk pro for a given tg
     * @param tg: trg
     * @param tags: POS tags
     * @return: list of pro if available 
     */
    public List<Word> getInChunkPro(Word tg, String[] tokens) {
        List<Word> ls = new ArrayList<Word>();
        if (contains(tg) && pros.size() > 0 && tg.inchunk) {
            // 11.10.2011 -> TODO: check this case again!
            if (tg.combined) {
                for (Word w : pros) {
                    if (w.pos == tg.pos) {
                        ls.add(w);
                        return ls;
                    }
                }
            }
            int idx = getStartPos(tg,tokens);
            for (Word w : pros) {
                if (!w.combined && w.pos < tg.pos && w.pos >=idx) {
                    ls.add(w);
                }
            }

        }
        return ls;
    }
    
    
    /**12.10.2011
     * Get pro list in form TG_PRO for strict case (no other trigger in between)
     * @param tags
     * @param tokens
     * @return 
     */
    public List<Word> getPro(String[] tokens){
        List<Word> ls = new ArrayList<Word>();
        Word tg;
        int pos1 =begin, pos2 =end; 
        if(pros.size()>0 && (is_merged || trigs.isEmpty())){
            return pros ;
        }else if(trigs.size()>0 && pros.size()>0){
            int i = 0;
            while(i<trigs.size()) {
               tg = trigs.get(i);
               if(tg.inchunk){ // TODO: should stop/break here? (14.12.2011)
                   pos1 = tg.pos+1;
                   i++;
                   continue;
               }else{ // PRO .. TG of ...
                  pos2 = tg.pos-1;
                  break;
               }
            }
            for(Word w: pros){
                if(w.pos>=pos1 && w.pos<=pos2){
                    ls.add(w);
                }
            }
        }
        return ls;
    }
    
    /** 12.10.2011
     * Get pro list for strict case (no other trigger in between)
     * @param tags
     * @param tokens
     * @return 
     */
    public List<Word> getProFront(){
        List<Word> ls = new ArrayList<Word>();
        Word tg;
        if(pros.size()>0 && (is_merged ||trigs.isEmpty())){
            return pros ;
//        }else if(trigs.size()>0 && pros.size()>0){
//            tg = trigs.get(trigs.size()-1);
//            int idx = tg.pos ;
//            for(Word pr: pros){
//                if(pr.pos > idx && !pr.combined){
//                    ls.add(pr);
//                }
//            }
//            return ls ;
        }
        return ls;
    }
    
   /**
     * Get pros (same chunk with trg) that follow the given trigger for pattern: TG - PR
     * This method is used in case there is no prep or trg has JJ/VBx form in noun chunk
     * @param tg: trigger
     * @return: list of proteins follow a given trigger
     */ 
    public List<Word> getPro(Word tg, String[] tokens){
        List<Word> ls = new ArrayList<Word>();
        int pos = Math.max(begin, tg.pos);
        int idx=end;
        if(contains(tg) && pros.size()>0){
            int pos1 = trigs.indexOf(tg);
            if(trigs.size()==1|| pos1==trigs.size()-1){
                for(Word pr: pros){
                    if(pr.pos> pos && pr.pos <=idx && !pr.combined){
                        ls.add(pr);
                    }
                }
            }else {
                if(pos1<trigs.size()-1){
                    return ls ;
                }
            }
            return ls ;
        }
        return ls;
    }
    
    public String getValues(){
        String st="["+type+" "+txt ;
        if(pros.size()>0){
            st+=" >>PRO: ";
            for(Word w:pros){
                st+=" "+w.word ;
            }
        }
        if(trigs.size()>0){
            st+=" >>TRG ";
            for(Word w:trigs){
                st+=" "+w.word+"_"+w.pos_tag ;
            }
            
        }
        st+="]";
        return st ;
    }
    @Override
    public String toString(){
        String text="["+type+" "+txt+"]" ;
        return text;
    }
}
