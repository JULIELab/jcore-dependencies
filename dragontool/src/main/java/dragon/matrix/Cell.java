package dragon.matrix;

/**
 * <p>Interface of cell which is the unit of a matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Cell extends Comparable{
    public int getRow();
    public int getColumn();

    /**
     * @return the corresponding cell in the transposed matrix.
     */
    public Cell transpose();

    /**
     * @return the byte array converted from the cell score.
     */
    public byte[] toByteArray();

    /**
     * Read the cell score from the byte array.
     * @param data the cell score in the byte-array format.
     */
    public void fromByteArray(byte[] data);

    /**
     * @return a string which stands for the score in the cell.
     */
    public String toString();

    /**
     * Read the cell score from the input string
     * @param data the string-formatted cell data
     */
    public void fromString(String data);

    public void setDoubleScore(double score);
    public double getDoubleScore();
    public void setLongScore(long score);
    public long getLongScore();
    public void setIntScore(int score);
    public int getIntScore();
    public void setByteScore(byte score);
    public byte getByteScore();
    /**
     * Merge the score of the input cell with the current cell. The current cell and the input cell should have the same column and row.
     * @param cell the input cell object
     */
    public void merge(Cell cell);

    /**
     * If the reset option is true, the merge method replaces the cell score with the new score, otherwise adds the new score to the existing score.
     * @return the reset option
     */
    public boolean getResetOption();

    /**
     * If the reset option is true, the merge method replaces the cell score with the new score, otherwise adds the new score to the existing score.
     * @param option the reset option
     */
    public void setResetOption(boolean option);
}