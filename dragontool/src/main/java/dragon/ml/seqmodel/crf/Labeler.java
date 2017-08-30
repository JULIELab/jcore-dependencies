package dragon.ml.seqmodel.crf;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;

/**
 * <p>Interface of CRF Labeler</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Labeler {
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
     * Reads the parameters of the CRF model from a text file
     * @param filename the name of the file containing the model parameters
     * @return true if reading successfully
     */
    public boolean readModelParameter(String filename);

    /**
     * Labels the given sequence
     * @param dataSeq the sequence for labeling
     * @return true if labeled successfully
     */
    public boolean label(DataSequence dataSeq);

    /**
     * Labels the sequence using the given model
     * @param dataSeq the sequence for labeling
     * @param lambda the parameters of the CRF model
     * @return true if labeled successfully
     */
    public boolean label(DataSequence dataSeq,double[] lambda);

    /**
     * Gets the order-th solution. The labels will be set to the data sequence. To get the best solution, the order should be set to zero
     * @param dataSeq the data seqeunce
     * @param order the ranking of the solutions
     * @return the score of the solution
     */
    public double getBestSolution(DataSequence dataSeq, int order);
}