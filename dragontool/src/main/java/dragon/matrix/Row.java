package dragon.matrix;

/**
 * <p>Interface of row for sparse matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Row extends Comparable{
    /**
     * This method will be called when the sparse matrix loads rows from a binary file.
     * @param row the index of the row
     * @param num the number of non-zero cells in the row
     * @param data the input data for the row
     */
    public void load(int row, int num, byte[] data);

    /**
     * @return the index of the row
     */
    public int getRowIndex();

    /**
     * @return the number of non-zero cells in the current row
     */
    public int getNonZeroNum();

    /**
     * @return an array of column indices of non-zero cells in the current row.
     */
    public int[] getNonZeroColumns();

    /**
     * @param index the index-th non-zero cell in the currrent row
     * @return the column index of the cell
     */
    public int getNonZeroColumn(int index);

    /**
     * If the specified cell does not exist, it returns null.
     * @param column the index of the column
     * @return the cell object
     */
    public Cell getCell(int column);

    /**
     * @param index the index-th non-zero cell
     * @return the cell object
     */
    public Cell getNonZeroCell(int index);

    /**
     * Return the double score of the index-th non-zero cell
     * @param index the index-th non-zero cell
     * @return double score
     */
    public double getNonZeroDoubleScore(int index);

    /**
     * Return the integer score of the index-th non-zero cell
     * @param index the index-th non-zero cell
     * @return integer score
     */
    public int getNonZeroIntScore(int index);

    /**
     * Some implementations of sparse matrix cache rows in memory. In this case, it is required to have a measure to determine if the row
     * should be removed from the cache when the cache is full.
     * @param factor the measure of how this row is frequently used
     */
    public void setLoadFactor(float factor);
    public float getLoadFactor();
}