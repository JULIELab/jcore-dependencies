package dragon.nlp.tool.xtract;

import dragon.nlp.*;
import dragon.onlinedb.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface WordPairIndexer {
    public DocumentParser getDocumentParser();
    public void setDocumentParser(DocumentParser parser);
    public void index(CollectionReader collectionReader);
    public boolean indexArticle(Article curArticle);
    public void close();
    public void flush();
}