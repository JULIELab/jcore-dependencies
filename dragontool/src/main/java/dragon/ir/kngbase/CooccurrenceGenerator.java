package dragon.ir.kngbase;

import dragon.matrix.*;
import dragon.nlp.*;
import java.util.*;
import java.io.*;
/**
 * <p>Generate Cooccurrence Matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang, Davis Zhou
 * @version 1.0
 */

public class CooccurrenceGenerator {
    private HashMap  hashMap;
    private int cacheSize;
    private int minDocFreq, maxDocFreq;

    public CooccurrenceGenerator() {
        hashMap = new HashMap();
        cacheSize=3000000;
        minDocFreq=1;
        maxDocFreq=Integer.MAX_VALUE;
    }

    public void setMinDocFrequency(int minDocFreq){
        this.minDocFreq =minDocFreq;
    }

    public int getMinDocFrequency(){
        return minDocFreq;
    }

    public void setMaxDocFrequency(int maxDocFreq){
        this.maxDocFreq =maxDocFreq;
    }

    public int getMaxDocFrequency(){
        return maxDocFreq;
    }

    public void setCacheSize(int size){
        this.cacheSize =size;
    }

    public int getCacheSize(){
        return cacheSize;
    }

    public boolean generate(IntSparseMatrix doctermMatrixA, IntSparseMatrix doctermMatrixB,String matrixFolder, String matrixKey){
        return generate(doctermMatrixA,null,doctermMatrixB,null,matrixFolder,matrixKey);
    }

    public boolean generate(IntSparseMatrix doctermMatrixA, int[] termDocFreqA, IntSparseMatrix doctermMatrixB, int[] termDocFreqB,
                            String matrixFolder, String matrixKey) {
        SimplePair coOccurPair;
        IntSuperSparseMatrix cooccurMatrix;
        SparseMatrixFactory smf;
        Counter counter;
        File file;
        String tmpMatrixFile, tmpIndexFile, matrixFile, indexFile;
        int i, j, k, docNum, indexListA[], indexListB[];
        boolean isFirst, usedListA[], usedListB[];

        try{
            usedListA=checkTermDocFrequency(doctermMatrixA.columns(),termDocFreqA);
            usedListB=checkTermDocFrequency(doctermMatrixB.columns(),termDocFreqB);

            matrixFile=matrixFolder+"/"+matrixKey+".matrix";
            indexFile = matrixFolder + "/" + matrixKey + ".index";
            file = new File(matrixFile);
            if (file.exists()) file.delete();
            file = new File(indexFile);
            if (file.exists()) file.delete();

            cooccurMatrix = new IntSuperSparseMatrix(indexFile, matrixFile, false, false);
            smf = null;
            tmpMatrixFile = matrixFolder + "/" + matrixKey + "tmp.matrix";
            tmpIndexFile = matrixFolder + "/" + matrixKey + "tmp.index";
            docNum = Math.min(doctermMatrixA.rows(),doctermMatrixB.rows());
            isFirst = true;
            SimplePair.setHashCapacity(doctermMatrixA.columns());
            for (i = 0; i < docNum; i++) {
                if (i % 1000 == 0)
                    System.out.println(new Date() + " " + i);
                if (hashMap.size()>=cacheSize) {
                    if (isFirst) {
                        convertPairsToMatrix(hashMap, cooccurMatrix,false);
                        isFirst = false;
                        smf = new SparseMatrixFactory(matrixFolder + "/" + matrixKey + ".matrix", 4);
                    } else {
                        cooccurMatrix = new IntSuperSparseMatrix(tmpIndexFile, tmpMatrixFile, false, false);
                        convertPairsToMatrix(hashMap, cooccurMatrix,false);
                        smf.add(cooccurMatrix);
                        cooccurMatrix.close();
                        (new File(tmpIndexFile)).delete();
                        (new File(tmpMatrixFile)).delete();
                    }
                }

                indexListA = doctermMatrixA.getNonZeroColumnsInRow(i);
                indexListB = doctermMatrixB.getNonZeroColumnsInRow(i);
                for (j = 0; j < indexListA.length; j++) {
                    if(!usedListA[indexListA[j]]) continue;
                    for (k =0; k < indexListB.length; k++) {
                        if(!usedListB[indexListB[k]]) continue;
                        coOccurPair = new SimplePair( -1, indexListA[j], indexListB[k]);
                        counter = (Counter) hashMap.get(coOccurPair);
                        if(counter!=null)
                            counter.addCount(1);
                        else
                            hashMap.put(coOccurPair, new Counter(1));
                    }
                }
            }

            if (hashMap.size() > 0) {
                if (isFirst) {
                    convertPairsToMatrix(hashMap, cooccurMatrix,false);
                }
                else {
                    cooccurMatrix = new IntSuperSparseMatrix(tmpIndexFile, tmpMatrixFile, false, false);
                    convertPairsToMatrix(hashMap, cooccurMatrix,false);
                    smf.add(cooccurMatrix);
                    cooccurMatrix.close();
                    new File(tmpIndexFile).delete();
                    new File(tmpMatrixFile).delete();
                    smf.genIndexFile(matrixFolder + "/" + matrixKey + ".index");
                }
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean generate(IntSparseMatrix doctermMatrix, String matrixFolder, String matrixKey){
        return generate(doctermMatrix,new int[0],matrixFolder,matrixKey);
    }

    public boolean generate(IntSparseMatrix doctermMatrix, int[] termDocFreq, String matrixFolder, String matrixKey) {
        SimplePair coOccurPair;
        IntSuperSparseMatrix cooccurMatrix;
        SparseMatrixFactory smf;
        Counter counter;
        File file;
        String tmpMatrixFile, tmpIndexFile, matrixFile, indexFile;
        int i, j, k, indexList[];
        boolean isFirst, usedList[];

        try{
            usedList=checkTermDocFrequency(doctermMatrix.columns(),termDocFreq);

            matrixFile=matrixFolder+"/"+matrixKey+".matrix";
            indexFile = matrixFolder + "/" + matrixKey + ".index";
            file = new File(matrixFile);
            if (file.exists()) file.delete();
            file = new File(indexFile);
            if (file.exists()) file.delete();

            cooccurMatrix = new IntSuperSparseMatrix(indexFile, matrixFile, false, false);
            smf = null;
            tmpMatrixFile = matrixFolder + "/" + matrixKey + "tmp.matrix";
            tmpIndexFile = matrixFolder + "/" + matrixKey + "tmp.index";
            isFirst = true;
            SimplePair.setHashCapacity(doctermMatrix.rows());
            for (i = 0; i < doctermMatrix.rows(); i++) {
                if (i % 1000 == 0)
                    System.out.println(new Date() + " " + i);
                if (hashMap.size()>=cacheSize) {
                    if (isFirst) {
                        convertPairsToMatrix(hashMap, cooccurMatrix,true);
                        isFirst = false;
                        smf = new SparseMatrixFactory(matrixFolder + "/" + matrixKey + ".matrix", 4);
                    } else {
                        cooccurMatrix = new IntSuperSparseMatrix(tmpIndexFile, tmpMatrixFile, false, false);
                        convertPairsToMatrix(hashMap, cooccurMatrix,true);
                        smf.add(cooccurMatrix);
                        cooccurMatrix.close();
                        (new File(tmpIndexFile)).delete();
                        (new File(tmpMatrixFile)).delete();
                    }
                }

                indexList = doctermMatrix.getNonZeroColumnsInRow(i);
                for (j = 0; j < indexList.length; j++) {
                    if(!usedList[indexList[j]])
                        continue;
                    for (k = j + 1; k < indexList.length; k++) {
                        if(!usedList[indexList[k]])
                            continue;
                        coOccurPair = new SimplePair( -1, indexList[j], indexList[k]);
                        counter = (Counter) hashMap.get(coOccurPair);
                        if(counter!=null)
                            counter.addCount(1);
                        else
                            hashMap.put(coOccurPair, new Counter(1));
                    }
                }
            }

            if (hashMap.size() > 0) {
                if (isFirst) {
                    convertPairsToMatrix(hashMap, cooccurMatrix,true);
                }
                else {
                    cooccurMatrix = new IntSuperSparseMatrix(tmpIndexFile, tmpMatrixFile, false, false);
                    convertPairsToMatrix(hashMap, cooccurMatrix,true);
                    smf.add(cooccurMatrix);
                    cooccurMatrix.close();
                    new File(tmpIndexFile).delete();
                    new File(tmpMatrixFile).delete();
                    smf.genIndexFile(matrixFolder + "/" + matrixKey + ".index");
                }
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void convertPairsToMatrix(HashMap hashMap, IntSuperSparseMatrix matrix, boolean symmetric) {
        SimplePair coOccurPair;
        Set keySet;
        Iterator iterator;
        Counter counter;

        if(symmetric)
            matrix.setFlushInterval(2*hashMap.size()+1);
        else
            matrix.setFlushInterval(hashMap.size()+1);
        keySet = hashMap.keySet();
        iterator = keySet.iterator();
        while (iterator.hasNext()) {
            coOccurPair = (SimplePair) iterator.next();
            counter = (Counter) hashMap.get(coOccurPair);
            matrix.add(coOccurPair.getFirstElement(), coOccurPair.getSecondElement(), counter.getCount());
            if(symmetric && coOccurPair.getFirstElement()!=coOccurPair.getSecondElement())
                matrix.add(coOccurPair.getSecondElement(), coOccurPair.getFirstElement(), counter.getCount());
        }
        matrix.finalizeData();
        hashMap.clear();
    }

    private boolean[] checkTermDocFrequency(int termNum, int[] arrTermDocFreq){
        boolean[] usedList;
        int i;

        usedList=new boolean[termNum];
        for(i=0;i<termNum;i++)
            usedList[i]=true;
        if(arrTermDocFreq==null || arrTermDocFreq.length==0 || minDocFreq<=1)
            return usedList;
        for(i=0;i<termNum;i++)
            if(arrTermDocFreq[i]<minDocFreq || arrTermDocFreq[i]>maxDocFreq)
                usedList[i]=false;
        return usedList;
    }
}
