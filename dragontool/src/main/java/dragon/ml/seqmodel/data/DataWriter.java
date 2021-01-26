package dragon.ml.seqmodel.data;

/**
 * <p>Interface of sequence data writer </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface DataWriter {
    /**
     * Writes out the whole dataset
     * @param dataset the dataset for writing
     * @return true if writing successfully
     */
    public boolean write(Dataset dataset);

    /**
     * Writes out one data sequence
     * @param dataSeq the data sequence for writing
     * @return true if writing successfully
     */
    public boolean write(DataSequence dataSeq);

    /**
     * Releases occupied resources
     */
    public void close();
}