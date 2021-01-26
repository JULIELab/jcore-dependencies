package dragon.ml.seqmodel.model;

import dragon.ml.seqmodel.data.DataSequence;

/**
 * <p>Interface of model graph</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface ModelGraph {
    /**
     * Gets the number of states. It is always equal to the number of labels except for the nested model.
     * @return the number of states
     */
    public int getStateNum();

    /**
     * @return the number of states which can serve as starting states
     */
    public int getStartStateNum();

    /**
     * @return the number of states which can serve as ending states
     */
    public int getEndStateNum();

    /**
     * Tests if the i-th state is an ending state
     * @param i the i-th state
     * @return true if the i-th state is an ending state
     */
    public boolean isEndState(int i);

    /**
     * Tests if the i-th state is a starting state
     * @param i the i-th state
     * @return true if the i-th state is a starting state
     */
    public boolean isStartState(int i);

    /**
     * Gets the index-th starting state. The index is between zero and the number of starting states minor one
     * @param index the index-th starting state
     * @return the index-th starting state.
     */
    public int getStartState(int index);

    /**
     * Gets the index-th ending state. The index is between zero and the number of ending states minor one
     * @param index the index-th ending state
     * @return the index-th ending state.
     */
    public int getEndState(int index);

    /**
     * @return the number of edges
     */
    public int getEdgeNum();

    /**
     * Gets the label of the state. The label and the state should be the same except for nested models.
     * @param state the index of the state
     * @return the label of the state
     */
    public int getLabel(int state);

    /**
     * Gets the number of unique labels. If there are 4 original labels and the markov chain is of the second order, this method returns 16.
     * @return the number of unique labels.
     */
    public int getLabelNum();

    /**
     * Gets the number of original unique labels no matter what order the markov chain is. It is always equal to the number of labels
     * unless markov is not the first order.
     * @return the number of original unique labels
     */
    public int getOriginalLabelNum();

    /**
     * Gets the order of the markov chain. It is always equal to one except for the complete model.
     * @return the order of the markov chain.
     */
    public int getMarkovOrder();

    /**
     * @return the iterator for edges
     */
    public EdgeIterator getEdgeIterator();

    /**
     * Maps states to labels. It does nothing unless nested model
     * @param seq the sequence for mapping
     * @return true if mapping successfully
     */
    public boolean mapStateToLabel(DataSequence seq);

    /**
     * Maps labels to states. It does nothing unless nested model.
     * @param seq the sequence for mapping
     * @return true if mapping successfully
     */
    public boolean mapLabelToState(DataSequence seq);

    /**
     * This method is used by inner model only
     * @param data the sequence for mapping
     * @param len the number of tokens for mapping
     * @param start the position of the starting token
     * @return true if mapping successfully
     */
    public boolean mapLabelToState(DataSequence data, int len, int start);
};
