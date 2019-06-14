package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.data.Dataset;

/**
 * <p>Interface of feature type which is actually a factory generating certain type of features</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface FeatureType {
    /**
     * Starts scanning features of the given segment of a sequence
     * @param seq the sequence
     * @param startPos the start position of the segment
     * @param endPos the end position of the segment
     * @return true if successfully
     */
    public boolean startScanFeaturesAt(DataSequence seq, int startPos, int endPos);

    /**
     * Tests if next feature exists
     * @return true if next feature exists
     */
    public boolean hasNext();

    /**
     * Gets the next feature
     * @return the next feature if any
     */
    public Feature next();

    /**
     * Tests if the current feature type needs training
     * @return true if the feature type needs training
     */
    public boolean needTraining();

    /**
     * Trains the feature types with the labeled training dataset
     * @param dataset the dataset for training
     * @return true if trained successfully
     */
    public boolean train(Dataset dataset);

    /**
     * Saves the training results in training mode
     * @return true if saved successfully
     */
    public boolean saveTrainingResult();

    /**
     * Reads the training result in testing mode
     * @return training result
     */
    public boolean readTrainingResult();

    /**
     * Gets the unique id of the current feature type.
     * @return type ID
     */
    public int getTypeID();

    /**
     * Sets the id of the current feature type
     * @param typeID the type id
     */
    public void setTypeID(int typeID);

    /**
     * Tests if the feature type supports generating features for segments.
     * @return true if supported
     */
    public boolean supportSegment();
}