package dragon.config;

import dragon.ir.index.IndexReader;
import dragon.ir.search.*;
import dragon.ir.search.feedback.Feedback;
import dragon.ir.search.smooth.Smoother;

/**
 * <p>Searcher configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SearcherConfig extends ConfigUtil{
    public SearcherConfig() {
       super();
    }

    public SearcherConfig(ConfigureNode root){
       super(root);
    }

    public SearcherConfig(String configFile){
        super(configFile);
    }

    public Searcher getSearcher(int searcherID){
        return getSearcher(root,searcherID);
    }

    public Searcher getSearcher(ConfigureNode node, int searcherID){
        return loadSearcher(node,searcherID);
    }

    private Searcher loadSearcher(ConfigureNode node, int searcherID){
        ConfigureNode searcherNode;
        String searcherName;

        searcherNode=getConfigureNode(node,"searcher",searcherID);
        if(searcherNode==null)
            return null;
        searcherName=searcherNode.getNodeName();
        return loadSearcher(searcherName,searcherNode);
    }

    protected Searcher loadSearcher(String searcherName,ConfigureNode searcherNode){
        if(searcherName.equalsIgnoreCase("FullRankSearcher"))
            return loadFullRankSearcher(searcherNode);
        else if(searcherName.equalsIgnoreCase("PartialRankSearcher"))
            return loadPartialRankSearcher(searcherNode);
        else if(searcherName.equalsIgnoreCase("BoolRankSearcher"))
            return loadBoolRankSearcher(searcherNode);
        else if(searcherName.equalsIgnoreCase("FeedbackSearcher"))
            return loadFeedbackSearcher(searcherNode);
        else
            return (Searcher)loadResource(searcherNode);
    }

    private Searcher loadFullRankSearcher(ConfigureNode node){
        IndexReader indexReader;
        int indexReaderID;

        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        return new FullRankSearcher(indexReader,getSmoother(node));
    }

    private Searcher loadPartialRankSearcher(ConfigureNode node){
        IndexReader indexReader;
         int indexReaderID;

         indexReaderID=node.getInt("indexreader");
         indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
         return new PartialRankSearcher(indexReader,getSmoother(node));
    }

    private Searcher loadBoolRankSearcher(ConfigureNode node){
        IndexReader indexReader;
        int indexReaderID;

        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        return new BoolRankSearcher(indexReader,getSmoother(node));
    }

    private Searcher loadFeedbackSearcher(ConfigureNode node){
        FeedbackConfig config;
        Feedback feedback;
        int feedbackID, searcherID;
        boolean sameSearcher;

        config=new FeedbackConfig();
        feedbackID=node.getInt("feedback");
        feedback=config.getFeedback(node,feedbackID);
        sameSearcher=node.getBoolean("sameasinitsearcher",false);
        if(sameSearcher)
            return new FeedbackSearcher(feedback.getSearcher(),feedback);
        else{
            searcherID=node.getInt("searcher");
            return new FeedbackSearcher(getSearcher(node,searcherID),feedback);
        }
    }

    private Smoother getSmoother(ConfigureNode node){
        SmootherConfig config;
        int smootherID;

        smootherID=node.getInt("smoother",0);
        if(smootherID<=0)
            return null;
        config=new SmootherConfig();
        return config.getSmoother(node,smootherID);
    }
}
