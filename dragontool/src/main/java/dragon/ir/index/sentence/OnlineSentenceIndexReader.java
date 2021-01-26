package dragon.ir.index.sentence;

import dragon.ir.index.AbstractIndexReader;
import dragon.ir.index.Indexer;
import dragon.matrix.IntSparseMatrix;
import dragon.onlinedb.Article;
import dragon.onlinedb.BasicArticle;
import dragon.onlinedb.CollectionReader;
/**
 * <p>Index reader for reading sentence index information on line</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSentenceIndexReader extends AbstractIndexReader{
    protected OnlineSentenceIndexer indexer;
    private OnlineSentenceBase sentBase;

    public OnlineSentenceIndexReader(OnlineSentenceIndexer indexer){
        this(indexer,null);
    }

    public OnlineSentenceIndexReader(OnlineSentenceIndexer indexer,CollectionReader collectionReader){
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
        this.sentBase =indexer.getSentenceBase();
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

    public void close(){
        indexer.close();
        super.close();
    }

    public Article getOriginalDoc(String key){
        BasicArticle article;

        article=new BasicArticle();
        article.setTitle(sentBase.get(key));
        if(article.getTitle()==null)
            return null;

        article.setKey(key);
        return article;
    }

    protected boolean index(Indexer indexer, CollectionReader collectionReader){
        Article article;

        try {
            article = collectionReader.getNextArticle();
            while (article != null) {
                if (!indexer.indexed(article.getKey()))
                    indexer.index(article);
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