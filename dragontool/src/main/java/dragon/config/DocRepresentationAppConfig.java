package dragon.config;

import dragon.ir.index.*;
import dragon.ir.kngbase.DocRepresentation;
import dragon.matrix.DoubleSparseMatrix;

/**
 * <p>Document representation application configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocRepresentationAppConfig {
    public DocRepresentationAppConfig() {
    }

    public static void main(String[] args) {
        DocRepresentationAppConfig app;
        ConfigureNode root,appNode;
        ConfigUtil util;
        String appName;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and document representation id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
       appNode=util.getConfigureNode(root,"docrepresentationapp",Integer.parseInt(args[1]));
        if(appNode==null)
            return;
        app=new DocRepresentationAppConfig();
        appName=appNode.getNodeName();
        if(appName.equalsIgnoreCase("ModelDocRepresentationApp"))
            app.genModelMatrix(appNode);
        else if(appName.equalsIgnoreCase("TFIDFDocRepresentationApp"))
            app.genTFIDFMatrix(appNode);
        else if(appName.equalsIgnoreCase("NormTFDocRepresentationApp"))
            app.genNormTFMatrix(appNode);
        else
            return;
    }

    public void genModelMatrix(ConfigureNode node){
        DocRepresentation docRepresentation;
        IndexReader indexReader,topicIndexReader;
        DoubleSparseMatrix transMatrix;
        String matrixKey, matrixPath;
        int indexReaderID, topicIndexReaderID, transMatrixID;
        double bkgCoefficient, transCoefficient, probThreshold;
        boolean isPhraseSignature;

        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        docRepresentation=new DocRepresentation(indexReader);
        topicIndexReaderID=node.getInt("topicindexreader");
        topicIndexReader=(new IndexReaderConfig()).getIndexReader(node,topicIndexReaderID);
        transMatrixID=node.getInt("transmatrix");
        transMatrix=(new SparseMatrixConfig()).getDoubleSparseMatrix(node,transMatrixID);
        bkgCoefficient=node.getDouble("bkgcoefficient");
        transCoefficient=node.getDouble("transcoefficient");
        probThreshold=node.getDouble("probthreshold");
        isPhraseSignature=node.getBoolean("phrasesignature",true);
        matrixKey=node.getString("matrixkey","doctermtrans");
        matrixPath=node.getString("matrixpath");
        docRepresentation.genModelMatrix(topicIndexReader,transMatrix,transCoefficient, bkgCoefficient, isPhraseSignature, probThreshold, matrixPath,matrixKey);
    }

    public void genTFIDFMatrix(ConfigureNode node){
        DocRepresentation docRepresentation;
        IndexReader indexReader;
        String matrixKey, matrixPath;
        int indexReaderID;

        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        docRepresentation=new DocRepresentation(indexReader);
        matrixPath=node.getString("matrixpath");
        matrixKey=node.getString("matrixkey","doctermtfidf");
        docRepresentation.genTFIDFMatrix(matrixPath,matrixKey);
    }

    public void genNormTFMatrix(ConfigureNode node){
        DocRepresentation docRepresentation;
        IndexReader indexReader;
        String matrixKey, matrixPath;
        int indexReaderID;

        indexReaderID = node.getInt("indexreader");
        indexReader = (new IndexReaderConfig()).getIndexReader(node, indexReaderID);
        docRepresentation=new DocRepresentation(indexReader);
        matrixKey = node.getString("matrixkey", "doctermnormtf");
        matrixPath = node.getString("matrixpath");
        docRepresentation.genNormTFMatrix(matrixPath, matrixKey);
   }
}