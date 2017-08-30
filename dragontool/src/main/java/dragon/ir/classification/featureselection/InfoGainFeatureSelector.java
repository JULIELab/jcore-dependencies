package dragon.ir.classification.featureselection;

import dragon.ir.classification.*;
import dragon.ir.classification.DocClassSet;
import dragon.ir.index.*;
import dragon.matrix.SparseMatrix;
import dragon.matrix.vector.DoubleVector;
import dragon.nlp.Token;
import dragon.nlp.compare.*;
import dragon.util.*;

/**
 * <p>A Feature Selector which uses information gain to select top features</p>
 * <p>Please refer the paper below for details of the algorithm.<br>
 * Yang, Y. and Pedersen, J.O., <em>A comparative study on feature selection in text categorization</em>,
 * In Proceedings of International Conference on Machine Learning, 1997, pp. 412-420.
 * </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class InfoGainFeatureSelector extends AbstractFeatureSelector implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private double topPercentage;

    public InfoGainFeatureSelector(double topPercentage) {
        this.topPercentage =topPercentage;
    }

    protected int[] getSelectedFeatures(SparseMatrix doctermMatrix, DocClassSet trainingSet){
        System.out.println("InfoGainSelector does not accept SparseMatrix as input. Please use IndexReader as input instead.");
        return null;
    }

    protected int[] getSelectedFeatures(IndexReader indexReader, DocClassSet trainingSet){
        SortedArray list,selectedList;
        int[] featureMap;
        int i,termNum;

        list=computeTermIG(indexReader,trainingSet);
        termNum=(int)(topPercentage*indexReader.getCollection().getTermNum());
        termNum=Math.min(list.size(),termNum);
        selectedList=new SortedArray(termNum,new IndexComparator());
        for(i=0;i<termNum;i++){
            selectedList.add(list.get(i));
        }
        featureMap=new int[selectedList.size()];
        for(i=0;i<featureMap.length;i++)
            featureMap[i]=((Token)selectedList.get(i)).getIndex();
        return featureMap;
    }

    private SortedArray computeTermIG(IndexReader indexReader, DocClassSet trainingSet){
        DoubleVector termVector, classVector, classPrior, classDistrWiTerm, classDistrWoTerm;
        DocClass docClass;
        SortedArray list;
        Token curTerm;
        int[] arrDoc, arrDocIndex;
        double classEntropy;
        int termNum, trainingDocNum, docCount, docLabel, i, j;

        trainingDocNum=0;
        for(i=0;i<trainingSet.getClassNum();i++)
            trainingDocNum+=trainingSet.getDocClass(i).getDocNum();

        classPrior=getClassPrior(trainingSet);
        classEntropy=calEntropy(classPrior);

        classVector=classPrior.copy();
        classVector.multiply(trainingDocNum);

        arrDoc=new int[indexReader.getCollection().getDocNum()];
        MathUtil.initArray(arrDoc,-1);
        for(i=0;i<trainingSet.getClassNum();i++){
            docClass=trainingSet.getDocClass(i);
            for(j=0;j<docClass.getDocNum();j++)
                arrDoc[docClass.getDoc(j).getIndex()]=i;
        }

        termNum=indexReader.getCollection().getTermNum();
        list=new SortedArray(termNum,new IndexComparator());
        termVector=new DoubleVector(termNum);
        classDistrWiTerm=new DoubleVector(classPrior.size());
        classDistrWoTerm=new DoubleVector(classPrior.size());
        for(i=0;i<termNum;i++){
            arrDocIndex=indexReader.getTermDocIndexList(i);
            if(arrDocIndex==null || arrDocIndex.length==0)
                continue;
            classDistrWiTerm.assign(0);
            classDistrWoTerm.assign(classVector);
            docCount=0;
            for(j=0;j<arrDocIndex.length;j++){
                docLabel=arrDoc[arrDocIndex[j]];
                if(docLabel>=0){
                    //this document is in the training set
                    classDistrWiTerm.add(docLabel,1);
                    classDistrWoTerm.add(docLabel,-1);
                    docCount++;
                }
            }
            if(docCount==0)
                continue;

            classDistrWiTerm.multiply(1.0/docCount);
            classDistrWoTerm.multiply(1.0/(trainingDocNum-docCount));
            termVector.set(i,classEntropy-calEntropy(classDistrWiTerm)-calEntropy(classDistrWoTerm));
        }

        for(i=0;i<termVector.size();i++){
            curTerm=new Token(i,0);
            if(termVector.get(i)<=0) //this term does not exist in the training documents
                continue;
            curTerm.setWeight(termVector.get(i));
            list.add(curTerm);
        }
        list.setComparator(new WeightComparator(true));
        return list;
    }

    private double calEntropy(DoubleVector probVector){
       double sum;
       int i;

       sum=0;
       for(i=0;i<probVector.size();i++){
           if(probVector.get(i)==0)
               sum-=Double.MIN_VALUE*Math.log(Double.MIN_VALUE);
           else
               sum -= probVector.get(i) * Math.log(probVector.get(i));
       }
        return sum;
    }
}
