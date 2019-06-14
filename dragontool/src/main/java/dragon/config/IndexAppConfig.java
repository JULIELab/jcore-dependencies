package dragon.config;

import dragon.ir.index.Indexer;
import dragon.onlinedb.Article;
import dragon.onlinedb.CollectionReader;

/**
 * <p>Index application configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IndexAppConfig {
    public IndexAppConfig() {
    }

    public static void main(String[] args) {
        IndexAppConfig indexApp;
        ConfigureNode root,indexAppNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and indexing applicaiton id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        indexAppNode=util.getConfigureNode(root,"indexapp",Integer.parseInt(args[1]));
        if(indexAppNode==null)
            return;
        indexApp=new IndexAppConfig();
        indexApp.indexCollection(indexAppNode);
    }

    public void indexCollection(ConfigureNode indexAppNode){
        CollectionReaderConfig collectionConfig;
        IndexerConfig indexerConfig;
        Indexer indexer;
        CollectionReader[] arrCollectionReader;
        String collectionIDs, arrCollection[];
        int indexerID;
        boolean useMeta;
        int i;

        collectionConfig=new CollectionReaderConfig();
        indexerConfig=new IndexerConfig();
        useMeta=indexAppNode.getBoolean("usemeta",false);
        indexerID=indexAppNode.getInt("indexer",-1);
        if(indexerID<0)
            return;
        indexer=indexerConfig.getIndexer(indexAppNode, indexerID);
        if(indexer==null)
            return;
        collectionIDs=indexAppNode.getString("collectionreader",null);
        if(collectionIDs==null)
            return;
        arrCollection=collectionIDs.split(";");
        arrCollectionReader=new CollectionReader[arrCollection.length];
        for(i=0;i<arrCollection.length;i++){
            arrCollectionReader[i]=collectionConfig.getCollectionReader(indexAppNode,Integer.parseInt(arrCollection[i]));
        }
        indexCollection(indexer,arrCollectionReader,useMeta);
    }

    public void indexCollection(Indexer indexer, CollectionReader[] arrCollectionReader, boolean useMeta){
        Article article;
        int i;

        try {
            for(i=0;i<arrCollectionReader.length;i++){
                article = arrCollectionReader[i].getNextArticle();
                while (article != null) {
                    if (!indexer.indexed(article.getKey())) {
                        System.out.print(new java.util.Date().toString() + " Indexing article #" + article.getKey() + ": ");
                        if (!useMeta)
                            article.setMeta(null);
                        if (!indexer.index(article)) {
                            System.out.println("failed");
                        }
                        else {
                            System.out.println("successful");
                        }
                    }
                    article = arrCollectionReader[i].getNextArticle();
                }
                arrCollectionReader[i].close();
            }
            indexer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            indexer.close();
        }
    }
}