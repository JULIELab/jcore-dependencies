package dragon.matrix;

/**
 * <p>Interface of Double-typed Sparse Matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface DoubleSparseMatrix extends SparseMatrix{
    /**
     * It is equal to create a double cell and then add the cell to the sparse matrix.
     * @param row the row of the cell
     * @param column the column of the cell
     * @param score the score of the cell
     * @return true if added successfully
     */
    public boolean add(int row, int column, double score);

    /**
     * @param row the index of the row
     * @return the summation score of the given row.
     */
    public double getRowSum(int row);
}