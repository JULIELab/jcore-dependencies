package dragon.onlinedb;

import java.util.Date;
/**
 * <p>Basic article implements basic functions related with article operations </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicArticle implements Article{
    protected String rawText;
    protected String key;
    protected String title,meta, abt,body;
    protected Date date;
    protected int category, length;

    public BasicArticle(String rawText){
        this.rawText =rawText;
        title=null;
        abt=null;
        key=null;
        body=null;
        date=null;
        category=-1;
        length=-1;
    }

    public BasicArticle(){
        title=null;
        abt=null;
        key=null;
        body=null;
        date=null;
        category=-1;
        rawText=null;
        length=-1;
    }

    public String getRawText(){
        return rawText;
    }

    public int getCategory(){
        return category;
    }

    public void setCategory(int category){
        this.category =category;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public String getMeta(){
        return meta;
    }

    public void setMeta(String meta){
        this.meta =meta;
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key=key;
    }

    public String getAbstract(){
        return abt;
    }

    public void setAbstract(String abt){
        this.abt=abt;
    }

    public String getBody(){
        return body;
    }

    public void setBody(String body){
        this.body=body;
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