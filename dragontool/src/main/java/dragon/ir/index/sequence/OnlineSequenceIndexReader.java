package dragon.ir.index.sequence;

import dragon.ir.index.Indexer;
import dragon.nlp.extract.ConceptExtractor;
import dragon.onlinedb.Article;
import dragon.onlinedb.CollectionReader;

/**
 * <p>The online index reader for sequencial data </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSequenceIndexReader extends AbstractSequenceIndexReader{
    private OnlineSequenceIndexer indexer;

    public OnlineSequenceIndexReader(ConceptExtractor ce){
        this(ce,null);
    }

    public OnlineSequenceIndexReader(ConceptExtractor ce, CollectionReader collectionReader) {
        this(new OnlineSequenceIndexer(ce),collectionReader);
    }

    public OnlineSequenceIndexReader(OnlineSequenceIndexer indexer){
        this(indexer,null);
    }

    public OnlineSequenceIndexReader(OnlineSequenceIndexer indexer, CollectionReader collectionReader) {
        this.collectionReader =collectionReader;
        this.indexer =indexer;
    }

    public void initialize(){
        if(initialized || collectionReader==null)
            return;

        indexer.initialize();
        if(!index(indexer,collectionReader)){
            System.out.println("Failed to index articles!");
            return;
        }
        indexer.close();
        this.collection =indexer.getIRCollection();
        this.docIndexList =indexer.getDocIndexList();
        this.docKeyList =indexer.getDocKeyList();
        this.termIndexList =indexer.getTermIndexList();
        this.termKeyList =indexer.getTermKeyList();
        this.doctermSeq =indexer.getSequenceReader();
        initialized=true;
    }

    public void setCollectionReader(CollectionReader collectionReader){
        initialized = false;
        this.collectionReader = collectionReader;
    }

    protected boolean index(Indexer indexer, CollectionReader collectionReader){
        Article article;

        try {
            article = collectionReader.getNextArticle();
            while (article != null) {
                if (!indexer.indexed(article.getKey())) {
                    indexer.index(article);
                }
                article = collectionReader.getNextArticle();
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}