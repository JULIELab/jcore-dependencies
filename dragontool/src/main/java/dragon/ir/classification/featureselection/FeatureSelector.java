package dragon.ir.classification.featureselection;

import dragon.ir.classification.DocClassSet;
import dragon.ir.index.IndexReader;
import dragon.matrix.SparseMatrix;
/**
 * <p>Interface of feature selector which often work tegether with text classifiers.</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface FeatureSelector {

    /**
     * This method chooses a subset of features for text classification. Some implmentations
     * such as DocFrequencySelector and InfoGainFeatureSelector do not support this method.
     * @param doctermMatrix the document-term matrix a classifer is working on
     * @param trainingSet the labeled training document set
     */
    public void train(SparseMatrix doctermMatrix, DocClassSet trainingSet);

    /**
     * This method chooses a subset of features for text classification
     * @param indexReader the index reader a classifer is working on
     * @param trainingSet the labeled training document set
     */
    public void train(IndexReader indexReader, DocClassSet trainingSet);

    /**
     * @param originalFeatureIndex the index of the feature in the old feature space
     * @return true if the given feature is selected for text classification
     */
    public boolean isSelected(int originalFeatureIndex);

    /**
     * @return the number of selected features
     */
    public int getSelectedFeatureNum();

    /**
     * Map the old feature index to the index in the new feature space.
     * @param originalFeatureIndex the index of the feature before feature selection
     * @return the index of the feature the new space. If the feature is not selected, it will return -1.
     */
    public int map(int originalFeatureIndex);

    /**
     * Manually set selected features. Usually used in testing stage.
     * @param selectedFeatures each elements contains the index of the selected feature in the old feature space.
     * The selected feature must be in the ascending order in the input array.
     */
    public void setSelectedFeatures(int[] selectedFeatures);
}