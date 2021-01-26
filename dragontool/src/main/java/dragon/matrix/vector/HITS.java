package dragon.matrix.vector;

import dragon.matrix.SparseMatrix;

/**
 * <p>HITS algorithm</p>
 * <p>Reference:J. Kleinberg. Authoritative sources in a hyperlinked environment.
 * In Proc. Ninth Ann. ACM-SIAM Symp. Discrete Algorithms, pages 668-677, ACM Press, New York, 1998 </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class HITS implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private static final int DEFAULTITERATION=20;
    private int numOfIteration;
    private boolean edgeWeightIsCounted;
    private boolean authorityPriorIsSet;
    private boolean hubPriorIsSet;
    private boolean showMessage;
    private DoubleVector authorityVector, hubVector;

    public HITS(boolean edgeWeightIsCounted){
         this(edgeWeightIsCounted, DEFAULTITERATION);
    }

    public HITS( boolean edgeWeightIsCounted, int numOfIteration) {
        this.edgeWeightIsCounted=edgeWeightIsCounted;
        this.numOfIteration=numOfIteration;
        authorityPriorIsSet=false;
        hubPriorIsSet=false;
        showMessage=true;
    }

    public void computeAuthorityHub(SparseMatrix matrix){
        computeAuthorityHub(matrix,(SparseMatrix)matrix.transpose());
    }

    public void computeAuthorityHub(SparseMatrix matrix,SparseMatrix tMatrix) {
        double sum, scoreList[];
        int i, j, k, indexList[];

        //initilization
        if(!authorityPriorIsSet){
            authorityVector = new DoubleVector(matrix.columns());
            authorityVector.assign(1);}

        if(!hubPriorIsSet){
            hubVector = new DoubleVector(matrix.rows());
            hubVector.assign(1);
        }
        scoreList=null;

        for (k = 0; k < numOfIteration; k++) {
            if(showMessage)
                System.out.println(new java.util.Date()+" iteration "+k);
            k++;

            //I operation
            for (i = 0; i < tMatrix.rows(); i++) {
                indexList = tMatrix.getNonZeroColumnsInRow(i);
                if(edgeWeightIsCounted)
                    scoreList = tMatrix.getNonZeroDoubleScoresInRow(i);
                sum = 0;
                for (j = 0; j < indexList.length; j++){
                    if (edgeWeightIsCounted)
                        sum += scoreList[j] * hubVector.get(indexList[j]);
                    else
                        sum += hubVector.get(indexList[j]);
                }
                authorityVector.set(i, sum);
            }
            authorityVector.multiply(1.0/authorityVector.distance());

            //O operation
            for (i = 0; i < matrix.rows(); i++) {
                indexList = matrix.getNonZeroColumnsInRow(i);
                if(edgeWeightIsCounted)
                    scoreList = matrix.getNonZeroDoubleScoresInRow(i);
                sum = 0;
                for (j = 0; j < indexList.length; j++) {
                    if(edgeWeightIsCounted)
                        sum += scoreList[j] * authorityVector.get(indexList[j]);
                    else
                        sum += authorityVector.get(indexList[j]);
                }
                hubVector.set(i, sum);
            }
            hubVector.multiply(1.0/hubVector.distance());
        }
    }

    public void setAuthorityVectorPrior(DoubleVector authorityVector){
        this.authorityVector=authorityVector;
        authorityPriorIsSet=true;
    }

    public void setHubVectorPrior(DoubleVector hubVector){
       this.hubVector=hubVector;
       hubPriorIsSet=true;
   }

    public DoubleVector getAuthorityVector(){
        return authorityVector;
    }

    public DoubleVector getHubVector(){
        return hubVector;
    }

    public void setMessageOption(boolean option){
        this.showMessage = option;
    }

}
