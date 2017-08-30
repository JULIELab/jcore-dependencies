package dragon.ir.index.sequence;

/**
 * <p>Interface of Sequence Reader</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface SequenceReader {
    /**
     * It is required to call this method before reading out sequence information from indexing files.
     */
    public void initialize();
    public void close();

    /**
     * For example, a document or a setence could be treated as a seuqnce.
     * @param index: the index of the sequence
     * @return the sequence information
     */
    public int[] getSequence(int index);

    /**
     * @param index: the index of the sequence
     * @return the length of the index-th sequence
     */
    public int getSequenceLength(int index);
}