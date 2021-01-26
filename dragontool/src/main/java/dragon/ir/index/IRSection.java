package dragon.ir.index;

/**
 * <p>IRCollection is the data structure for document section indexing </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IRSection implements Comparable{
    public static final int SEC_ALL=0;
    public static final int SEC_TITLE=1;
    public static final int SEC_ABSTRACT=2;
    public static final int SEC_BODY=3;
    public static final int SEC_META=4;
    public static final int DEF_SEC_NUM=4;

    private int id;
    private String name;
    private IndexWriter indexWriter;
    private IndexReader indexReader;
    private boolean enabled;
    private double weight;

    public IRSection(int section) {
        this.id=section;
        switch(section){
            case 1:
                name="title";
                break;
            case 2:
                name="absract";
                break;
            case 3:
                name="body";
                break;
            case 4:
                name="meta";
                break;
            default:
                name="all";
        }
        enabled=true;
        weight=1;
    }

    public IRSection(int section, String name){
        this.id=section;
        this.name =name;
        enabled=true;
    }

    public IRSection copy(){
        IRSection cur;

        cur=new IRSection(id,name);
        if(enabled())
            cur.enable();
        else
            cur.disable();
        cur.setIndexReader(getIndexReader());
        cur.setIndexWriter(getIndexWriter());
        cur.setWeight(getWeight());
        return cur;
    }

    public int compareTo(Object obj){
        int objID;

        objID=((IRSection)obj).getSectionID();
        if(id==objID)
            return 0;
        else  if(id>objID)
            return 1;
        else
            return -1;
    }

    public String getSectionName(){
        return name;
    }

    public int getSectionID(){
        return id;
    }

    public void setWeight(double weight){
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public IndexWriter getIndexWriter(){
        return indexWriter;
    }

    public void setIndexWriter(IndexWriter writer){
        this.indexWriter =writer;
    }

    public IndexReader getIndexReader(){
        return indexReader;
    }

    public void setIndexReader(IndexReader reader){
        this.indexReader =reader;
    }

    public boolean enabled(){
        return enabled;
    }

    public void enable(){
        enabled=true;
    }

    public void disable(){
        enabled=false;
    }
}