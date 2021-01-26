package dragon.onlinedb;

/**
 * <p>Simple article parser which simply treats the content of a file as the body of an article</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleArticleParser implements ArticleParser{
    public Article parse(String content){
        Article article;

        article=new BasicArticle();
        article.setBody(content);
        return article;
    }

    public String assemble(Article article){
        return article.getBody();
    }
}