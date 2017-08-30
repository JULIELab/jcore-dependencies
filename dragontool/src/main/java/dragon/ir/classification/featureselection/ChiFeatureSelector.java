package dragon.ir.classification.featureselection;

import dragon.ir.classification.DocClassSet;
import dragon.ir.index.*;
import dragon.matrix.*;
import dragon.matrix.vector.DoubleVector;
import dragon.nlp.Token;
import dragon.nlp.compare.*;
import dragon.util.SortedArray;

/**
 * <p>CHI Feature Selector which uses the statistics CHI to select top features</p>
 * * <p>Please refer the paper below for details of the algorithm.<br>
 * Yang, Y. and Pedersen, J.O., <em>A comparative study on feature selection in text categorization</em>,
 * In Proceedings of International Conference on Machine Learning, 1997, pp. 412-420.
 * </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ChiFeatureSelector extends AbstractFeatureSelector implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private double topPercentage;
    private boolean avgMode;

    public ChiFeatureSelector(double topPercentage, boolean avgMode) {
        this.topPercentage =topPercentage;
        this.avgMode =avgMode;
    }

    protected int[] getSelectedFeatures(IndexReader indexReader, DocClassSet trainingSet){
        SortedArray list,selectedList;
        DoubleVector classPrior;
        int[] featureMap;
        int i,termNum, docNum;

        classPrior=getClassPrior(trainingSet);
        docNum=0;
        for(i=0;i<trainingSet.getClassNum();i++)
            docNum+=trainingSet.getDocClass(i).getDocNum();
        list=computeTermCHI(getTermDistribution(indexReader,trainingSet),classPrior,docNum);
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

    protected int[] getSelectedFeatures(SparseMatrix doctermMatrix, DocClassSet trainingSet){
        SortedArray list,selectedList;
        DoubleVector classPrior;
        int[] featureMap;
        int i,termNum, docNum;

        classPrior=getClassPrior(trainingSet);
        docNum=0;
        for(i=0;i<trainingSet.getClassNum();i++)
            docNum+=trainingSet.getDocClass(i).getDocNum();
        list=computeTermCHI(getTermDistribution(doctermMatrix,trainingSet),classPrior,docNum);
        termNum=(int)(topPercentage*doctermMatrix.columns());
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

    private SortedArray computeTermCHI(IntDenseMatrix termDistri, DoubleVector classPrior, int docNum){
        DoubleVector termVector, classVector, chiVector;
        SortedArray list;
        Token curTerm;
        double total;
        int i, j;

        classVector=classPrior.copy();
        classVector.multiply(docNum);

        termVector=new DoubleVector(termDistri.columns());
        for(i=0;i<termDistri.columns();i++)
            termVector.set(i,termDistri.getColumnSum(i));

        total=docNum;
        chiVector=new DoubleVector(classVector.size());
        list=new SortedArray(termVector.size(),new IndexComparator());
        for(i=0;i<termVector.size();i++){
            if(termVector.get(i)<=0) //this term does not exist in the training documents
                continue;
            for(j=0;j<classVector.size();j++){
                 chiVector.set(j,calChiSquare(termDistri.getInt(j,i),classVector.get(j),termVector.get(i),total));
            }
            curTerm=new Token(i,0);
            if(avgMode)
                curTerm.setWeight(chiVector.dotProduct(classPrior));
            else
                curTerm.setWeight(chiVector.getMaxValue());
            list.add(curTerm);
        }
        list.setComparator(new WeightComparator(true));
        return list;
    }

    private double calChiSquare(double t1t2occur, double t1sum, double t2sum, double total){
        double chi;
        double expectedValue;
        double observedValue;

        if(t1sum==0 || t2sum==0) return 0;

        expectedValue = t1sum*t2sum/total;
        observedValue=t1t2occur;
        chi=(observedValue-expectedValue)*(observedValue-expectedValue)/expectedValue;

        expectedValue=t1sum*(total-t2sum)/total;
        observedValue=t1sum-t1t2occur;
        chi+=(observedValue-expectedValue)*(observedValue-expectedValue)/expectedValue;

        expectedValue=(total-t1sum)*t2sum/total;
        observedValue=t2sum-t1t2occur;
        chi+=(observedValue-expectedValue)*(observedValue-expectedValue)/expectedValue;

        expectedValue=(total-t1sum)*(total-t2sum)/total;
        observedValue=total-t1sum-t2sum+t1t2occur;
        chi+=(observedValue-expectedValue)*(observedValue-expectedValue)/expectedValue;;

        return chi;
    }
}