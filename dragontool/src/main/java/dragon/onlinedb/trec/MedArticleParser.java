package dragon.onlinedb.trec;

import dragon.onlinedb.*;

/**
 * <p>TREC medical article parser</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class MedArticleParser implements ArticleParser {

    public Article parse(String content){
        BasicArticle article;
        int start, end;

        try{
            article=new BasicArticle();

            //get PMID
            start=content.indexOf("<DOCNO>")+7;
            end=content.indexOf("<",start);
            article.setKey(content.substring(start, end).trim());

            //get Title
            start = end;
            start = content.indexOf("<ArticleTitle>");
            if (start >= 0) {
                start = start+14;
                end = content.indexOf("</ArticleTitle>", start);
                article.setTitle(content.substring(start, end).replace('\n', ' '));
            }

            //get Abstract
            start = end;
            start = content.indexOf("<AbstractText>");
            if (start >= 0) {
                start = start + 14;
                end = content.indexOf("</AbstractText>", start);
                article.setAbstract( content.substring(start, end));
            }

            return article;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String assemble(Article article){
        return null;
    }
}