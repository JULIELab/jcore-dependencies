package dragon.ir.classification;

import dragon.ir.classification.featureselection.FeatureSelector;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.matrix.Row;
/**
 * <p>Interface of Text Classifier</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Classifier {
    /**
     * @return the index reader the classifier iw working on
     */
    public IndexReader getIndexReader();

    /**
     * @return the feature selector the current classifier is using
     */
    public FeatureSelector getFeatureSelector();

    /**
     * @param selector the feature selector for the classifier.
     */
    public void setFeatureSelector(FeatureSelector selector);

    /**
     * This method trains the classifier with the training document set.
     * @param trainingDocSet training document set
     */
    public void train(DocClassSet trainingDocSet);
    
    /**
     * This method trains the classifier with the training document set and validating document set.
     * @param trainingDocSet training document set
     * @param validatingDocSet validating document set
     */
    public void train(DocClassSet trainingDocSet, DocClassSet validatingDocSet);

    /**
     * This method uses the trained model to classify the testing documents. The train method should be called before calling this method.
     * @param testingDocs testing document set
     * @return classified testing document set
     */
    public DocClassSet classify(DocClass testingDocs);

    /**
     * This method trains the classifier with the training document set and then using the trained model to classify the testing documents.
     * @param trainingDocSet training document set
     * @param testingDocs testing document set
     * @return classified testing document set
     */
    public DocClassSet classify(DocClassSet trainingDocSet, DocClass testingDocs);

    /**
     * @param trainingDocSet the training document set
     * @param validatingDocSet the validation document set, usually for avoiding the overfitting problem
     * @param testingDocs the testing document set
     * @return classified testing document set
     */
    public DocClassSet classify(DocClassSet trainingDocSet, DocClassSet validatingDocSet, DocClass testingDocs);

    /**
     * Classify one particular document
     * @param doc the index of the document is stored in the IRDoc object
     * @return the index of the category of this document. The index starts from zero.
     */
    public int classify(IRDoc doc);
    
    /**
     * Classify one particular document
     * @param doc document represented by a Row object
     * @return the index of the category of this document. The index starts from zero.
     */
    public int classify(Row doc);

    /**
     * Gets the label of a given document category
     * @param index the index of the category
     * @return the label of the category
     */
    public String getClassLabel(int index);

    /**
     * Rank all class labels. The classify(Row doc) method should be called before calling this method.
     * @return the ranking of class labels. The first and last element of the returned array contain the most possible 
     * label and the least possible label, respectively.
     */
    public int[] rank();
    
    /**
     * Save the trained classifier model to a file which can be used to restore the classifier later. If the model is not trained yet, this
     * method does nothing. The classifier restored from this model file later can only execute the method classify(Row doc) to determine the
     * label of a document.
     * @param modelFile output file name
     */
    public void saveModel(String modelFile);
}