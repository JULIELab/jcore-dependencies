package dragon.onlinedb.trec;

import dragon.onlinedb.Article;
import dragon.onlinedb.ArticleParser;
import dragon.onlinedb.BasicArticle;

/**
 * <p>DUC 2004 MT article parser </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DUC2004MTArticleParser implements ArticleParser {
    public String assemble(Article article){
        return null;
    }

    public Article parse(String content){
        BasicArticle article;
        StringBuffer body;
        String sentence;
        int start, end;

        article=null;
        try{
            article=new BasicArticle();

            //get PMID
            start=content.indexOf("<DOCNO>")+7;
            end=content.indexOf("<",start);
            article.setKey(content.substring(start, end).trim());

            //Body
            body=null;
            start=content.indexOf("<s num",end);
            while(start>0){
                start=content.indexOf(">",start+6)+1;
                end=content.indexOf("</s>",start);
                sentence=content.substring(start,end);
                start=sentence.indexOf("(AFP) -");
                if(start>=0)
                    sentence=sentence.substring(start+7);
                if(body==null)
                    body=new StringBuffer(sentence);
                else{
                    body.append(' ');
                    body.append(sentence);
                }
                start=content.indexOf("<s num",end+4);
            }
            article.setBody(body.toString());
            return article;
        }
        catch(Exception e){
            e.printStackTrace();
            if(article.getKey()!=null)
               return article;
            else
                return null;
        }
    }
}