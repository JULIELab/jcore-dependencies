package dragon.onlinedb.isi;

import dragon.onlinedb.Article;
import dragon.onlinedb.ArticleParser;
import dragon.onlinedb.BasicArticle;

/**
 * <p>Journal of ISI article parser</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class ISIArticleParser implements ArticleParser {

    public Article parse(String content){
        Article article;
        String[] fields;

        fields=null;
        article=new BasicArticle();
        if(content==null) return null;
        fields = content.split("\t");
        article.setAbstract(fields[10]);
        article.setMeta(fields[1]);
        article.setTitle(fields[3]);
        article.setKey(fields[fields.length-1]);

        return article;
    }

    public String assemble(Article article){
        return null;
    }

}