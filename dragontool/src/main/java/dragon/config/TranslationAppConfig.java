package dragon.config;

import dragon.ir.index.*;
import dragon.ir.kngbase.*;
import dragon.matrix.*;

/**
 * <p>Translation probability application configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TranslationAppConfig {
    public TranslationAppConfig() {
    }

    public static void main(String[] args) {
        TranslationAppConfig transApp;
        ConfigureNode root,transAppNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and translation applicaiton id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        transAppNode=util.getConfigureNode(root,"translationapp",Integer.parseInt(args[1]));
        if(transAppNode==null)
            return;
        transApp=new TranslationAppConfig();
        transApp.translate(transAppNode);
    }

    public void translate(ConfigureNode transNode){
        IntSparseMatrix cooccurMatrix;
        String indexSection, transMatrixKey;
        String srcIndexFolder,destIndexFolder;
        boolean relation,useEM, useDocFreq, useMeanTrim;
        double emBkgCoefficient, probThreshold;
        int minFrequency, matrixID;

        indexSection=transNode.getString("indexsection","all");
        srcIndexFolder=transNode.getString("srcindexfolder");
        destIndexFolder=transNode.getString("destindexfolder",srcIndexFolder);
        srcIndexFolder=srcIndexFolder+"/"+indexSection;
        destIndexFolder=destIndexFolder+"/"+indexSection;
        transMatrixKey=transNode.getString("translationmatrixkey");
        relation=transNode.getBoolean("relation",false);
        useEM=transNode.getBoolean("useem",true);
        useDocFreq=transNode.getBoolean("usedocfrequency",true);
        useMeanTrim=transNode.getBoolean("usemeantrim",false);
        minFrequency=transNode.getInt("minfrequency",2);
        emBkgCoefficient=transNode.getDouble("embkgcoefficient",0.5);
        probThreshold=transNode.getDouble("probthreshold",0.001);
        matrixID=transNode.getInt("cooccurrencematrix");
        if(matrixID>0)
            cooccurMatrix=(new SparseMatrixConfig()).getIntSparseMatrix(transNode,matrixID);
        else
            cooccurMatrix=null;
        translate(cooccurMatrix, srcIndexFolder,relation,minFrequency,destIndexFolder,transMatrixKey,useEM,useDocFreq,
                  useMeanTrim,probThreshold,emBkgCoefficient);
    }

    public void translate(IntSparseMatrix cooccurMatrix, String srcIndexFolder, boolean relation, int minFrequency, String destIndexFolder, String matrixKey,
                          boolean useEM, boolean useDocFreq, boolean useMeanTrim, double probThreshold, double emBkgCoefficient){
        TopicSignatureModel model;
        IntSparseMatrix srcMatrix, destMatrix;
        IRSignatureIndexList srcIndexList,destIndexList;

        if(relation)
            srcIndexList = new BasicIRTermIndexList(srcIndexFolder + "/relationindex.list", false);
        else
            srcIndexList = new BasicIRTermIndexList(srcIndexFolder + "/termindex.list", false);
        if(useEM)
            destIndexList = new BasicIRTermIndexList(destIndexFolder + "/termindex.list", false);
        else
            destIndexList=null;
        if(cooccurMatrix==null){
            if (relation)
                srcMatrix = new IntGiantSparseMatrix(srcIndexFolder + "/relationdoc.index", srcIndexFolder + "/relationdoc.matrix");
            else
                srcMatrix = new IntGiantSparseMatrix(srcIndexFolder + "/termdoc.index",srcIndexFolder + "/termdoc.matrix");
            destMatrix = new IntSuperSparseMatrix(destIndexFolder + "/docterm.index", destIndexFolder + "/docterm.matrix");
            model = new TopicSignatureModel(srcIndexList, srcMatrix, destIndexList, destMatrix);
        }
        else
            model = new TopicSignatureModel(srcIndexList, destIndexList, cooccurMatrix);
        model.setUseDocFrequency(useDocFreq);
        model.setUseEM(useEM);
        model.setUseMeanTrim(useMeanTrim);
        model.setProbThreshold(probThreshold);
        model.setEMBackgroundCoefficient(emBkgCoefficient);
        model.genTransMatrix(minFrequency,destIndexFolder,matrixKey);
    }
}