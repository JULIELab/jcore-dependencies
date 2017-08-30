package dragon.onlinedb.trec;

import dragon.onlinedb.*;
import dragon.util.FileUtil;
import java.util.ArrayList;
/**
 * <p>TREC topic reader earlier than 2004 </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class EarlyTrecTopicReader extends AbstractTopicReader{

    public EarlyTrecTopicReader(String topicFile){
        super(topicFile);
    }

    protected ArrayList loadTopics(String topicFile){
        ArrayList list;
        String content;
        int start, end;

        try {
            content=FileUtil.readTextFile(topicFile);
            list=new ArrayList();
            start=0;
            end=content.indexOf("</top>");
            while(end>0){
                end=end+6;
                list.add(parseEarlyTrecTopic(content.substring(start,end)));
                start=end;
                end=content.indexOf("</top>",start);
            }
            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Article parseEarlyTrecTopic(String content){
        BasicArticle article;
        String section;
        int start;

        article=new BasicArticle();
        section=getTopicSection("num",content);
        start=section.indexOf(":")+1;
        article.setKey(section.substring(start).trim());
        article.setCategory(Integer.parseInt(article.getKey()));

        section=getTopicSection("title",content);
        if(section!=null){
            start = section.indexOf(":") + 1;
            article.setTitle(section.substring(start).trim());
        }

        section=getTopicSection("desc",content);
        if(section!=null){
            start = section.indexOf(":") + 1;
            article.setAbstract(section.substring(start).trim());
        }

        section=getTopicSection("narr",content);
        if(section!=null){
            start = section.indexOf(":") + 1;
            article.setBody(section.substring(start).trim());
        }

        section=getTopicSection("con",content);
        if(section!=null){
            start = section.indexOf(":") + 1;
            article.setMeta(section.substring(start).trim());
        }

        return article;
    }

    private String getTopicSection(String sectionName, String topic){
        int start, end;

        start=topic.indexOf("<"+sectionName+">");
        if(start<0) return null;

        start+=sectionName.length()+2;
        end=topic.indexOf("\n<",start);
        topic=topic.substring(start,end);
        return topic.replace('\n',' ').trim();
    }
    
}