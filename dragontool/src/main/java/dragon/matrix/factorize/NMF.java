package dragon.matrix.factorize;

import dragon.matrix.*;

/**
 * <p>None negative matrix factorization </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class NMF extends AbstractFactorization {
    private SparseMatrix xt;
    private DoubleDenseMatrix u, m, v;
    private int iterations;

    public NMF(int iterations) {
        this.iterations =iterations;
    }

    public void factorize(SparseMatrix x, int dimension){
        DoubleDenseMatrix xv, vt, vtv, uvtv, xtu, ut, utu, vutu;
        int k,i,j;
        double score;

        this.xt=(SparseMatrix)x.transpose();
        u=genPositiveMatrix(x.rows(),dimension);
        v=genPositiveMatrix(x.columns(),dimension);
        xv=new DoubleFlatDenseMatrix(x.rows(),dimension);
        vt=new DoubleFlatDenseMatrix(dimension,x.columns());
        vtv=new DoubleFlatDenseMatrix(dimension, dimension);
        utu=vtv;
        uvtv=new DoubleFlatDenseMatrix(x.rows(),dimension);
        xtu=new DoubleFlatDenseMatrix(x.columns(),dimension);
        ut=new DoubleFlatDenseMatrix(dimension, x.rows());
        vutu=new DoubleFlatDenseMatrix(x.columns(),dimension);

        for(k=0;k<iterations;k++){
            product(x, v, xv);
            transpose(v,vt);
            product(vt, v,vtv);
            product(u, vtv,uvtv);

            product(xt, u, xtu);
            transpose(u, ut);
            product(ut, u, utu);
            product(v, utu, vutu);

            for (i = 0; i < v.rows(); i++) {
                for (j = 0; j < v.columns(); j++) {
                    score=vutu.getDouble(i, j)+SMALL_QUANTITY;
                    score=v.getDouble(i, j) * xtu.getDouble(i, j) / score;
                    v.setDouble(i, j, score);
                 }
            }

            for (i = 0; i < u.rows(); i++) {
                for (j = 0; j < u.columns(); j++) {
                    score=uvtv.getDouble(i, j)+SMALL_QUANTITY;
                    score=u.getDouble(i, j) * xv.getDouble(i, j) /score;
                    u.setDouble(i, j, score);
                }
            }
            normalizeColumn(u);
        }
   }

   public DoubleDenseMatrix getLeftMatrix() {
       return u;
   }

   public DoubleDenseMatrix getRightMatrix() {
       return v;
   }

   public DoubleDenseMatrix getMiddleMatrix() {
       return m;
   }
}