package dragon.config;

import dragon.ir.clustering.docdistance.CosineDocDistance;
import dragon.ir.clustering.docdistance.DocDistance;
import dragon.ir.clustering.docdistance.EuclideanDocDistance;
import dragon.ir.clustering.docdistance.KLDivDocDistance;
import dragon.ir.index.IndexReader;
import dragon.matrix.DoubleSparseMatrix;

/**
 * <p>Document distance calculation configuration</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocDistanceConfig extends ConfigUtil{
    public DocDistanceConfig() {
       super();
    }

    public DocDistanceConfig(ConfigureNode root){
       super(root);
    }

    public DocDistanceConfig(String configFile){
        super(configFile);
    }

    public DocDistance getDocDistance(int docDistanceID){
        return getDocDistance(root,docDistanceID);
    }

    public DocDistance getDocDistance(ConfigureNode node, int docDistanceID){
        return loadDocDistance(node,docDistanceID);
    }

    private DocDistance loadDocDistance(ConfigureNode node, int docDistanceID){
        ConfigureNode docDistanceNode;
        String docDistanceName;

        docDistanceNode=getConfigureNode(node,"docdistance",docDistanceID);
        if(docDistanceNode==null)
            return null;
        docDistanceName=docDistanceNode.getNodeName();
        return loadDocDistance(docDistanceName,docDistanceNode);
    }

    protected DocDistance loadDocDistance(String docDistanceName,ConfigureNode docDistanceNode){
        if(docDistanceName.equalsIgnoreCase("CosineDocDistance"))
            return loadCosineDocDistance(docDistanceNode);
        else if(docDistanceName.equalsIgnoreCase("EuclideanDocDistance"))
            return loadEuclideanDocDistance(docDistanceNode);
        else if(docDistanceName.equalsIgnoreCase("KLDivDocDistance"))
            return loadKLDivDocDistance(docDistanceNode);
        else
            return (DocDistance)loadResource(docDistanceNode);
    }

    private DocDistance loadCosineDocDistance(ConfigureNode node){
        int matrixID;

        matrixID=node.getInt("doublematrix",-1);
        if(matrixID>0)
            return new CosineDocDistance((new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID));
        else{
            matrixID=node.getInt("intmatrix",-1);
            return new CosineDocDistance((new SparseMatrixConfig()).getIntSparseMatrix(node,matrixID));
        }
    }

    private DocDistance loadEuclideanDocDistance(ConfigureNode node){
        int matrixID;

        matrixID=node.getInt("doublematrix",-1);
        if(matrixID>0)
            return new EuclideanDocDistance((new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID));
        else{
            matrixID=node.getInt("intmatrix",-1);
            return new EuclideanDocDistance((new SparseMatrixConfig()).getIntSparseMatrix(node,matrixID));
        }
    }

    private DocDistance loadKLDivDocDistance(ConfigureNode node){
        IndexReader indexReader;
        DoubleSparseMatrix matrix;
        double normThreshold;
        int indexReaderID,matrixID;

        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        matrixID=node.getInt("doublematrix");
        matrix=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID);
        normThreshold=node.getDouble("normthreshold",0);
        if(indexReader!=null)
            return new KLDivDocDistance(indexReader,matrix, normThreshold);
        else
            return new KLDivDocDistance(indexReader,matrix);
    }
}
