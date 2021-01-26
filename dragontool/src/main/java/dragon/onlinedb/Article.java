package dragon.onlinedb;

import java.util.Date;

/**
 * <p>Interface of Article which is the unit of collections</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Article extends Comparable{
    /**
     * Gets the label of the article
     * @return usually the label of the article.
     */
    public int getCategory();
    public void setCategory(int category);
    public String getTitle();
    public void setTitle(String title);
    /**
     * The meta data of an article refers to topical terms (often manually coded) for the articles
     * @return meta data
     */
    public String getMeta();
    public void setMeta(String meta);
    /**
     * The unique string-based entry number of an article. It is required for dragon toolkit.
     * @return the document number
     */
    public String getKey();
    public void setKey(String key);
    public String getAbstract();
    public void setAbstract(String abt);
    public String getBody();
    public void setBody(String body);
    public Date getDate();
    public void setDate(Date date);
    public int getLength();
    public void setLength(int length);
}