package dragon.ir.index.sequence;

/**
 * <p>Interface of sequence writer</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface SequenceWriter {
    /**
     * It is required to call this method before calling the addSequence method.
     */
    public void initialize();
    public void close();

    /**
     * @param index: the index of the sequence
     * @param seq: the sequence information
     * @return true if added successfully
     */
    public boolean addSequence(int index, int[] seq);
}