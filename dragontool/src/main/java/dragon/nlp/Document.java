package dragon.nlp;

/**
 * <p>Data structure for document</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Document {
    public static final int TITLE=1;
    public static final int ABSTRACT = 2;
    public static final int INTRODUCTION = 3;
    public static final int BODY = 4;
    public static final int CONCLUSION = 5;

    private Paragraph start;
    private Paragraph end;
    private int count, index;

    public Document(){
        start=null;
        end=null;
        count=0;
    }

    public boolean addParagraph(Paragraph paragraph){
        if(paragraph==null)
            return false;

        paragraph.setParent(this);
        if(end!=null)
            end.next =paragraph;
        if(start==null)
            start=paragraph;
        paragraph.prev =end;
        paragraph.next =null;
        end=paragraph;
        count=count+1;
        return false;
    }

    public Paragraph getFirstParagraph(){
        return start;
    }

    public Paragraph getLastParagraph(){
        return end;
    }

    public int getParagraphNum(){
        return count;
    }
    
    public int getIndex(){
    	return index;
    }
    
    public void setIndex(int index){
    	this.index=index;
    }
}