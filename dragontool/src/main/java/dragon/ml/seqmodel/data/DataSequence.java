package dragon.ml.seqmodel.data;

/**
 * <p>Interface of data sequence</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */
public interface DataSequence {
    /**
     * Gets the dataset object which the current data sequence belongs to
     * @return he dataset object which the current data sequence belongs to
     */
    public Dataset getParent();

    /**
     * Sets the dataset object which the current data sequence belongs to
     * @param parent the parsent dataset
     */
    public void setParent(Dataset parent);

    /**
     * Makes a copy of the current data sequence
     * @return a copy of the current data sequence
     */
    public DataSequence copy();

    /**
     * @return the length of the sequence
     */
    public int length();

    /**
     * Gets the label of the token in the given position
     * @param pos the position in the sequence
     * @return the label of the token in the given position
     */
    public int getLabel(int pos);

    /**
     * Gets the original label of the token in the given position. For high order markov models, the getLabel method returns a label which
     * combines the current label and the lables of previous positions. However, this method still returns the original label of the given
     * position.
     * @param pos the position in the sequence
     * @return the label of the token in the given position
     */
    public int getOriginalLabel(int pos);

    /**
     * Gets the token in the given position of the sequence
     * @param pos the position of the token
     * @return a token object in the given position
     */
    public BasicToken getToken(int pos);

    /**
     * Sets the label to the token in the given position. Same as the getLabel method, for high order markov models, the input label is actually
     * a combined label. This method will convert the combined label to original lable (i.e. the label in the first order)
     * @param pos the position of the token
     * @param label the label for the position
     */
    public void setLabel(int pos, int label);

    /**
     * Gets the ending position of the segment beginning at the given position
     * @param segmentStart the starting position of the segment
     * @return the ending position of the segment beginning at the given position
     */
    public int getSegmentEnd(int segmentStart);

    /**
     * Marks the bondary of the segment and sets the given label to all tokens of the segment.
     * @param segmentStart the starting position of the segment
     * @param segmentEnd the ending position of the segment
     * @param label the lable for the segment
     */
    public void setSegment(int segmentStart, int segmentEnd, int label);
};
