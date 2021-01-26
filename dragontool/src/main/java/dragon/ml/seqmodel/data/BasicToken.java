package dragon.ml.seqmodel.data;

/**
 * <p>Basic token related with sequence data</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicToken {
    protected String content;
    protected int label, index;
    protected boolean isSegmentStart;

    public BasicToken(String content){
        this(content,-1);
    }

    public BasicToken(String content, int label) {
        this.content =content;
        this.label =label;
        isSegmentStart=true;
        index=-1;
    }

    public BasicToken copy(){
         BasicToken cur;

         cur=new BasicToken(content,label);
         cur.setSegmentMarker(isSegmentStart);
         cur.setIndex(index);
         return cur;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content =content;
    }

    public int getLabel(){
        return label;
    }

    public void setLabel(int label){
        this.label =label;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index =index;
    }

    public boolean isSegmentStart(){
        return isSegmentStart;
    }

    public void setSegmentMarker(boolean isSegmentStart){
        this.isSegmentStart =isSegmentStart;
    }
}