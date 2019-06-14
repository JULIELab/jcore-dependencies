package dragon.config;

import dragon.ir.index.BasicIndexReader;
import dragon.ir.index.IRCollection;
import dragon.ir.index.IndexReader;
import dragon.ir.search.smooth.*;
import dragon.matrix.DoubleSparseMatrix;

/**
 * <p>Semantic smoothing configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SmootherConfig extends ConfigUtil{
    public SmootherConfig() {
       super();
    }

    public SmootherConfig(ConfigureNode root){
       super(root);
    }

    public SmootherConfig(String configFile){
        super(configFile);
    }

    public Smoother getSmoother(int smootherID){
        return getSmoother(root,smootherID);
    }

    public Smoother getSmoother(ConfigureNode node, int smootherID){
        return loadSmoother(node,smootherID);
    }

    private Smoother loadSmoother(ConfigureNode node, int smootherID){
        ConfigureNode smootherNode;
        String smootherName;

        smootherNode=getConfigureNode(node,"smoother",smootherID);
        if(smootherNode==null)
            return null;
        smootherName=smootherNode.getNodeName();
        return loadSmoother(smootherName,smootherNode);
    }

    protected Smoother loadSmoother(String smootherName,ConfigureNode smootherNode){
        if(smootherName.equalsIgnoreCase("JMSmoother"))
            return loadJMSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("DirichletSmoother"))
            return loadDirichletSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("AbsoluteDiscountSmoother"))
            return loadAbsoluteDiscountSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("TwoStageSmoother"))
            return loadTwoStageSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("OkapiSmoother"))
            return loadOkapiSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("TFIDFSmoother"))
            return loadTFIDFSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("PivotedNormSmoother"))
            return loadPivotedNormSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("QueryFirstTransSmoother"))
            return loadQueryFirstTransSmoother(smootherNode);
        else if(smootherName.equalsIgnoreCase("DocFirstTransSmoother"))
            return loadDocFirstTransSmoother(smootherNode);
        else
            return (Smoother)loadResource(smootherNode);
    }

    private Smoother loadJMSmoother(ConfigureNode node){
        IRCollection collection;
        double bkgCoefficient;
        int collectionStatID;

        collectionStatID=node.getInt("collectionstat");
        collection=(new IndexReaderConfig()).getIRCollectionStat(node,collectionStatID);
        bkgCoefficient=node.getDouble("bkgcoefficient",0.5);
        return new JMSmoother(collection,bkgCoefficient);
    }

    private Smoother loadDirichletSmoother(ConfigureNode node){
        IRCollection collection;
        double dirichletCoefficient;
        int collectionStatID;

        collectionStatID=node.getInt("collectionstat");
        collection=(new IndexReaderConfig()).getIRCollectionStat(node,collectionStatID);
        dirichletCoefficient=node.getDouble("dirichletcoefficient",1000);
        return new DirichletSmoother(collection,dirichletCoefficient);
    }

    private Smoother loadAbsoluteDiscountSmoother(ConfigureNode node){
        IRCollection collection;
        double absoluteDiscount;
        int collectionStatID;

        collectionStatID=node.getInt("collectionstat");
        collection=(new IndexReaderConfig()).getIRCollectionStat(node,collectionStatID);
        absoluteDiscount=node.getDouble("absolutediscount",0.6);
        return new DirichletSmoother(collection,absoluteDiscount);
    }

    private Smoother loadTwoStageSmoother(ConfigureNode node){
        IRCollection collection;
        double dirichletCoefficient;
        double bkgCoefficient;
        int collectionStatID;

        collectionStatID=node.getInt("collectionstat");
        collection=(new IndexReaderConfig()).getIRCollectionStat(node,collectionStatID);
        dirichletCoefficient=node.getDouble("dirichletcoefficient",1000);
        bkgCoefficient=node.getDouble("bkgcoefficient",0.5);
        return new TwoStageSmoother(collection, bkgCoefficient,dirichletCoefficient);
    }

    private Smoother loadPivotedNormSmoother(ConfigureNode node){
        IRCollection collection;
        double s;
        int collectionStatID;

        collectionStatID=node.getInt("collectionstat");
        collection=(new IndexReaderConfig()).getIRCollectionStat(node,collectionStatID);
        s=node.getDouble("s",0.2);
        return new PivotedNormSmoother(collection,s);
    }

    private Smoother loadOkapiSmoother(ConfigureNode node){
        IRCollection collection;
        double bm25k1,bm25b;
        int collectionStatID;

        collectionStatID=node.getInt("collectionstat");
        collection=(new IndexReaderConfig()).getIRCollectionStat(node,collectionStatID);
        bm25k1=node.getDouble("bm25k1",2.0);
        bm25b=node.getDouble("bm25b",0.75);
        return new OkapiSmoother(collection,bm25k1, bm25b);
    }

    private Smoother loadTFIDFSmoother(ConfigureNode node){
        IRCollection collection;
        boolean useBM25;
        double bm25k1,bm25b;
        int collectionStatID;

        collectionStatID=node.getInt("collectionstat");
        collection=(new IndexReaderConfig()).getIRCollectionStat(node,collectionStatID);
        useBM25=node.getBoolean("usebm25",false);
        if(useBM25){
            bm25k1 = node.getDouble("bm25k1", 2.0);
            bm25b = node.getDouble("bm25b", 0.75);
            return new TFIDFSmoother(collection, bm25k1, bm25b);
        }
        else{
            return new TFIDFSmoother(collection);
        }
    }

    private Smoother loadQueryFirstTransSmoother(ConfigureNode node){
        IndexReaderConfig config;
        BasicIndexReader srcIndexReader,destIndexReader;
        Smoother basicSmoother;
        DoubleSparseMatrix transMatrix;
        boolean relationTrans;
        int smootherID, srcIndexReaderID, destIndexReaderID, matrixID;
        double transCoefficient;

        config=new IndexReaderConfig();
        srcIndexReaderID=node.getInt("srcindexreader");
        destIndexReaderID=node.getInt("destindexreader");
        srcIndexReader=(BasicIndexReader)config.getIndexReader(node,srcIndexReaderID);
        if(destIndexReaderID==srcIndexReaderID)
            destIndexReader=srcIndexReader;
        else
            destIndexReader=(BasicIndexReader)config.getIndexReader(node,destIndexReaderID);
        transCoefficient=node.getDouble("transcoefficient");
        matrixID=node.getInt("transposedtransmatrix");
        smootherID=node.getInt("basicsmoother");
        basicSmoother=getSmoother(node,smootherID);
        transMatrix=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID);
        if(srcIndexReaderID==destIndexReaderID){
            relationTrans=node.getBoolean("relationtrans",true);
            return new QueryFirstTransSmoother(srcIndexReader,transMatrix,relationTrans,transCoefficient,basicSmoother);
        }
        else{
            return new QueryFirstTransSmoother(srcIndexReader,destIndexReader,transMatrix,transCoefficient,basicSmoother);
        }
    }

    private Smoother loadDocFirstTransSmoother(ConfigureNode node){
        IndexReaderConfig config;
        IndexReader srcIndexReader,destIndexReader;
        Smoother basicSmoother;
        DoubleSparseMatrix transMatrix;
        boolean relationTrans;
        int smootherID, srcIndexReaderID, destIndexReaderID, matrixID;
        double transCoefficient;

        config=new IndexReaderConfig();
        srcIndexReaderID=node.getInt("srcindexreader");
        destIndexReaderID=node.getInt("destindexreader");
        srcIndexReader=config.getIndexReader(node,srcIndexReaderID);
        if(destIndexReaderID==srcIndexReaderID)
            destIndexReader=srcIndexReader;
        else
            destIndexReader=config.getIndexReader(node,destIndexReaderID);
        transCoefficient=node.getDouble("transcoefficient");
        matrixID=node.getInt("transmatrix");
        smootherID=node.getInt("basicsmoother");
        basicSmoother=getSmoother(node,smootherID);
        transMatrix=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID);
        if(srcIndexReaderID==destIndexReaderID){
            relationTrans=node.getBoolean("relationtrans",true);
            return new DocFirstTransSmoother(srcIndexReader,transMatrix,relationTrans,transCoefficient,basicSmoother);
        }
        else{
            return new QueryFirstTransSmoother(srcIndexReader,destIndexReader,transMatrix,transCoefficient,basicSmoother);
        }
    }
}
