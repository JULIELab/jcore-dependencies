package dragon.config;

import dragon.ir.query.QueryGenerator;
import dragon.onlinedb.Article;
import dragon.onlinedb.CollectionReader;
import dragon.util.FileUtil;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * <p>Query application configuration</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class QueryAppConfig {
    public static void main(String[] args) {
        QueryAppConfig queryApp;
        ConfigureNode root,indexAppNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and indexing applicaiton id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        indexAppNode=util.getConfigureNode(root,"queryapp",Integer.parseInt(args[1]));
        if(indexAppNode==null)
            return;
        queryApp=new QueryAppConfig();
        queryApp.generateQuery(indexAppNode);
    }

    public void generateQuery(ConfigureNode indexAppNode){
        QueryGenerator queryGenerator;
        CollectionReader topicReader;
        String queryFile;
        int queryGeneratorID, collectionID;

        collectionID=indexAppNode.getInt("topicreader");
        queryGeneratorID=indexAppNode.getInt("querygenerator");
        topicReader=(new CollectionReaderConfig()).getCollectionReader(indexAppNode,collectionID);
        queryGenerator=(new QueryGeneratorConfig()).getQueryGenerator(indexAppNode,queryGeneratorID);
        queryFile=indexAppNode.getString("queryfile");
        generateQuery(queryGenerator,topicReader,queryFile);
    }

    public void generateQuery(QueryGenerator queryGenerator, CollectionReader topicReader, String queryFile){
        PrintWriter out;
        ArrayList topics;
        Article article;
        String curQuery;
        int i;

        try {
            topics=new ArrayList();
            while((article=topicReader.getNextArticle())!=null)
                topics.add(article);
            out = FileUtil.getPrintWriter(queryFile);
            out.write(topics.size() + "\n");

            for (i = 0; i < topics.size(); i++) {
                article=(Article)topics.get(i);
                curQuery = queryGenerator.generate(article).toString();
                out.write(article.getCategory() + "\t" + curQuery + "\n");
                out.flush();
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
