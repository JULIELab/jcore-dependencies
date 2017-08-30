package dragon.matrix.factorize;

import dragon.matrix.*;
import java.util.Random;

/**
 * <p>Abstract class for matrix factorization </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

 public abstract class AbstractFactorization implements Factorization {
    public static final double SMALL_QUANTITY = 0.000000001;

    protected DoubleDenseMatrix genPositiveMatrix(int x, int y) {
        DoubleDenseMatrix matrix;
        Random rand;
        int i, j;

        rand = new Random(1);
        matrix = new DoubleFlatDenseMatrix(x, y);
        for (i = 0; i < x; i++) {
            for (j = 0; j < y; j++) {
                matrix.setDouble(i, j, rand.nextDouble() + SMALL_QUANTITY);
            }
        }
        return matrix;
    }

    protected void product(DoubleDenseMatrix a, DoubleDenseMatrix b, DoubleDenseMatrix c) {
        int row, col, i;
        double score;

        for (row = 0; row < a.rows(); row++) {
            for (col = 0; col < b.columns(); col++) {
                score = 0;
                for (i = 0; i < a.columns(); i++) {
                    score += a.getDouble(row, i) * b.getDouble(i, col);
                }
                c.setDouble(row, col, score);
            }
        }
    }

    protected void product(SparseMatrix a, DoubleDenseMatrix b, DoubleDenseMatrix c) {
        int row, col, i;
        int[] arrColumn;
        double[] arrScore;
        double score;

        for (row = 0; row < a.rows(); row++) {
            arrColumn = a.getNonZeroColumnsInRow(row);
            arrScore = a.getNonZeroDoubleScoresInRow(row);
            for (col = 0; col < b.columns(); col++) {
                score = 0;
                for (i = 0; i < arrColumn.length; i++) {
                    score += arrScore[i] * b.getDouble(arrColumn[i], col);
                }
                c.setDouble(row, col, score);
            }
        }
    }

    protected void transpose(DoubleDenseMatrix a, DoubleDenseMatrix at) {
        int row, col;

        for (row = 0; row < a.rows(); row++) {
            for (col = 0; col < a.columns(); col++) {
                at.setDouble(col, row, a.getDouble(row, col));
            }
        }
    }

    protected void normalizeColumn(DoubleDenseMatrix a) {
        int row, col;
        double norm, score;

        for (col = 0; col < a.columns(); col++) {
            norm = 0;
            for (row = 0; row < a.rows(); row++) {
                score = a.getDouble(row, col);
                norm += score * score;
            }
            norm = Math.sqrt(norm);
            for (row = 0; row < a.rows(); row++) {
                score = a.getDouble(row, col);
                a.setDouble(row, col, score / norm);
            }

        }
    }
}