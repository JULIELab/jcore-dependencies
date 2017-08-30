package dragon.ir.query;

import dragon.onlinedb.*;

/**
 * <p>Abstract class of query generator which implements function "generate". </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractQueryGenerator implements QueryGenerator{

    public IRQuery generate(String topic){
        BasicArticle article;

        article=new BasicArticle();
        article.setTitle(topic);
        return generate(article);
    }
}