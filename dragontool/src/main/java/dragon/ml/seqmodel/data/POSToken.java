package dragon.ml.seqmodel.data;

/**
 * <p>Token with Part of Speech Information</p>
 * <p>: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class POSToken extends BasicToken{
    protected int posTag;

    public POSToken(String content) {
        super(content);
        posTag=-1;
    }

    public POSToken(String content, int label){
        super(content,label);
        posTag=-1;
    }

    public int getPOSTag(){
        return posTag;
    }

    public void setPOSTag(int tag) {
        this.posTag = tag;
    }

    public BasicToken copy(){
        POSToken cur;

        cur = new POSToken(content, label);
        cur.setPOSTag(posTag);
        cur.setSegmentMarker(isSegmentStart);
        cur.setIndex(index);
        return cur;
    }
}