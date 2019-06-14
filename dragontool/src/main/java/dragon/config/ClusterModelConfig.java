package dragon.config;

import dragon.ir.clustering.clustermodel.ClusterModel;
import dragon.ir.clustering.clustermodel.CosineClusterModel;
import dragon.ir.clustering.clustermodel.EuclideanClusterModel;
import dragon.ir.clustering.clustermodel.MultinomialClusterModel;
import dragon.ir.index.IndexReader;
import dragon.ir.kngbase.KnowledgeBase;
import dragon.matrix.DoubleSparseMatrix;

/**
 * <p>Cluster model configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ClusterModelConfig extends ConfigUtil{
    public ClusterModelConfig() {
       super();
    }

    public ClusterModelConfig(ConfigureNode root){
       super(root);
    }

    public ClusterModelConfig(String configFile){
        super(configFile);
    }

    public ClusterModel getClusterModel(int clusterModelID){
        return getClusterModel(root,clusterModelID);
    }

    public ClusterModel getClusterModel(ConfigureNode node, int clusterModelID){
        return loadClusterModel(node,clusterModelID);
    }

    private ClusterModel loadClusterModel(ConfigureNode node, int clusterModelID){
        ConfigureNode clusterModelNode;
        String clusterModelName;

        clusterModelNode=getConfigureNode(node,"clustermodel",clusterModelID);
        if(clusterModelNode==null)
            return null;
        clusterModelName=clusterModelNode.getNodeName();
        return loadClusterModel(clusterModelName,clusterModelNode);
    }

    protected ClusterModel loadClusterModel(String clusterModelName,ConfigureNode clusterModelNode){
        if(clusterModelName.equalsIgnoreCase("CosineClusterModel"))
            return loadCosineClusterModel(clusterModelNode);
        else if(clusterModelName.equalsIgnoreCase("EuclideanClusterModel"))
            return loadEuclideanClusterModel(clusterModelNode);
        else if(clusterModelName.equalsIgnoreCase("MultinomialClusterModel"))
            return loadMultinomialClusterModel(clusterModelNode);
        else
            return (ClusterModel)loadResource(clusterModelNode);
    }

    private ClusterModel loadCosineClusterModel(ConfigureNode node){
        int matrixID, clusterNum;

        clusterNum=node.getInt("clusternum");
        matrixID=node.getInt("doublematrix",-1);
        if(matrixID>0)
            return new CosineClusterModel(clusterNum, (new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID));
        else{
            matrixID=node.getInt("intmatrix",-1);
            return new CosineClusterModel(clusterNum, (new SparseMatrixConfig()).getIntSparseMatrix(node,matrixID));
        }
    }

    private ClusterModel loadEuclideanClusterModel(ConfigureNode node){
        int matrixID, clusterNum;

        clusterNum=node.getInt("clusternum");
        matrixID=node.getInt("doublematrix",-1);
        if(matrixID>0)
            return new EuclideanClusterModel(clusterNum, (new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID));
        else{
            matrixID=node.getInt("intmatrix",-1);
            return new EuclideanClusterModel(clusterNum, (new SparseMatrixConfig()).getIntSparseMatrix(node,matrixID));
        }
    }

    private ClusterModel loadMultinomialClusterModel(ConfigureNode node){
        IndexReader indexReader, topicIndexReader;
        KnowledgeBase kngBase;
        DoubleSparseMatrix topicTransMatrix;
        int indexReaderID, topicIndexReaderID, matrixID, kngID, clusterNum;
        double bkgCoefficient, transCoefficient;

        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        clusterNum=node.getInt("clusternum");
        bkgCoefficient=node.getDouble("bkgcoefficient",-1);
        if(bkgCoefficient<=0)
            return new MultinomialClusterModel(clusterNum,indexReader);
        else{
            matrixID=node.getInt("transmatrix");
            kngID=node.getInt("knowledgebase");
            topicIndexReaderID=node.getInt("topicindexreader",indexReaderID);
            if(matrixID<=0 && kngID<=0)
                return new MultinomialClusterModel(clusterNum,indexReader, bkgCoefficient);
            if(topicIndexReaderID==indexReaderID)
                topicIndexReader=indexReader;
            else
                topicIndexReader=(new IndexReaderConfig()).getIndexReader(node,topicIndexReaderID);
            transCoefficient=node.getDouble("transcoefficient");
            if(matrixID>0){
                topicTransMatrix = (new SparseMatrixConfig()).getDoubleSparseMatrix(node, matrixID);
                return new MultinomialClusterModel(clusterNum, indexReader, topicIndexReader, topicTransMatrix,
                    transCoefficient, bkgCoefficient);
            }
            else{
                kngBase= (new KnowledgeBaseConfig()).getKnowledgeBase(node,kngID);
                return new MultinomialClusterModel(clusterNum, indexReader, topicIndexReader, kngBase,
                    transCoefficient, bkgCoefficient);
            }
        }
    }
}
