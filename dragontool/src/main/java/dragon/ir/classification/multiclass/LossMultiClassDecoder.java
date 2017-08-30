package dragon.ir.classification.multiclass;

import dragon.matrix.vector.DoubleVector;

/**
 * <p>Loss-based Multi-class Decoder</p>
 * <p>A multi-class decoder which predict the category of an example by minimizing the loss. More details can be found in the
 * following paper:<br>
 * Allwein, E.L., Schapire, R.E., and Singer, Y., "Reducing multiclass to binary: A unifying approach for margin classifiers,"
 * Journal of Machine Learning Research, 1:113:C141, 2000.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LossMultiClassDecoder implements MultiClassDecoder, java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private LossFunction lossFunc;
    private DoubleVector lossVector;
    private int[] rankings;

    public LossMultiClassDecoder(LossFunction lossFunc) {
        this.lossFunc =lossFunc;
    }

    public int decode(CodeMatrix matrix, double[] binClassifierResults){
        
        int i, j;

        if(binClassifierResults.length!=matrix.getClassifierNum()){
            System.out.println("The input data are not valid. Number of binary classifiers is not consistent.");
            return -1;
        }

        lossVector=new DoubleVector(matrix.getClassNum());
        for(i=0;i<matrix.getClassNum();i++){
            for(j=0;j<matrix.getClassifierNum();j++)
                lossVector.add(i,lossFunc.loss(matrix.getCode(i,j)*binClassifierResults[j]));
        }
        rankings=lossVector.rank(false);
        return rankings[0];
    }
    
    public int[] rank(){
    	return rankings;
    }
}