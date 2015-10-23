package relations;
/**
 *
 * @author Chinh
 * @Date: Oct 29, 2010
 */
public class Word implements Comparable{
    public String TID="";
    public String word;
    public String fullword = null;// word contains this word
    public boolean combined = false ; //PRO-REL
    public int pos = 0; // order of this word in the sentence
    public int loc = 0; //  index of this word in the sentence
    public int [] locs =null; // real position related to full abstract/paragraph
    public boolean compound = false ; // PRO/PRO more than one protein OR word-REL
    public String pos_tag =null; //POS tag for trigger (if two-word trigger, use main word)
    public String chunk_type="";
    // For trigger only; these fields will be filled by evaluation method
    public String type= null ; // trigger type:gene expression, binding, .. ; default -> null: undertermined type
    public int keytype =0 ;// single /mixed / shared trigger; default -> single
    public boolean inchunk=false;
    public boolean used =false;
    public boolean cause =false ;
    public Word(String w, int pos, int loc){
        word = w;
        this.pos = pos ;
        this.loc = loc ;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(TID);
        sb.append('\t');
        sb.append(type);
        sb.append(' ');
        sb.append(locs[0]);
        sb.append(' ');
        sb.append(locs[1]);
        sb.append('\t');
        sb.append(word);
        sb.append('\n');
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return pos- ((Word)o).pos;
    }
}
