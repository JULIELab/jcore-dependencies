package dragon.matrix;

/**
 * <p>Interface of Double Dense Matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface DoubleDenseMatrix extends DenseMatrix{
    /**
     * Assign the same given score to all cells of the matrix
     * @param val the score
     */
    public void assign(double val);

    public boolean add(int row, int column, double score);
    public boolean setDouble(int row, int column, double score);

    /**
     * @param row the index of the row
     * @return the summation score of the given row.
     */
    public double getRowSum(int row);

    /**
     * @param column the index of the column
     * @return the summation score of the given column
     */
    public double getColumnSum(int column);
}