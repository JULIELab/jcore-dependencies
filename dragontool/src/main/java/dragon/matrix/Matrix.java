package dragon.matrix;

/**
 * <p>Interface of matrix including dense matrix and sparse matrix </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Matrix {
    /**
     * @return number of rows
     */
    public int rows();

    /**
     * @return number of columns
     */
    public int columns();

    /**
     * The index of the first row in a matrix is zero. The base row is the index of the first non-empty row of the matrix.
     * @return the index of the first non-empty row of the matrix.
     */
    public int getBaseRow();

    /**
     * The index of the first column in a matrix is zero. The base column is the index of the first non-empty column of the matrix. This
     * method is not used in this version. It always simply returns zero.
     * @return zero.
     */
    public int getBaseColumn();

    /**
     * For double matrix, this method returns 8; for integer matrix, it returns 4.
     * @return the length in bytes of the data stored in the cell.
     */
    public int getCellDataLength();

    /**
     * Return the score stored in the specified cell. If the score is double-typed, it will be coverted to an integer.
     * @param row the row index of the cell
     * @param column the column index of the cell
     * @return the score stored in the cell
     */
    public int getInt(int row, int column);

    /**
     * Return the score stored in the specified cell. If the score is integer-typed, it will be coverted to a double.
     * @param row the row index of the cell
     * @param column the column index of the cell
     * @return the score stored in the cell
     */
    public double getDouble(int row, int column);

    /**
     * This method treats each row a vector and return the cosine similarity of two rows.
     * @param rowA the row index of the first vector
     * @param rowB the row index of the second vector
     * @return the cosine similarity of two given rows.
     */
    public double cosine(int rowA, int rowB);

    /**
     * If two cells in the same column have non-zero scores, they will be counted as co-occurred.
     * @param rowA the index of the first row
     * @param rowB the index of the second row
     * @return the cooccurrence count
     */
    public int getCooccurrenceCount(int rowA, int rowB);

    /**
     * if one has set the transposed matrix by calling the method setTranspose, this method simply return that transposed matrix. Otherwise,
     * it will generate the transposed matrix online. So this method may take several minutes.
     * @return the transposed matrix
     */
    public Matrix transpose();

    /**
     * If one never calls niether the method of setTranspose nor the method of transpose, it returns null.
     * @return the transpose matrix
     */
    public Matrix getTranspose();

    /**
     * Manually set the transposed matrix.
     * @param matrix the transpose matrix of the current matrix.
     */
    public void setTranspose(Matrix matrix);

    /**
     * Close the matrix and release all resources.
     */
    public void close();
}