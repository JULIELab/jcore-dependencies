package dragon.onlinedb.trec;

import dragon.onlinedb.*;

/**
 * <p>A coarse parser for sgm-styled news articles </p>
 * <p>It extracts document # as the key and treats all remaining tags as the body of an article.
 * All tags will be removed and some special characters wil be replaced.
 * </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CoarseSgmArticleParser extends SgmArticleParser {
    public Article parse(String content){
        BasicArticle article;
        int start, end;

        article=null;
        try{
            article=new BasicArticle();

            //get document #
            start=content.indexOf("<DOCNO>")+7;
            end=content.indexOf("<",start);
            article.setKey(content.substring(start, end).trim());

            //get body
            article.setBody(removeTag(getBodyContent(content,end+8)));
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

    /**
     * One can override this method to exclude some noisy tags.
     * @param rawText original input text
     * @param start the starting position for locating the body content
     * @return tag-removed body content
     */
    protected String getBodyContent(String rawText, int start){
        int end;

        //skip the next tag. ususally FileID or DocID
        start=rawText.indexOf("</",start);
        end=rawText.indexOf(">",start+1);
        return rawText.substring(end+1);
    }
}