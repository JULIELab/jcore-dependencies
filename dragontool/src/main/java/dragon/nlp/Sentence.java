package dragon.nlp;

 /**
 * <p>Data structure for sentence extracted from text</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Sentence {
    public Sentence next;
    public Sentence prev;
    private Paragraph parent;
    private Word start;
    private Word end;
    private int count, index;
    private char m_punctuation;
    private Word subjStart, subjEnd;

    public Sentence() {
        next=null;
        prev=null;
        start=null;
        subjStart=null;
        subjEnd=null;
        end=null;
        count=0;
        parent=null;
        index=-1;
    }

    public int getIndex(){
    	return index;
    }
    
    public void setIndex(int index){
    	this.index=index;
    }
    
    public Paragraph getParent(){
        return parent;
    }

    public void setParent(Paragraph parent) {
        this.parent = parent;
    }

    public void setPunctuation(char punctuation){
        m_punctuation=punctuation;
    }
    public char getPunctuation(){
        return m_punctuation;
    }

    public int getWordNum()
    {
        return count;
    }

    public Word getWord(int index)
    {
        int curPos;
        Word cur;

        if(index<0 || index>=count) return null;

        curPos=0;
        cur=start;
        while(curPos<index){
            cur=cur.next;
            curPos++;
        }
        return cur;
    }

    public Word getFirstWord(){
        return start;
    }
    
    public void resetBoundary(Word start, Word end){
    	this.start=start;
    	start.prev=null;
    	this.end=end;
    	end.next=null;
    	count=0;
    	while(start!=null){
    		start.setParent(this);
    		start.setPosInSentence(count);
    		count++;
    		start=start.next;
    	}
    }

    public Word getLastWord(){
        return end;
    }

    public boolean addWord(Word cur){
        if(cur==null)
            return false;
        cur.setParent(this);
        if(end!=null)
            end.next =cur;
        if(start==null)
            start=cur;
        cur.prev=end;
        cur.next=null;
        cur.setPosInSentence(count);
        end=cur;
        count=count+1;
        return true;
    }

    public String toLinkGrammarString(){
        StringBuffer str=new StringBuffer();
        Word word=getFirstWord();
        while(word!=null)
        {
            str.append(word.getContent());
            str.append(' ');
            word=word.next;
        }
        //str.append(getPunctuation());
        str.setCharAt(str.length()-1,getPunctuation());
        return str.toString();
    }

    public String toPOSTaggedString(){
        StringBuffer str=new StringBuffer();
        Word word=getFirstWord();
        while(word!=null)
        {
            str.append(word.getContent());
            str.append('/');
            str.append(word.getPOSLabel( ));
            str.append(' ');
            word=word.next;
        }
        //str.append(getPunctuation());
        str.setCharAt(str.length()-1,getPunctuation());
        return str.toString();
    }

    public String toBrillTaggerString(){
        StringBuffer str = new StringBuffer();
        Word word = getFirstWord();
        while (word != null) {
            str.append(word.getContent());
            str.append(' ');
            word = word.next;
        }
        str.append(getPunctuation());
        return str.toString();
    }

    public String toString(){
        StringBuffer str;
        Word word, last;

        word = getFirstWord();
        if(word==null)
            return null;
        str= new StringBuffer(word.getContent());

        last=word;
        word=word.next;
        while (word != null) {
            if(word.isPunctuation())
                str.append(word.getContent());
            else if(last.isPunctuation()){
                 if("-_".indexOf(last.getContent()) >= 0)
                     str.append(word.getContent());
                 else if(".'".lastIndexOf(last.getContent())>=0 && word.getContent().length()<=2)
                     str.append(word.getContent());
                else{
                    str.append(' ');
                    str.append(word.getContent());
                }
            }
            else{
                str.append(' ');
                str.append(word.getContent());
            }
            last=word;
            word = word.next;
        }
        str.append(getPunctuation());
        return str.toString();
    }

    public Word getFirstSubjectWord(){
        return subjStart;
    }

    public Word getLastSubjectWord(){
        return subjEnd;
    }

    public void setSubject(Word starting, Word ending){
        subjStart=starting;
        subjEnd=ending;
    }

    public Word indexOf(Word word){
        return indexOf(word.getContent(),0);
    }

    public Word indexOf(Word word, int start){
        return indexOf(word.getContent(),start);
    }

    public Word indexOf(String word){
        return indexOf(word,0);
    }

    public Word indexOf(String word, int start) {
        Word next;

        next = getWord(start);
        while (next != null && !next.getContent().equalsIgnoreCase(word)) {
            next = next.next;
        }
        if (next != null)
            return next;
        else
            return null;
    }
}