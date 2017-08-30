package dragon.ml.seqmodel.feature;

/**
 * <p>A single feature returned by the FeatureGenerator needs to support this interface.</p>
 * @author Sunita Sarawagi
 */

public interface Feature {
    /**
     * One can quickly find out the index of the current feature according to its unique identifier
     * @return the feature identifier of the current feature
     */
    public FeatureIdentifier getID();

    /**
     * Sets the identitifer
     * @param id the feature identifier
     */
    public void setID(FeatureIdentifier id);

    /**
     * Gets a copy of the current feature
     * @return the copy the current feature
     */
    public Feature copy();

    /**
     * The index of this feature from 0..numFeatures-1.
     * @return the index of the feature
     */
    public int getIndex();

    /**
     * Sets the index of the feature
     * @param index the index of the feature
     */
    public void setIndex(int index);

    /**
     * has to be a label index from 0..numLabels-1
     * @return the label of the feature
     */
    public int getLabel();

    /**
     * Sets the label of the current feature
     * @param label the label of the feature
     */
    public void setLabel(int label);

    /**
     * can be -1 if the feature is a state rather than an edge feature
     * @return the label of the starting state of an edge feature, otherwise -1
     */
    public int getPrevLabel();

    /**
     * Sets the label of the starting state of an edge feature if the current feature is an edge feature.
     * @param prevLabel the label of the starting state of an edge feature
     */
    public void setPrevLabel(int prevLabel);

    /**
     * @return the value of feature
     */
    public double getValue();

    /**
     * Sets the value to the feature
     * @param val the value of the feature
     */
    public void setValue(double val);
  };
