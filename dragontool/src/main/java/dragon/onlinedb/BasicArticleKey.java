package dragon.onlinedb;

/**
 * <p>Data structure for article key </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicArticleKey implements Comparable {
    private String key;
    private long offset;
    private int length, fileIndex;

    public BasicArticleKey(String key){
        this.key=key;
        offset=-1;
        fileIndex=-1;
        length=0;
    }

    public String getKey(){
        return key;
    }

    public long getOffset(){
        return offset;
    }

    public int getLength(){
        return length;
    }

    public void setLength(int length){
        this.length =length;
    }

    public void setOffset(long offset){
        this.offset =offset;
    }

    public int getFileIndex(){
        return fileIndex;
    }

    public void setFileIndex(int index){
        fileIndex=index;
    }

    public int compareTo(Object obj){
        String objKey;

        objKey=((BasicArticleKey)obj).getKey();
        return key.compareTo(objKey);
    }
}