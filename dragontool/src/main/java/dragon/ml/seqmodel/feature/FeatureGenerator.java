package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.data.Dataset;

/**
 * <p>Interface of Feature Generator</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface FeatureGenerator  {
    /**
     * Adds wanted feature type to the feature generator
     * @param featureType the wanted feature type
     * @return true if added successfully
     */
    public boolean addFeatureType(FeatureType featureType);

    /**
     * Gets the number of wanted feature types.
     * @return the number of wanted feature types.
     */
    public int getFeatureTypeNum();

    /**
     * Gets the index-th feature type
     * @param index the index of the feature type
     * @return the index-th feature type
     */
    public FeatureType getFeatureTYpe(int index);

    /**
     * This method will be called in training mode. The training procedure will build a index of all possible features. It will also train
     * each feature type if necessary.
     * @param data the dataset for training
     * @return true if trained successfully
     */
    public boolean train(Dataset data);

    /**
     * This method will be called in testing mode. The related data for wanted feature types will be loaded. Actually this method will call
     * the readTrainingResult method of each wanted feature type.
     * @return true if data is loaded successfully
     */
    public boolean loadFeatureData();

    /**
     * Reads feature list from a text file
     * @param featureFile the name of the file containing the feature information
     * @return true if read successfully
     */
    public boolean readFeatures(String featureFile);

    /**
     * Save features to a text file
     * @param featureFile the name of the file all features will be save to
     * @return true if saved successfully
     */
    public boolean saveFeatures(String featureFile);

    /**
     * Starts scanning features for the specified segment
     * @param data the sequence data
     * @param startPos the start position of the segment
     * @param endPos the end position of the segment
     */
    public void startScanFeaturesAt(DataSequence data, int startPos, int endPos);

    /**
     * Tests if next feature exists
     * @return true if next feature exist.
     */
    public boolean hasNext();

    /**
     * Gets the next feature.
     * @return next feature
     */
    public Feature next();

    /**
     * @return the number of unique features
     */
    public int getFeatureNum();

    /**
     * @param featureIndex the index of the feature
     * @return the name of the feature
     */
    public String getFeatureName(int featureIndex);

    /**
     * Tests if the feature generatore supports the segment labeling problem
     * @return true if supported
     */
    public boolean supportSegment();
};
