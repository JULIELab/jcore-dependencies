package dragon.ir.index;

import dragon.matrix.IntSparseMatrix;
import dragon.onlinedb.*;

/**
 * <p>The class is used to read indexing information from memory </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineIndexReader extends AbstractIndexReader {
    protected OnlineIndexer indexer;

    public OnlineIndexReader(OnlineIndexer indexer){
        this(indexer,null);
    }

    public OnlineIndexReader(OnlineIndexer indexer,CollectionReader collectionReader){
        super(indexer.isRelationSupported(),collectionReader);
        this.indexer=indexer;
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
        this.doctermMatrix =indexer.getDocTermMatrix();
        this.doctermMatrix.finalizeData();
        this.termdocMatrix=(IntSparseMatrix)doctermMatrix.transpose();
        if(relationSupported){
            this.relationIndexList =indexer.getRelationIndexList();
            this.docrelationMatrix =indexer.getDocRelationMatrix();
            this.docrelationMatrix.finalizeData();
            this.relationdocMatrix =(IntSparseMatrix)docrelationMatrix.transpose();
        }
        initialized=true;
    }

    public void setCollectionReader(CollectionReader collectionReader){
        initialized=false;
        this.collectionReader =collectionReader;
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