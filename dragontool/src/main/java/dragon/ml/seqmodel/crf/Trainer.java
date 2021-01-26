package dragon.ml.seqmodel.crf;

import dragon.ml.seqmodel.data.Dataset;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;

/**
 * <p>Interface of CRF trainer</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Trainer {
    /**
     * Gets the model graph
     * @return the model graph
     */
    public ModelGraph getModelGraph();

    /**
     * Gets the feature generator
     * @return the feature generator
     */
    public FeatureGenerator getFeatureGenerator();

    /**
     * Gets the parameters of the CRF model
     * @return the parameters of the CRF model
     */
    public double[] getModelParameter();

    /**
     * Saves the parameters of the CRF model to a text file
     * @param filename the name of the file the model parameters will save to
     * @return true if saving successfully
     */
    public boolean saveModelParameter(String filename);

    /**
     * Trains the CRF model with labeled dataset
     * @param dataset the dataset for training
     * @return true if trained successfully
     */
    public boolean train(Dataset dataset);

    /**
     * Gets the scaling option. The likelihood of the sequence may be too small. Thus it may be necessary to scale the likelihood
     * @return true if the training needs scaling
     */
    public boolean needScaling();

    /**
     * Sets the scaling option
     * @param option the scaling option
     */
    public void setScalingOption(boolean option);

    /**
     * Gets the number of maximum iterations
     * @return the number of maximum iterations
     */
    public int getMaxIteration();

    /**
     * Sets the number of maximum iterations
     * @param maxIteration the number of maximum iterations
     */
    public void setMaxIteration(int maxIteration);
}