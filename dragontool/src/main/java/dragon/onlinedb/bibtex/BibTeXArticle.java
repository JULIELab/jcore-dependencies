package dragon.onlinedb.bibtex;

import dragon.onlinedb.Article;

import java.util.Date;
import java.util.TreeMap;

/**
 * <p>BibTex Article</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BibTeXArticle implements Article{
    private String key;
    private TreeMap map;
    private int category, length;
    private Date date;

    public BibTeXArticle(String text) {
        int start, end, next;
        String field, val;

        //get article key
        start=text.indexOf('{');
        end=text.indexOf(',',start);
        key=text.substring(start+1,end).trim();
        map=new TreeMap();

        next=text.indexOf("= {");
        while(next>=0){
            start=text.lastIndexOf('\t',next);
            field=text.substring(start+1,next).trim();
            end=text.indexOf("},",next);
            if(end<0)
                end=text.indexOf("}",next);
            val=text.substring(next+3,end);
            set(field,val);
            next=text.indexOf("= {",end);
        }
    }

    public void set(String field, String val){
        map.put(field,val);
    }

    public String get(String field){
        return (String)map.get(field);
    }

    public int getCategory(){
        return category;
    }

    public void setCategory(int category){
        this.category =category;
    }

    public String getTitle(){
        return get("title");
    }

    public void setTitle(String title){
        set("title",title);
    }

    public String getMeta(){
        return get("keywords");
    }

    public void setMeta(String meta){
        set("keywords",meta);
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key=key;
    }

    public String getAbstract(){
        return get("abstract");
    }

    public void setAbstract(String abt){
        set("abstract",abt);
    }

    public String getBody(){
        return null;
    }

    public void setBody(String body){
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date){
        this.date =date;
    }

    public int getLength(){
        return length;
    }

    public void setLength(int length){
        this.length =length;
    }

    public int compareTo(Object obj){
        return key.compareTo(((Article)obj).getKey());
    }
}