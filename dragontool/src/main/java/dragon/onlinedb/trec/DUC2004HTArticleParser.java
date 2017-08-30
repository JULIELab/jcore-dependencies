package dragon.onlinedb.trec;

import dragon.onlinedb.*;

/**
 * <p>DUC 2004 HT article parser</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DUC2004HTArticleParser implements ArticleParser{
    public String assemble(Article article){
        return null;
    }

    public Article parse(String content){
        BasicArticle article;
        int start, end;

        article=null;
        try{
            article=new BasicArticle();

            //get PMID
            start=content.indexOf("docid=");
            if(start<0)
                return null;
            start=start+6;
            end=content.indexOf(" ",start);
            article.setKey(content.substring(start, end).trim());

            //Body
            start=content.indexOf(">",end+1);
            start=start+1;
            end=content.indexOf("(AFP)",start);
            if(end>0){
                start=end+5;
                end=content.indexOf(" - ",start);
                if(end<start+5)
                    start=end+3;
            }
            article.setBody(content.substring(start));
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