package dragon.config;

import dragon.ir.index.BasicIRTermIndexList;
import dragon.ir.index.IRTermIndexList;
import dragon.ir.kngbase.CooccurrenceGenerator;
import dragon.matrix.IntSparseMatrix;

/**
 * <p>Co-occurence application configuration</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CooccurrenceAppConfig {
    public static void main(String[] args) {
        CooccurrenceAppConfig cooccurApp;
        ConfigureNode root,appNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and indexing applicaiton id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        appNode=util.getConfigureNode(root,"cooccurrenceapp",Integer.parseInt(args[1]));
        if(appNode==null)
            return;
        cooccurApp=new CooccurrenceAppConfig();
        cooccurApp.generateCooccurrenceMatrix(appNode);
    }

    public void generateCooccurrenceMatrix(ConfigureNode node){
        IntSparseMatrix matrixA, matrixB;
        String matrixFolder, matrixKey, indexListFileA, indexListFileB;
        int minDocFreq, maxDocFreq, matrixAID, matrixBID, cache;

        minDocFreq=node.getInt("mindocfrequency",1);
        maxDocFreq=node.getInt("maxdocfrequency",Integer.MAX_VALUE);
        cache=node.getInt("cache",5000000);
        matrixFolder=node.getString("cooccurrencematrixpath");
        matrixKey=node.getString("cooccurrencematrixkey");
        matrixAID=node.getInt("firstmatrix");
        matrixBID=node.getInt("secondmatrix",matrixAID);
        indexListFileA=node.getString("firstindexlistfile",null);
        indexListFileB=node.getString("secondindexlistfile",null);
        matrixA=(new SparseMatrixConfig()).getIntSparseMatrix(node,matrixAID);
        if(matrixAID==matrixBID)
            generateCooccurrenceMatrix(matrixA,indexListFileA,matrixFolder,matrixKey,cache, minDocFreq, maxDocFreq);
        else{
            matrixB=(new SparseMatrixConfig()).getIntSparseMatrix(node,matrixBID);
            generateCooccurrenceMatrix(matrixA,indexListFileA, matrixB, indexListFileB, matrixFolder,matrixKey,cache, minDocFreq, maxDocFreq);
        }
    }

    public void generateCooccurrenceMatrix(IntSparseMatrix doctermMatrixA, String indexListFileA, IntSparseMatrix doctermMatrixB,
                                           String indexListFileB, String matrixFolder, String matrixKey, int cache, int minDocFreq, int maxDocFreq){
        CooccurrenceGenerator generator;

        generator=new CooccurrenceGenerator();
        generator.setCacheSize(cache);
        generator.setMinDocFrequency(minDocFreq);
        generator.setMaxDocFrequency(maxDocFreq);
        generator.generate(doctermMatrixA, getTermDocFrequencyList(indexListFileA), doctermMatrixB,
                           getTermDocFrequencyList(indexListFileB),matrixFolder, matrixKey);
    }

    public void generateCooccurrenceMatrix(IntSparseMatrix doctermMatrix, String indexListFile, String matrixFolder, String matrixKey,
                                           int cache, int minDocFreq, int maxDocFreq){
        CooccurrenceGenerator generator;

        generator=new CooccurrenceGenerator();
        generator.setCacheSize(cache);
        generator.setMinDocFrequency(minDocFreq);
        generator.setMaxDocFrequency(maxDocFreq);
        generator.generate(doctermMatrix, getTermDocFrequencyList(indexListFile),matrixFolder, matrixKey);
    }

    public int[] getTermDocFrequencyList(String indexListFile){
        IRTermIndexList list;
        int i, arrDocFreq[];

        if(indexListFile==null)
            return null;
        list=new BasicIRTermIndexList(indexListFile,false);
        arrDocFreq=new int[list.size()];
        for(i=0;i<list.size();i++)
            arrDocFreq[i]=list.get(i).getDocFrequency();
        return arrDocFreq;
    }
}