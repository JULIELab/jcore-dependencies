package dragon.ml.seqmodel.data;

/**
 * <p>Interface of sequence data reader</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface DataReader {
    /**
     * Reads out all sequences
     * @return a dataset
     */
    public Dataset read();

    /**
     * Reads out one sequence and the pointer autoamtically moves to the next sequence.
     * @return a data sequence
     */
    public DataSequence readRow();

    /**
     * Releases occupied resources
     */
    public void close();
}