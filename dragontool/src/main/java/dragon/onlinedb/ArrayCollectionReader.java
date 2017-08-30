package dragon.onlinedb;

import dragon.util.SortedArray;
import java.util.ArrayList;

/**
 * <p>Collection reader for reading multiple collection </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ArrayCollectionReader implements CollectionReader{
    private SortedArray sortlist;
    private ArrayList list;
    private int curPos;

    public ArrayCollectionReader() {
        list=new ArrayList();
        sortlist=new SortedArray();
        curPos=0;
    }

    public ArticleParser getArticleParser(){
        return null;
    }

    public void setArticleParser(ArticleParser parser){
    }

    public int size(){
    	return list.size();
    }
    
    public void close(){
        list.clear();
        sortlist.clear();
        curPos=0;
    }

    public Article getNextArticle(){
        if(curPos<list.size())
            return (Article)list.get(curPos++);
        else
            return null;
    }

    public Article getArticleByKey(String key){
        BasicArticle article;

        article=new BasicArticle();
        article.setKey(key);
        if(sortlist.binarySearch(article)<0)
            return null;
        else
            return (Article)sortlist.get(sortlist.insertedPos());
    }

    public boolean addArticle(Article article){
        if(!sortlist.add(article))
            return false;
        else{
            list.add(article);
            return true;
        }
    }

    public boolean supportArticleKeyRetrieval(){
        return true;
    }

    public void restart(){
        curPos = 0;
    }
}