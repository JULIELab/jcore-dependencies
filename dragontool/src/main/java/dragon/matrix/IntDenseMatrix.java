package dragon.matrix;

/**
 * <p>Interface of Integer-typed Dense Matrix </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IntDenseMatrix extends DenseMatrix{
    /**
     * Assign the same given score to all cells of the matrix
     * @param val the score
     */
    public void assign(int val);
    public boolean add(int row, int column, int score);
    public boolean setInt(int row, int column, int score);

    /**
     * @param row the index of the row
     * @return the summation score of the given row.
     */
    public long getRowSum(int row);

    /**
     * @param column the index of the column
     * @return the summation score of the given column
     */
    public long getColumnSum(int column);
}