package dragon.matrix.vector;

import dragon.matrix.DenseMatrix;
import dragon.matrix.SparseMatrix;

/**
 * <p>Power Method</p>
 * <p>This algorithm is developed for the egienvector corresponding to the dominant eigenvalue
 * Reference:Lawrence Page, Sergey Brin, Rajeev Motwani, and Terry Winograd (1999).
 * "The PageRank citation ranking: Bringing order to the Web
 * </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class PowerMethod implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private double tolerance;
    private double dampingFactor;
    private boolean showMessage;
    private int maxIteration;

    public PowerMethod(double tolerance, double dampingFactor) {
        this.tolerance =tolerance;
        this.dampingFactor =dampingFactor;
        showMessage=true;
        maxIteration=50;
    }

    public void setMaxIteration(int iteration){
        this.maxIteration =iteration;
    }

    public int getMaxIteration(){
        return maxIteration;
    }

    /**
     * Get the largest eigenvector which satisifies p=(dU+(1-d)M)'*p
     * @param matrix: a non-negative square matrix M.
     * @return the largest eigenvector.
     */
    public DoubleVector getEigenVector(SparseMatrix matrix){
        DoubleVector oldVector, newVector;
        double[] arrRate;
        double d, constFactor,distance;
        int i, j, index, len,k;

        oldVector=new DoubleVector(Math.max(matrix.rows(),matrix.columns()));
        newVector=new DoubleVector(oldVector.size());
        oldVector.assign(1.0/oldVector.size());

        //arrRate[i] is for normalizing row i
        arrRate=new double[matrix.rows()];
        for(i=0;i<matrix.rows();i++){
            arrRate[i]=0;
            len=matrix.getNonZeroNumInRow(i);
            //get the summation of each row
            for(j=0;j<len;j++)
                arrRate[i]+=matrix.getNonZeroDoubleScoreInRow(i,j);
            arrRate[i]=(1-dampingFactor)/arrRate[i];
        }

        // dampingFactor/N
        constFactor=dampingFactor/matrix.rows();

        k=0;
        while(k<maxIteration){
            if(showMessage)
                System.out.println(new java.util.Date()+" iteration "+k);
            k++;
            newVector.assign(0);
            for(i=0;i<matrix.rows();i++){
                len=matrix.getNonZeroNumInRow(i);
                for(j=0;j<len;j++){
                    d=arrRate[i]* matrix.getNonZeroDoubleScoreInRow(i, j)+constFactor;
                    index=matrix.getNonZeroColumnInRow(i, j);
                    newVector.set(index,newVector.get(index) +d*oldVector.get(i));
                }
            }
            distance = newVector.distance(oldVector);
            System.out.println(distance);
            if(distance<tolerance)
                break;
            oldVector.assign(newVector);
        }
        return newVector;
    }

    public DoubleVector getEigenVector(DenseMatrix matrix){
        DoubleVector oldVector, newVector;
        double[] arrRate;
        double d, constFactor;
        int i, j, k;

        if(matrix.rows()!=matrix.columns())
            return null;

        oldVector=new DoubleVector(matrix.rows());
        newVector=new DoubleVector(matrix.rows());
        oldVector.assign(1.0/matrix.rows());

        //arrRate[i] is for normalizing row i
        arrRate=new double[matrix.rows()];
        for(i=0;i<matrix.rows();i++){
            arrRate[i]=0;
            //get the summation of each row
            for(j=0;j<matrix.rows();j++)
                arrRate[i]+=matrix.getDouble(i,j);
            arrRate[i]=(1-dampingFactor)/arrRate[i];
        }

        // dampingFactor/N
        constFactor=dampingFactor/matrix.rows();

        k=0;
        while(k<maxIteration){
            if(showMessage)
                System.out.println(new java.util.Date()+" iteration "+k);
            k++;
            newVector.assign(0);
            for(i=0;i<matrix.rows();i++){
                for(j=0;j<matrix.rows();j++){
                    d=arrRate[i]* matrix.getDouble(i, j)+constFactor;
                    newVector.set(j, newVector.get(j) + d * oldVector.get(i));
                }
            }
            if(newVector.distance(oldVector)<tolerance)
                break;
            oldVector.assign(newVector);
        }
        return newVector;
    }

    public void setMessageOption(boolean option){
        this.showMessage=option;
    }
}