package dragon.onlinedb.trec;

import dragon.onlinedb.Article;
import dragon.onlinedb.ArticleParser;
import dragon.onlinedb.BasicArticleParser;
import dragon.onlinedb.CollectionReader;

import java.util.ArrayList;
/**
 * <p>Abstract class for TREC topic reading</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractTopicReader implements CollectionReader{
    private ArrayList topics;
    private int curTopic;

    public AbstractTopicReader(String topicFile) {
        topics=loadTopics(topicFile);
        curTopic=0;
    }

    protected abstract ArrayList loadTopics(String topicFile);

    public boolean loadCollection(String topicFile){
        topics=loadTopics(topicFile);
        curTopic=0;
        return true;
    }

    public ArticleParser getArticleParser(){
        return new BasicArticleParser();
    }

    public void setArticleParser(ArticleParser parser){
        //do nothing
    }

    public Article getNextArticle(){
        if(topics==null || curTopic>=topics.size())
            return null;
        else{
            curTopic++;
            return (Article) topics.get(curTopic-1);
        }
    }

    public Article getArticleByKey(String key){
        return null;
    }

    public void close(){
        topics=null;
    }
    
    public int size(){
    	return topics.size();
    }

    public boolean supportArticleKeyRetrieval(){
        return false;
    }

    public void restart(){
        curTopic=0;
    }

}