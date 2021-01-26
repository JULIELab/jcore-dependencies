package dragon.nlp;

/**
 * <p>Data structure for paragraph of document</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Paragraph {
    public Paragraph next;
    public Paragraph prev;
    private Document parent;
    private Sentence start;
    private Sentence end;
    private int role;
    private int count, index;

    public Paragraph(){
        next = null;
        prev = null;
        start = null;
        end = null;
        parent=null;
        role = 0;
        count = 0;
    }

    public int getIndex(){
    	return index;
    }
    
    public void setIndex(int index){
    	this.index=index;
    }
    
    public Document getParent(){
        return parent;
    }

    public void setParent(Document parent) {
        this.parent = parent;
    }

    public boolean addSentence(Sentence sent){

        if(sent==null)
            return false;

        sent.setParent(this);
        if(end!=null)
            end.next=sent;
        if(start==null)
            start=sent;
        sent.prev=end;
        sent.next=null;
        end=sent;
        count=count+1;
        return true;
    }

    public Sentence getFirstSentence(){
        return start;
    }

    public Sentence getLastSentence(){
        return end;
    }

    public int getRoleInDocument(){
        return role;
    }

    public void setRoleInDocument(int role){
        this.role=role;
    }

    public int getSentenceNum(){
        return count;
    }
}