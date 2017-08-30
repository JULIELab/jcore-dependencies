package dragon.config;

import dragon.onlinedb.*;
import dragon.onlinedb.trec.*;

/**
 * <p>Collection reader configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CollectionReaderConfig extends ConfigUtil{
    public CollectionReaderConfig() {
       super();
    }

    public CollectionReaderConfig(ConfigureNode root){
       super(root);
    }

    public CollectionReaderConfig(String configFile){
        super(configFile);
    }

    public CollectionReader getCollectionReader(int collectionID){
        return loadCollectionReader(root,collectionID);
    }

    public CollectionReader getCollectionReader(ConfigureNode node, int extractorID){
        return loadCollectionReader(node,extractorID);
    }

    private CollectionReader loadCollectionReader(ConfigureNode node, int extractorID){
        ConfigureNode extractorNode;
        String extractorName;

        extractorNode=getConfigureNode(node,"collectionreader",extractorID);
        if(extractorNode==null)
            return null;
        extractorName=extractorNode.getNodeName();
        return loadCollectionReader(extractorName,extractorNode);
    }

    protected CollectionReader loadCollectionReader(String collectionName,ConfigureNode collectionNode){
        if(collectionName.equalsIgnoreCase("BasicCollectionReader"))
            return loadBasicCollectionReader(collectionNode);
        else if(collectionName.equalsIgnoreCase("SimpleCollectionReader"))
            return loadSimpleCollectionReader(collectionNode);
        else if(collectionName.equalsIgnoreCase("TrecCollectionReader"))
            return loadTrecCollectionReader(collectionNode);
        else if(collectionName.equalsIgnoreCase("EarlyTrecTopicReader"))
            return new EarlyTrecTopicReader(collectionNode.getString("topicfile"));
        else if(collectionName.equalsIgnoreCase("Genomics2005TopicReader"))
            return new Genomics2005TopicReader(collectionNode.getString("topicfile"));
        else if(collectionName.equalsIgnoreCase("Genomics2004TopicReader"))
            return new Genomics2004TopicReader(collectionNode.getString("topicfile"));
        else
            return (CollectionReader)loadResource(collectionNode);
    }

    private CollectionReader loadBasicCollectionReader(ConfigureNode curNode){
        CollectionReader reader;
        ArticleParser parser;
        String collectionPath, collectionName, collectionFile, indexFile;
        String articleParser;

        try{
            collectionPath = curNode.getString("collectionpath");
            collectionName = curNode.getString("collectionname");
            collectionFile = curNode.getString("collectionfile");
            indexFile = curNode.getString("indexfile");
            articleParser=curNode.getString("articleparser","dragon.onlinedb.BasicArticleParser");
            if(collectionFile==null){
                collectionFile=collectionPath+"/"+collectionName+".collection";
                indexFile=collectionPath+"/"+collectionName+".index";
                reader = new BasicCollectionReader(collectionFile, indexFile);
            }
            else
                reader = new BasicCollectionReader(collectionFile, indexFile);
            parser=getArticleParser(articleParser);
            if(parser==null){
                System.out.println("Can not load the article parser.");
                return null;
            }
            reader.setArticleParser(parser);
            return reader;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private CollectionReader loadSimpleCollectionReader(ConfigureNode curNode){
        CollectionReader reader;
        ArticleParser parser;
        String collectionPath;
        String articleParser;

        try{
            collectionPath = curNode.getString("collectionpath",null);
            articleParser=curNode.getString("articleparser","dragon.onlinedb.SimpleArticleParser");
            parser=getArticleParser(articleParser);
            if(parser==null){
                System.out.println("Can not load the article parser.");
                return null;
            }

            if(collectionPath==null)
                reader=new SimpleCollectionReader(parser);
            else
                reader = new SimpleCollectionReader(collectionPath,parser);
            return reader;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private CollectionReader loadTrecCollectionReader(ConfigureNode curNode){
        CollectionReader reader;
        ArticleParser parser;
        String collectionPath, indexFile;
        String articleParser;

        try{
            collectionPath = curNode.getString("collectionpath",null);
            indexFile = curNode.getString("indexfile",null);
            articleParser=curNode.getString("articleparser","dragon.onlinedb.BasicArticleParser");
            parser=getArticleParser(articleParser);
            if(parser==null){
                System.out.println("Can not load the article parser.");
                return null;
            }
            if(collectionPath==null && indexFile==null)
                reader = new TrecCollectionReader(parser);
            else if(indexFile==null)
                reader = new TrecCollectionReader(collectionPath,parser);
            else
                reader = new TrecCollectionReader(collectionPath,indexFile,parser);
            return reader;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    protected ArticleParser getArticleParser(String className){
         Class myClass;

         try{
             myClass = Class.forName(className);
             return (ArticleParser) myClass.newInstance();
         }
         catch(Exception e){
             e.printStackTrace();
             return null;
         }
    }
}
