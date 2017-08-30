package dragon.matrix;

/**
 * <p>Interface of Integer-typed Sparse Matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IntSparseMatrix extends SparseMatrix {
    /**
     * It is equal to create an integer cell and then add the cell to the sparse matrix.
     * @param row the row of the cell
     * @param column the column of the cell
     * @param score the score of the cell
     * @return true if added successfully
     */
    public boolean add(int row, int column, int score);

    /**
     * @param row the index of the row
     * @return the summation score of the given row.
     */
    public long getRowSum(int row);
}