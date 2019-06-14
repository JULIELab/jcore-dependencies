package dragon.ir.classification;

import dragon.ir.classification.featureselection.DocFrequencySelector;
import dragon.ir.classification.featureselection.FeatureSelector;
import dragon.ir.classification.featureselection.NullFeatureSelector;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.matrix.DoubleRow;
import dragon.matrix.IntRow;
import dragon.matrix.Row;
import dragon.matrix.SparseMatrix;

/**
 * <p>Basic function class for classifying</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractClassifier implements Classifier{
    protected IndexReader indexReader;
    protected SparseMatrix doctermMatrix;
    protected DocClassSet validatingDocSet;
    protected FeatureSelector featureSelector;
    protected String[] arrLabel;
    protected int classNum;

    public AbstractClassifier(IndexReader indexReader) {
        this.featureSelector =new DocFrequencySelector(1);
        this.indexReader =indexReader;
        this.doctermMatrix =null;
        validatingDocSet =null;
        arrLabel=null;
    }

    public AbstractClassifier(SparseMatrix doctermMatrix) {
        this.featureSelector =new DocFrequencySelector(1);
        this.indexReader =null;
        this.doctermMatrix =doctermMatrix;
        validatingDocSet =null;
        arrLabel=null;
    }

    public AbstractClassifier(){
    	this.featureSelector =new NullFeatureSelector();
        this.indexReader =null;
        this.doctermMatrix =null;
        validatingDocSet =null;
        arrLabel=null;
    }

    public String getClassLabel(int index){
        if(arrLabel==null || index>=arrLabel.length)
            return null;
        else
            return arrLabel[index];
    }

    public IndexReader getIndexReader(){
        return indexReader;
    }

    public SparseMatrix getDocTermMatrix(){
        return doctermMatrix;
    }

    public FeatureSelector getFeatureSelector(){
        return featureSelector;
    }

    public void setFeatureSelector(FeatureSelector selector){
        this.featureSelector =selector;
    }

    public DocClassSet classify(DocClassSet trainingDocSet, DocClass testingDocs){
        train(trainingDocSet);
        return classify(testingDocs);
    }

    public DocClassSet classify(DocClassSet trainingDocSet, DocClassSet validatingDocSet, DocClass testingDocs){
        this.validatingDocSet =validatingDocSet;
        return classify(trainingDocSet,testingDocs);
    }
    
    public void train(DocClassSet trainingDocSet, DocClassSet validatingDocSet){
    	this.validatingDocSet =validatingDocSet;
    	train(trainingDocSet);
    }
    
    public DocClassSet classify(DocClass testingDocs) {
        DocClassSet docSet;
        int i, label;

        if (indexReader == null && doctermMatrix == null)
            return null;

        try {
            docSet = new DocClassSet(classNum);
            for(i=0;i<classNum;i++)
                docSet.getDocClass(i).setClassName(getClassLabel(i));
            for (i = 0; i < testingDocs.getDocNum(); i++) {
                label=classify(testingDocs.getDoc(i));
                if(label>=0)
                	docSet.addDoc(label, testingDocs.getDoc(i));
            }
            return docSet;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public int classify(IRDoc doc){
    	return classify(getRow(doc.getIndex()));
    }

    protected Row getRow(int docIndex){
        int[] columns;

        if(indexReader!=null){
            columns=indexReader.getTermIndexList(docIndex);
            if(columns==null || columns.length ==0)
                return null;
            return new IntRow(docIndex,columns.length,columns,indexReader.getTermFrequencyList(docIndex));
        }
        else{
            columns=doctermMatrix.getNonZeroColumnsInRow(docIndex);
            if(columns==null || columns.length ==0)
                return null;
            return new DoubleRow(docIndex,columns.length,columns, doctermMatrix.getNonZeroDoubleScoresInRow(docIndex));
        }
    }

    protected void trainFeatureSelector(DocClassSet trainingSet){
        if(indexReader!=null)
            featureSelector.train(indexReader,trainingSet);
        else
            featureSelector.train(doctermMatrix,trainingSet);
    }
}