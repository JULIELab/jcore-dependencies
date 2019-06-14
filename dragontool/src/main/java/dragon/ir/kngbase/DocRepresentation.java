package dragon.ir.kngbase;

import dragon.ir.index.IndexReader;
import dragon.matrix.DoubleFlatSparseMatrix;
import dragon.matrix.DoubleSparseMatrix;
import dragon.matrix.DoubleSuperSparseMatrix;
import dragon.nlp.Token;
import dragon.util.MathUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * <p>A tool for converting document represenations</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocRepresentation {
    private IndexReader indexReader;
    private int[] termMap;
    private boolean showMessage;

    public DocRepresentation(IndexReader indexReader) {
        this.indexReader =indexReader;
        showMessage=true;
    }

    public DocRepresentation(IndexReader indexReader, int[] termMap) {
        this.indexReader =indexReader;
        this.termMap =termMap;
        showMessage=true;
    }

    public void setMessageOption(boolean showMessage){
        this.showMessage =showMessage;
    }

    public DoubleSparseMatrix genModelMatrix(IndexReader signatureIndexReader, DoubleSparseMatrix transMatrix, double transCoefficient,
                               double bkgCoefficient, boolean isPhraseSignature, double probThreshold, String matrixPath, String matrixKey){
        return genModelMatrix(signatureIndexReader,null,transMatrix,transCoefficient,bkgCoefficient,isPhraseSignature,probThreshold,matrixPath, matrixKey);
    }

    public DoubleSparseMatrix genModelMatrix(IndexReader signatureIndexReader, int[] signatureMap, DoubleSparseMatrix transMatrix, double transCoefficient,
                               double bkgCoefficient, boolean isPhraseSignature, double probThreshold, String matrixPath, String matrixKey){
        DoubleSuperSparseMatrix matrix;
        File file;
        String indexDir;

        indexDir=matrixPath;
        file=new File(indexDir + "/" + matrixKey+".index");
        if(file.exists()) file.delete();
        file=new File(indexDir + "/" + matrixKey+".matrix");
        if(file.exists()) file.delete();
        matrix = new DoubleSuperSparseMatrix(indexDir + "/" + matrixKey+".index",indexDir+"/"+matrixKey+".matrix",false,false);
        return genModelMatrix(signatureIndexReader,signatureMap,transMatrix,transCoefficient,bkgCoefficient,isPhraseSignature,probThreshold,matrix);
    }

    public DoubleSparseMatrix genModelMatrix(IndexReader signatureIndexReader, DoubleSparseMatrix transMatrix, double transCoefficient,
                               double bkgCoefficient, boolean isPhraseSignature, double probThreshold){
         return genModelMatrix(signatureIndexReader,null,transMatrix,transCoefficient,bkgCoefficient,isPhraseSignature,probThreshold);
    }

    public DoubleSparseMatrix genModelMatrix(IndexReader signatureIndexReader, int[] signatureMap, DoubleSparseMatrix transMatrix, double transCoefficient,
                               double bkgCoefficient, boolean isPhraseSignature, double probThreshold){
        DoubleFlatSparseMatrix matrix;

        matrix=new DoubleFlatSparseMatrix(false,false);
        return genModelMatrix(signatureIndexReader,signatureMap,transMatrix,transCoefficient,bkgCoefficient,isPhraseSignature,probThreshold,matrix);
    }

    private DoubleSparseMatrix genModelMatrix(IndexReader signatureIndexReader, int[] signatureMap, DoubleSparseMatrix transMatrix, double transCoefficient,
                               double bkgCoefficient, boolean isPhraseSignature, double probThreshold, DoubleSparseMatrix matrix){
        ArrayList termList;
        Token curToken;
        double[] arrBkgModel, arrTransModel, scores;
        int[] indexList, freqList,cols;
        int termNum, docNum, usedSignature, signatureNum;
        int i,j,k, curSignatureIndex;
        double weightSum, rate;

        docNum=indexReader.getCollection().getDocNum();
        if(termMap==null)
            termNum=indexReader.getCollection().getTermNum();
        else
            termNum=Math.max(transMatrix.columns(),MathUtil.max(termMap)+1);
        arrBkgModel=new double[indexReader.getCollection().getTermNum()];
        arrTransModel=new double[termNum];
        weightSum=indexReader.getCollection().getTermCount();
        for(i=0;i<arrBkgModel.length;i++)
            arrBkgModel[i]=indexReader.getIRTerm(i).getFrequency()/weightSum*(1-transCoefficient)*bkgCoefficient;


        termList = new ArrayList();
        indexList=null;
        freqList=null;
        for(i=0;i<docNum;i++){
            if(i>0 && i%2000==0){
               matrix.flush();
               if(showMessage)
                   System.out.println(new java.util.Date() + " processing doc #" + i);
           }
           if(indexReader.getDoc(i).getTermNum()<=0)
                continue;

            //add the translation model
            if(i>=signatureIndexReader.getCollection().getDocNum())
                signatureNum=0;
            else{
                for (j = 0; j < termNum; j++)
                    arrTransModel[j] = 0;
                if (isPhraseSignature) {
                    signatureNum=signatureIndexReader.getDoc(i).getTermNum();
                    indexList = signatureIndexReader.getTermIndexList(i);
                    freqList = signatureIndexReader.getTermFrequencyList(i);
                } else {
                    signatureNum=signatureIndexReader.getDoc(i).getRelationNum();
                    indexList = signatureIndexReader.getRelationIndexList(i);
                    freqList = signatureIndexReader.getRelationFrequencyList(i);
                }
            }

            usedSignature=0;
            weightSum=0;
            for(j=0;j<signatureNum;j++){
                if(signatureMap==null)
                    curSignatureIndex=indexList[j];
                else
                    curSignatureIndex=signatureMap[indexList[j]];
                if(curSignatureIndex>=transMatrix.rows()) break;
                cols=transMatrix.getNonZeroColumnsInRow(curSignatureIndex);
                scores=transMatrix.getNonZeroDoubleScoresInRow(curSignatureIndex);
                if(cols.length>0)
                    usedSignature++;
                rate=freqList[j];
                weightSum+=freqList[j];
                for(k=0;k<cols.length;k++){
                    arrTransModel[cols[k]]+=scores[k]*rate;
                }
            }
            if(usedSignature>0){
                rate =transCoefficient/usedSignature/weightSum;
                for(j=0;j<termNum;j++)
                    if(arrTransModel[j]>0)
                        arrTransModel[j]=arrTransModel[j]*rate;
            }

            //add unitgram document model
            indexList = indexReader.getTermIndexList(i);
            freqList = indexReader.getTermFrequencyList(i);
            weightSum=indexReader.getDoc(i).getTermCount();
            rate=(1-transCoefficient)*(1-bkgCoefficient)/weightSum;
            for(j=0;j<indexList.length;j++)
                arrTransModel[map(indexList[j])]+=freqList[j]*rate;

            //add background collection model
            for(j=0;j<arrBkgModel.length;j++)
                arrTransModel[map(j)]+=arrBkgModel[j];

            if(usedSignature==0){
                //if there is no translation, adjust the probability
                rate=1.0/(1-transCoefficient);
                for(j=0;j<termNum;j++)
                    arrTransModel[j]=arrTransModel[j]*rate;
            }

            termList.clear();
            weightSum=0;
            for(j=0;j<termNum;j++)
            {
                if(arrTransModel[j]>=probThreshold){
                    curToken=new Token(null);
                    curToken.setIndex(j);
                    curToken.setWeight(arrTransModel[j]);
                    termList.add(curToken);
                    weightSum+=arrTransModel[j];
                }
            }
            for(j=0;j<termList.size();j++){
                curToken=(Token)termList.get(j);
                matrix.add(i,curToken.getIndex(),curToken.getWeight()/weightSum);
            }
        }
        matrix.finalizeData();
        return matrix;
    }

    public DoubleSparseMatrix genTFIDFMatrix(String matrixPath,String matrixKey){
        DoubleSuperSparseMatrix tfidfMatrix;
        File file;
        String indexFolder;

        indexFolder=matrixPath;
        file = new File(indexFolder + "/" + matrixKey + ".index");
        if (file.exists()) file.delete();
        file = new File(indexFolder + "/" + matrixKey + ".matrix");
        if (file.exists()) file.delete();
        tfidfMatrix = new DoubleSuperSparseMatrix(indexFolder + "/"+matrixKey + ".index",indexFolder + "/"+matrixKey + ".matrix", false, false);
        return genTFIDFMatrix(tfidfMatrix);
    }

    public DoubleSparseMatrix genTFIDFMatrix(){
        DoubleFlatSparseMatrix matrix;

        matrix=new DoubleFlatSparseMatrix(false,false);
        return genTFIDFMatrix(matrix);
   }

   private DoubleSparseMatrix genTFIDFMatrix(DoubleSparseMatrix matrix){
       int[] termIndexList, termFreqList;
       double[] arrIDF;
       double sum;
       int i,j, docNum;

       arrIDF=new double[indexReader.getCollection().getTermNum()];
       sum=indexReader.getCollection().getDocNum();
       for(i=0;i<arrIDF.length;i++)
           arrIDF[i]=Math.log(sum/indexReader.getIRTerm(i).getDocFrequency());

       docNum=indexReader.getCollection().getDocNum();
       for(i=0;i<docNum;i++){
           if(i>0 && i%2000==0){
               matrix.flush();
               if(showMessage)
                   System.out.println(new java.util.Date() + " processing doc #" + i);
           }
           termIndexList = indexReader.getTermIndexList(i);
           termFreqList = indexReader.getTermFrequencyList(i);
           for(j=0;j<termIndexList.length;j++){
               matrix.add(i,map(termIndexList[j]),termFreqList[j]*arrIDF[termIndexList[j]]);
           }
       }
       matrix.finalizeData();
       return matrix;
   }

   public DoubleSparseMatrix genNormTFMatrix(String matrixPath, String matrixKey){
       DoubleSuperSparseMatrix matrix;
       File file;
       String indexFolder;

       indexFolder=matrixPath;
       file=new File(indexFolder + "/" + matrixKey+".index");
       if (file.exists()) file.delete();
       file = new File(indexFolder + "/" + matrixKey + ".matrix");
       if (file.exists())  file.delete();
       matrix = new DoubleSuperSparseMatrix(indexFolder + "/"+matrixKey+ ".index",indexFolder + "/"+matrixKey + ".matrix", false, false);
       return genNormTFMatrix(matrix);
   }

   public DoubleSparseMatrix genNormTFMatrix(){
        DoubleFlatSparseMatrix matrix;

        matrix=new DoubleFlatSparseMatrix(false,false);
        return genNormTFMatrix(matrix);
   }

   private DoubleSparseMatrix genNormTFMatrix(DoubleSparseMatrix matrix){
       int[] termIndexList, termFreqList;
       int i, j;
       double sum, docNum;

       docNum=indexReader.getCollection().getDocNum();
       for (i = 0; i <docNum; i++) {
           if(i>0 && i%2000==0){
               matrix.flush();
               if(showMessage)
                   System.out.println(new java.util.Date() + " processing doc #" + i);
           }
           termIndexList = indexReader.getTermIndexList(i);
           termFreqList = indexReader.getTermFrequencyList(i);
           sum=0;
           for (j = 0; j < termIndexList.length; j++) {
               sum+=(double) termFreqList[j]*termFreqList[j];
           }
           sum=Math.sqrt(sum);
           for(j=0;j<termIndexList.length;j++){
               matrix.add(i,map(termIndexList[j]),termFreqList[j]/sum);
           }
       }
       matrix.finalizeData();
       return matrix;
   }

   private int map(int oldTermIndex){
       if(termMap==null)
           return oldTermIndex;
       else
           return termMap[oldTermIndex];
   }
}