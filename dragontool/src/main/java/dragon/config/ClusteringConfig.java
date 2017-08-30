package dragon.config;

import dragon.ir.index.*;
import dragon.ir.clustering.*;
import dragon.ir.clustering.clustermodel.*;
import dragon.ir.clustering.docdistance.*;
import dragon.matrix.SparseMatrix;

/**
 * <p>Clustering configuration </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ClusteringConfig extends ConfigUtil{
    public ClusteringConfig() {
       super();
    }

    public ClusteringConfig(ConfigureNode root){
       super(root);
    }

    public ClusteringConfig(String configFile){
        super(configFile);
    }

    public Clustering getClustering(int clusteringID){
        return getClustering(root,clusteringID);
    }

    public Clustering getClustering(ConfigureNode node, int clusteringID){
        return loadClustering(node,clusteringID);
    }

    private Clustering loadClustering(ConfigureNode node, int clusteringID){
        ConfigureNode clusteringNode;
        String clusteringName;

        clusteringNode=getConfigureNode(node,"clustering",clusteringID);
        if(clusteringNode==null)
            return null;
        clusteringName=clusteringNode.getNodeName();
        return loadClustering(clusteringName,clusteringNode);
    }

    protected Clustering loadClustering(String clusteringName,ConfigureNode clusteringNode){
        if(clusteringName.equalsIgnoreCase("HierClustering"))
            return loadHierClustering(clusteringNode);
        else if(clusteringName.equalsIgnoreCase("BasicKMean"))
            return loadBasicKMean(clusteringNode);
        else if(clusteringName.equalsIgnoreCase("BisectKMean"))
            return loadBisectKMean(clusteringNode);
        else if(clusteringName.equalsIgnoreCase("LinkKMean"))
            return loadLinkKMean(clusteringNode);
        else
            return (Clustering)loadResource(clusteringNode);
    }

    private Clustering loadHierClustering(ConfigureNode node){
        HierClustering clustering;
        IndexReader indexReader;
        DocDistance docDistance;
        String linkageMode;
        int indexReaderID, docDistanceID, filterID, clusterNum, linkage;

        clusterNum=node.getInt("clusternum");
        filterID=node.getInt("featurefilter",-1);
        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        docDistanceID=node.getInt("docdistance");
        docDistance=(new DocDistanceConfig()).getDocDistance(node,docDistanceID);
        linkageMode=node.getString("linkage","complete");
        if(linkageMode.equalsIgnoreCase("complete"))
            linkage=HierClustering.COMPLETE_LINKAGE;
        else if(linkageMode.equalsIgnoreCase("average"))
            linkage=HierClustering.AVERAGE_LINKAGE;
        else if(linkageMode.equalsIgnoreCase("single"))
            linkage=HierClustering.SINGLE_LINKAGE;
        else
            return null;
        clustering=new HierClustering(indexReader,docDistance,clusterNum,linkage);
        if(filterID>0)
            clustering.setFeatureFilter((new FeatureFilterConfig()).getFeatureFilter(node,filterID));
        return clustering;
    }

    private Clustering loadBasicKMean(ConfigureNode node){
        BasicKMean kmean;
        IndexReader indexReader;
        ClusterModel clusterModel;
        int indexReaderID, clusterModelID, filterID, clusterNum, maxIteration;
        long randomSeed;
        boolean initAllObjects;

        clusterNum=node.getInt("clusternum");
        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        clusterModelID=node.getInt("clustermodel");
        clusterModel=(new ClusterModelConfig()).getClusterModel(node,clusterModelID);
        maxIteration=node.getInt("maxiteration",50);
        randomSeed=node.getInt("randomseed",-1);
        filterID=node.getInt("featurefilter",-1);
        initAllObjects=node.getBoolean("initallobjects",false);
        kmean=new BasicKMean(indexReader,clusterModel,clusterNum,initAllObjects);
        kmean.setMaxIteration(maxIteration);
        kmean.setRandomSeed(randomSeed);
        if(filterID>0)
            kmean.setFeatureFilter((new FeatureFilterConfig()).getFeatureFilter(node,filterID));
        return kmean;
    }

    private Clustering loadBisectKMean(ConfigureNode node){
        BisectKMean kmean;
        IndexReader indexReader;
        ClusterModel clusterModel;
        int indexReaderID, clusterModelID, clusterNum, maxIteration, filterID;
        long randomSeed;
        boolean refine;
        boolean initAllObjects;

        clusterNum=node.getInt("clusternum");
        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        clusterModelID=node.getInt("clustermodel");
        clusterModel=(new ClusterModelConfig()).getClusterModel(node,clusterModelID);
        refine=node.getBoolean("refine",true);
        maxIteration=node.getInt("maxiteration",50);
        randomSeed=node.getInt("randomseed",-1);
        filterID=node.getInt("featurefilter",-1);
        initAllObjects=node.getBoolean("initallobjects",false);
        kmean=new BisectKMean(indexReader,clusterModel,clusterNum, initAllObjects);
        kmean.setMaxIteration(maxIteration);
        kmean.setRandomSeed(randomSeed);
        kmean.setRefine(refine);
        if(filterID>0)
                kmean.setFeatureFilter((new FeatureFilterConfig()).getFeatureFilter(node,filterID));
        return kmean;
    }

    private Clustering loadLinkKMean(ConfigureNode node){
        LinkKMean kmean;
        SparseMatrix outLinks, inLinks;
        Clustering initClustering;
        String paraType;
        int clusteringID, outLinkID, inLinkID;

        clusteringID=node.getInt("initclustering");
        initClustering=(new ClusteringConfig()).getClustering(node,clusteringID);
        outLinkID=node.getInt("outlinkmatrix");
        paraType=node.getParameterType("outlinkmatrix");
        if(paraType==null || paraType.equalsIgnoreCase("intsparematrix"))
            outLinks=(new SparseMatrixConfig()).getIntSparseMatrix(node,outLinkID);
        else
            outLinks=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,outLinkID);
        inLinkID=node.getInt("inlinkmatrix");
        if(inLinkID<=0 || inLinkID==outLinkID)
            inLinks=null;
        else{
            paraType=node.getParameterType("inlinkmatrix");
            if(paraType==null || paraType.equalsIgnoreCase("intsparematrix"))
                inLinks = (new SparseMatrixConfig()).getIntSparseMatrix(node, inLinkID);
            else
                inLinks = (new SparseMatrixConfig()).getDoubleSparseMatrix(node, inLinkID);
        }
        kmean=new LinkKMean(initClustering,outLinks,inLinks);
        kmean.setMaxIteration(node.getInt("maxiteration",10));
        kmean.setRandomSeed(node.getInt("randomseed",-1));
        kmean.setUseWeight(node.getBoolean("useweight",false));
        return kmean;
    }
}
