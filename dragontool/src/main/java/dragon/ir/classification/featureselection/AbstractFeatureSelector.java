package dragon.ir.classification.featureselection;

import dragon.ir.classification.*;
import dragon.ir.index.*;
import dragon.matrix.*;
import dragon.matrix.vector.DoubleVector;
import dragon.util.*;

/**
 * <p>Abstract function class for feature selection</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractFeatureSelector implements FeatureSelector, java.io.Serializable  {
    protected int[] featureMap;
    protected int selectedFeatureNum;

    protected abstract int[] getSelectedFeatures(IndexReader indexReader, DocClassSet trainingSet);
    protected abstract int[] getSelectedFeatures(SparseMatrix doctermMatrix, DocClassSet trainingSet);

    public void train(IndexReader indexReader, DocClassSet trainingSet){
        setSelectedFeatures(getSelectedFeatures(indexReader,trainingSet));
    }

    public void train(SparseMatrix doctermMatrix, DocClassSet trainingSet){
        setSelectedFeatures(getSelectedFeatures(doctermMatrix,trainingSet));
    }

    public void setSelectedFeatures(int[] selectedFeatures){
        int i, oldFeatureNum;

        if(selectedFeatures==null)
            return;
        oldFeatureNum=selectedFeatures[selectedFeatures.length-1]+1;
        featureMap=new int[oldFeatureNum];
        MathUtil.initArray(featureMap,-1);
        for(i=0;i<selectedFeatures.length;i++)
            featureMap[selectedFeatures[i]]=i;
        selectedFeatureNum=selectedFeatures.length;
    }

    public boolean isSelected(int originalFeatureIndex){
        if(originalFeatureIndex>=featureMap.length)
            return false;
        else
            return featureMap[originalFeatureIndex]!=-1;
    }

    public int map(int originalFeatureIndex){
        if(originalFeatureIndex>=featureMap.length)
            return -1;
        else
            return featureMap[originalFeatureIndex];
    }

    public int getSelectedFeatureNum(){
        return selectedFeatureNum;
    }

    protected DoubleVector getClassPrior(DocClassSet docSet){
        DoubleVector vector;
        int i, sum;

        sum=docSet.getClassNum();
        vector=new DoubleVector(docSet.getClassNum());
        vector.assign(0);
        for(i=0;i<docSet.getClassNum();i++){
            vector.set(i,docSet.getDocClass(i).getDocNum());
            sum+=docSet.getDocClass(i).getDocNum();
        }
        for(i=0;i<docSet.getClassNum();i++)
            vector.set(i, vector.get(i)/sum);
        return vector;
    }

    protected int[] getTermDocFrequency(SparseMatrix matrix, DocClassSet trainingSet){
        DocClass curClass;
        IRDoc curDoc;
        int[] arrIndex, arrStat;
        int i, j, k;

        arrStat=new int[matrix.columns()];
        for(i=0;i<trainingSet.getClassNum();i++){
            curClass=trainingSet.getDocClass(i);
            for(j=0;j<curClass.getDocNum();j++){
                curDoc=curClass.getDoc(j);
                arrIndex=matrix.getNonZeroColumnsInRow(curDoc.getIndex());
                if(arrIndex==null || arrIndex.length==0) continue;
                for(k=0;k<arrIndex.length;k++){
                    arrStat[arrIndex[k]]+=1;
                }
            }
        }
        return arrStat;
    }
    
    protected IntDenseMatrix getTermDistribution(IndexReader indexReader, DocClassSet trainingSet){
       IntFlatDenseMatrix matrix;
       DocClass curClass;
       IRDoc curDoc;
       int[] arrIndex;
       int i, j, k;

       matrix=new IntFlatDenseMatrix(trainingSet.getClassNum(),indexReader.getCollection().getTermNum());
       matrix.assign(0);
       for(i=0;i<trainingSet.getClassNum();i++){
           curClass=trainingSet.getDocClass(i);
           for(j=0;j<curClass.getDocNum();j++){
               curDoc=curClass.getDoc(j);
               arrIndex=indexReader.getTermIndexList(curDoc.getIndex());
               if(arrIndex==null || arrIndex.length==0) continue;
               for(k=0;k<arrIndex.length;k++){
                   matrix.add(i, arrIndex[k], 1);
               }
           }
       }
       return matrix;
   }

    protected IntDenseMatrix getTermDistribution(SparseMatrix doctermMatrix, DocClassSet trainingSet){
        IntFlatDenseMatrix matrix;
        DocClass curClass;
        IRDoc curDoc;
        int[] arrIndex;
        int i, j, k;

        matrix=new IntFlatDenseMatrix(trainingSet.getClassNum(),doctermMatrix.columns());
        matrix.assign(0);
        for(i=0;i<trainingSet.getClassNum();i++){
            curClass=trainingSet.getDocClass(i);
            for(j=0;j<curClass.getDocNum();j++){
                curDoc=curClass.getDoc(j);
                arrIndex=doctermMatrix.getNonZeroColumnsInRow(curDoc.getIndex());
                if(arrIndex==null || arrIndex.length==0) continue;
                for(k=0;k<arrIndex.length;k++){
                    matrix.add(i, arrIndex[k], 1);
                }
            }
        }
        return matrix;
    }
}