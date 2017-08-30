package dragon.onlinedb.dm;

import dragon.onlinedb.*;

/**
 * <p>Reuters article parser</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ReutersArticleParser extends BasicArticleParser{

    public Article parse(String text){
        Article article;
        String articleKey,topicStr,topics,title,body;
        int start,end;

        //get article key
        start = text.indexOf("NEWID")+7;
        end = text.indexOf(">")-1;
        articleKey=text.substring(start,end);
        article = new BasicArticle();
        article.setKey(articleKey);

        //get Topics
        topics=null;
        start = text.indexOf("<TOPICS>")+8;
        end = text.indexOf("</TOPICS>");
        topicStr=text.substring(start,end);
        if(topicStr.length()>1){
            while (topicStr.indexOf("<D>") >= 0) {
                start = topicStr.indexOf("<D>") + 3;
                end = topicStr.indexOf("</D>");
                if(topics!=null)
                    topics = topics+", "+topicStr.substring(start, end).trim();
                else
                    topics=topicStr.substring(start, end).trim();
                topicStr = topicStr.substring(end + 4);
            }
            article.setMeta(topics);
        }

        //get Text
        start = text.indexOf("<TEXT>")+6;
        end = text.indexOf("</TEXT>");
        text = text.substring(start,end);

        //get Title
        start=text.indexOf("<TITLE>");
        if(start>=0){
            start = start+ 7;
            end = text.indexOf("</TITLE>");
            title = text.substring(start, end);
            text = text.substring(end + 8);
            article.setTitle(title);
        }

        //get body
        start=text.indexOf("<BODY>");
        if(start>=0){
            start = start+6;
            end = text.indexOf("</BODY>",start);
            body=text.substring(start,end);
        }
        else{
            end=text.indexOf("</AUTHOR>");
            if(end>0)
                text=text.substring(end+9);

            end=text.indexOf("</DATELINE>");
            if(end>0)
                text=text.substring(end+11);
            body=text;
        }
        article.setBody(body);

        return article;
    }
}