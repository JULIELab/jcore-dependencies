package dragon.onlinedb;

import dragon.onlinedb.amazon.*;
import dragon.util.*;
import java.io.BufferedReader;

/**
 * <p>Basic collection preparer which writes article to disk in "collection" format which can be later processed by BasicIndexer and so on </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicCollectionPreparer implements CollectionPreparer{
    private CollectionWriter collectionWriter;
    private ArticleQuery query;

    public static void main(String[] args){
        CollectionWriter writer;
        ArticleQuery query;
        BasicCollectionPreparer preparer;

        writer=new BasicCollectionWriter("indexreview/dc/dc.collection","indexreview/dc/dc.index",false);
        query=new AmazonReviewQuery();
        preparer=new BasicCollectionPreparer(writer,query);
        preparer.addArticles("indexreview/dc/dc.query");
        preparer.close();
    }

    public BasicCollectionPreparer(CollectionWriter writer, ArticleQuery query) {
        this.collectionWriter=writer;
        this.query=query;
    }

    public boolean addListedArticles(String articleKeyFile){
        Article article;
        BufferedReader br;
        String line;

        try{
            if(!query.supportArticleKeyRetrieval()) return false;

            br=FileUtil.getTextReader(articleKeyFile);
            while((line=br.readLine())!=null){
                System.out.println((new java.util.Date()).toString()+ " Processing article #"+line);
                article=query.getArticleByKey(line);
                if(article!=null){
                    collectionWriter.add(article);
                }
            }
            br.close();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean addArticles(ArticleQuery query){
        Article article;

        try{
            while(query.moveToNextArticle())
            {
                article = query.getArticle();
                if(article!=null){
                    System.out.println("Processing article #" + article.getKey());
                    collectionWriter.add(article);
                }
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public boolean addArticles(String queryFile){
        String[] arrQuery;

        arrQuery=loadQuery(queryFile);
        if(arrQuery==null)
            return false;
        return addArticles(arrQuery);
    }

    public boolean addArticles(String[] queries){
        int i;

        try{
            for (i = 0; i < queries.length; i++) {
                System.out.print((new java.util.Date()).toString()+ " Initializing Query: "+queries[i]);
                query.setSearchTerm(queries[i]);
                if (!query.initQuery()){
                    System.out.println("  "+0);
                    return false;
                }
                System.out.println("  "+query.size());
                addArticles(query);
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean addArticles(ArticleQuery query, int interval){
        Article article;
        int i;

        try{
            i=-1;
            while(query.moveToNextArticle())
            {
                i=i+1;
                if(i%interval==0){
                    article = query.getArticle();
                    if(article!=null){
                        System.out.println("Processing article #" + article.getKey());
                        collectionWriter.add(article);
                    }
                }
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void close(){
        collectionWriter.close();
    }

    private String[] loadQuery(String queryFile){
        BufferedReader br;
        String[] queries;
        int i, queryNum;

        try
        {
            br = FileUtil.getTextReader(queryFile);
            queryNum = Integer.parseInt(br.readLine());
            queries = new String[queryNum];
            for(i = 0; i<queryNum; i++)
                queries[i]=br.readLine();
            return queries;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}