package dragon.ml.seqmodel.data;

/**
 * <p>The basic interface to be implemented by the user of this package for
 * providing training and test data to the learner.<p>
 * @author Sunita Sarawagi
 */

public interface Dataset {
    /**
     * Moves the pointer to the position before the first sequence.
     */
    public void startScan();

    /**
     * Tests if the next sequence exists
     * @return true if next sequence exists
     */
    public boolean hasNext();

    /**
     * Reads out the next sequence
     * @return the next sequence if any, otherwise null
     */
    public DataSequence next();

    /**
     * Gets the number of unique labels. If there are 4 original labels and the markov chain is of the second order, this method returns 16.
     * @return the number of unique labels.
     */
    public int getLabelNum();

    /**
     * Gets the number of original unique labels no matter what order the markov chain is.
     * @return the number of original unique labels no matter what order the markov chain is.
     */
    public int getOriginalLabelNum();

    /**
     * Gets the order of the markov chain
     * @return the order of the markov chain
     */
    public int getMarkovOrder();

    /**
     * Gets the number of sequences in the dataset
     * @return the number of sequences in the dataset
     */
    public int size();
};
