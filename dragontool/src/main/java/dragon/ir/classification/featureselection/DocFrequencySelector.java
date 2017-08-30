package dragon.ir.classification.featureselection;

import dragon.ir.classification.DocClassSet;
import dragon.ir.index.*;
import dragon.matrix.*;
import java.util.ArrayList;

/**
 * <p>Unsupervised Feature Selector which exclude features with its document frequency less than a given threshold</p>
 * <p>Please refer the paper below for details of the algorithm.<br>
 * Yang, Y. and Pedersen, J.O., "A comparative study on feature selection in text categorization,"
 * In Proceedings of International Conference on Machine Learning, 1997, pp. 412-420.
 * </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocFrequencySelector extends AbstractFeatureSelector implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int minDocFrequency;

    public DocFrequencySelector(int minDocFrequency) {
        this.minDocFrequency =minDocFrequency;
    }

    protected int[] getSelectedFeatures(SparseMatrix doctermMatrix, DocClassSet trainingSet){
        ArrayList list;
        int[] featureMap;
        int i,termNum;

        featureMap=getTermDocFrequency(doctermMatrix,trainingSet);
        termNum=featureMap.length;
        list=new ArrayList(termNum);
        for(i=0;i<termNum;i++){
            if(featureMap[i]>=minDocFrequency)
                list.add(new Integer(i));
        }
        
        featureMap=new int[list.size()];
        for(i=0;i<featureMap.length;i++)
            featureMap[i]=((Integer)list.get(i)).intValue();
        return featureMap;
    }

    protected int[] getSelectedFeatures(IndexReader indexReader, DocClassSet trainingSet){
        IntDenseMatrix termDistri;
        ArrayList list;
        IRTerm curTerm;
        int[] featureMap;
        int i,termNum;

        termDistri=getTermDistribution(indexReader,trainingSet);
        termNum=termDistri.columns();
        list=new ArrayList(termNum);
        for(i=0;i<termNum;i++){
            if(termDistri.getColumnSum(i)<=0)
                continue;
            curTerm=indexReader.getIRTerm(i);
            if(curTerm.getDocFrequency()>=minDocFrequency)
                list.add(curTerm);
        }
        featureMap=new int[list.size()];
        for(i=0;i<featureMap.length;i++)
            featureMap[i]=((IRTerm)list.get(i)).getIndex();
        return featureMap;
    }
}