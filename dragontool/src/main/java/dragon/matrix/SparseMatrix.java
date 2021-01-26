package dragon.matrix;

/**
 * <p>Interface of sparse matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface SparseMatrix extends Matrix{
    /**
     * @return the number of non-zero cells
     */
    public int getNonZeroNum();

    /**
     * @param row the index of the row
     * @return the number of non-zero cells in the given row.
     */
    public int getNonZeroNumInRow(int row);

    /**
     * If the transposed matrix is not set yet, this method will automatically generate the transposed matrix. Thus, it may take several
     * minutes for the first call.
     * @param column the index of the column
     * @return the number of non-zero cells in the given column.
     */
    public int getNonZeroNumInColumn(int column);

    /**
     * This method returns the column index of the index-th non-zero cell in the given row. The index starts from zero.
     * @param row the index of the row
     * @param index the index-th non-zero cell
     * @return the column index of the specified cell
     */
    public int getNonZeroColumnInRow(int row, int index);

    /**
     * This method returns the row index of the index-th non-zero cell in the given column. The index starts from zero. If the transposed
     * matrix is not set yet, this method will automatically generate the transposed matrix. Thus, it may take several minutes.
     * @param column the index of the column
     * @param index the index-th non-zero cell
     * @return the row index of the specified cell.
     */
    public int getNonZeroRowInColumn(int column, int index);

    /**
     * If the given row has n non-zero cells, this method returns a n-length integer array. Each element of the array stores the column index
     * of the corresponding non-zero cell.
     * @param row the index of row
     * @return the array of non-zero columns in the given row
     */
    public int[] getNonZeroColumnsInRow(int row);

    /**
     * If the given column has n non-zero cells, this method returns a n-length integer array. Each element of the array stores the row index
     * of the corresponding non-zero cell. If the transposed matrix is not set yet, this method will automatically generate the transposed
     * matrix. Thus, it may take several minutes.
     * @param column the index of the column
     * @return the array of non-zero rows in the given column.
     */
    public int[] getNonZeroRowsInColumn(int column);

    /**
     * This method returns the index-th non-zero cell in the given column. The index starts from zero.
     * @param row the index of the row
     * @param index the index-th non-zero cell
     * @return the cell
     */
    public Cell getNonZeroCellInRow(int row, int index);

    /**
     * This method returns the index-th non-zero cell in the given row. The index starts from zero. If the transposed matrix is not set yet,
     * this method will automatically generate the transposed matrix. Thus, it may take several minutes.
     * @param column the index of the column
     * @param index the index-th non-zero cell
     * @return the cell
     */
    public Cell getNonZeroCellInColumn(int column, int index);

    /**
     * This method returns the double score of the index-th non-zero cell in the given row. The index starts from zero.
     * @param row the index of the row.
     * @param index the index-th non-zero cell
     * @return the double score
     */
    public double getNonZeroDoubleScoreInRow(int row, int index);

    /**
     * This method returns the double score of the index-th non-zero cell in the given column. The index starts from zero. If the transposed
     * matrix is not set yet, this method will automatically generate the transposed matrix. Thus, it may take several minutes.
     * @param column the index of the column
     * @param index the index-th non-zero cell
     * @return the double score
     */
    public double getNonZeroDoubleScoreInColumn(int column, int index);

    /**
     * If the given row has n non-zero cells, this method returns a n-length double array. Each element of the array stores the double score
     * of the corresponding non-zero cell.
     * @param row the index of row
     * @return the array of non-zero double scores in the given row
     */
    public double[] getNonZeroDoubleScoresInRow(int row);

    /**
     * If the given column has n non-zero cells, this method returns a n-length double array. Each element of the array stores the double score
     * of the corresponding non-zero cell. If the transposed matrix is not set yet, this method will automatically generate the transposed
     * matrix. Thus, it may take several minutes.
     * @param column the index of the column
     * @return the array of non-zero double scores in the given column.
     */
    public double[] getNonZeroDoubleScoresInColumn(int column);

    /**
     * This method returns the integer score of the index-th non-zero cell in the given row. The index starts from zero.
     * @param row the index of the row.
     * @param index the index-th non-zero cell
     * @return the integer score
     */
    public int getNonZeroIntScoreInRow(int row, int index);

    /**
     * This method returns the integer score of the index-th non-zero cell in the given column. The index starts from zero. If the transposed
     * matrix is not set yet, this method will automatically generate the transposed matrix. Thus, it may take several minutes.
     * @param column the index of the column
     * @param index the index-th non-zero cell
     * @return the integer score
     */
    public int getNonZeroIntScoreInColumn(int column, int index);

    /**
     * If the given row has n non-zero cells, this method returns a n-length integer array. Each element of the array stores the integer score
     * of the corresponding non-zero cell.
     * @param row the index of row
     * @return the array of non-zero integer scores in the given row
     */
    public int[] getNonZeroIntScoresInRow(int row);

    /**
     * If the given column has n non-zero cells, this method returns a n-length integer array. Each element of the array stores the integer score
     * of the corresponding non-zero cell. If the transposed matrix is not set yet, this method will automatically generate the transposed
     * matrix. Thus, it may take several minutes.
     * @param column the index of the column
     * @return the array of non-zero integer scores in the given column.
     */
    public int[] getNonZeroIntScoresInColumn(int column);

    /**
     * The cell (i,j) in the output cooccurrence matrix stores the cooccurrence count of the i-th row and the j-th row.
     * @param outputCooccurMatrix the output cooccurrence matrix
     * @return true if successfully
     */
    public boolean genCooccurrenceMatrix(IntSparseMatrix outputCooccurMatrix);

    /**
     * The cell (i,j) in the output cooccurrence matrix stores the cooccurrence count of the i-th row in the current matrix and the j-th row
     * in the matrixY. The current matrix and the input matrix should have the same number of columns.
     * @param matrixY another sparse matrix
     * @param outputCooccurMatrix the output cooccurrence matrix
     * @return true if successfully
     */
    public boolean genCooccurrenceMatrix(SparseMatrix matrixY, IntSparseMatrix outputCooccurMatrix);

    /**
     * The cell (i,j) in the output cooccurrence matrix stores the cooccurrence count of the i-th row in the current matrix and the j-th row
     * in the matrixY. The current matrix and the input matrix should have the same number of columns. If the cooccurrence count less than the
     * given threshold, it will be set to zero.
     * @param matrixY another sparse matrix
     * @param minOccurrence the minimum cooccurrence count
     * @param outputCooccurMatrix the output cooccurrence matrix
     * @return true if successfully
     */
    public boolean genCooccurrenceMatrix(SparseMatrix matrixY, int minOccurrence, IntSparseMatrix outputCooccurMatrix);

    /**
     * If the specified cell doesn't exist, this method will return null.
     * @param row the index of row in the matrix
     * @param column the index of column in the matrix
     * @return the cell
     */
    public Cell getCell(int row, int column);

    /**
     * Add a cell to the sparse matrix. If the cell already exists in the sparse matrix, the behavior of the addition is subject to the
     * implementations. It may simply add the score to the existing cell. It may return false. It may cause errors even if the method returns
     * true. Thus, one should be cautious when adding possibly existing cells to the sparse matrix.
     * @param cell the input cell
     * @return true if successfully added.
     */
    public boolean add(Cell cell);

    /**
     * Many implementations of sparse matrix cache cells in memory. This method gives the chance to dump cells in the cache to the disk file.
     */
    public void flush();

    /**
     * It is equal to calling finalizeData(true)
     * @return true if finalizing successfully
     */
    public boolean finalizeData();

    /**
     * Any sparse matrix has two possible modes, writing mode and reading mode. After calling this method, writing is not allowed any more.
     * Instead, one call read out data from the sparse matrix. In most cases, one should set the sorting option to true. In one case that all
     * cells are added to the sparse matrix in their natural order (from top to bottom, from left to right), one can set the sorting option
     * to false for saving time.
     * @param sorting if need to sort the added cells before finalization.
     * @return true if finalizing successfully
     */
    public boolean finalizeData(boolean sorting);

    /**
     * If this method returns true, one can not add data to the sparse matrix any more.
     * @return true if finalized
     */
    public boolean isFinalized();

    /**
     * This method return an empty sparse matrix with the same implemented type as the current matrix.
     * @return an emtpy sparse matrix
     */
    public SparseMatrix createSparseMatrix();

    /**
     * This method will be called when the sparse matrix loads data from a binary file.
     * @param row the row index of the cell
     * @param column the column index of the cell
     * @param data the input data for the cell
     * @return cell object
     */
    public Cell createCell(int row, int column, byte[] data);

    /**
     * This method will be called when the sparse matrix loads data from a text file.
     * @param row the row index of the cell
     * @param column the column index of the cell
     * @param data the input data for the cell
     * @return cell object
     */
    public Cell createCell(int row, int column, String data);
}