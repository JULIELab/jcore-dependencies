package dragon.matrix.factorize;

import dragon.matrix.DoubleDenseMatrix;
import dragon.matrix.SparseMatrix;

/**
 * <p>Interface of matrix factorization</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Factorization {
    /**
     * Factorizes the given matrix
     * @param matrix the matrix for factorization
     * @param dimension the number of dimensions for the middle matrix
     */
    public void factorize(SparseMatrix matrix, int dimension);

    /**
     * Gets the left matrix
     * @return the left matrix
     */
    public DoubleDenseMatrix getLeftMatrix();

    /**
     *Gets the right matrix
     * @return the right matrix
     */
    public DoubleDenseMatrix getRightMatrix();

    /**
     * Gets the middle matrix
     * @return the middle matrix
     */
    public DoubleDenseMatrix getMiddleMatrix();
}