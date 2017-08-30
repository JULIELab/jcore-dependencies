package dragon.onlinedb.citeulike;

import dragon.onlinedb.*;
import dragon.onlinedb.bibtex.BibTeXArticle;

/**
 * <p>CiteULike Article Parser </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CiteULikeArticleParser extends BasicArticleParser{

    /**
     * parse the CiteULIke format article
     * @param raw the html source code of the given article
     * @return an article
     */
    public Article parse(String raw){
        Article article;
        int start, end;

        start=raw.indexOf("<pre>");
        if(start<0)
            return null;
        start=start+5;
        end=raw.indexOf("</pre>",start);
        if(end<0)
            return null;
        article=new BibTeXArticle(raw.substring(start,end));
        if(article!=null){
            if(article.getKey()!=null){
               start = article.getKey().indexOf(':');
               if (start >= 0)
                article.setKey(article.getKey().substring(start + 1));
            }
            if(article.getMeta()!=null){
                article.setMeta(article.getMeta().replace('-','_'));
            }

        }
        return article;
    }
}