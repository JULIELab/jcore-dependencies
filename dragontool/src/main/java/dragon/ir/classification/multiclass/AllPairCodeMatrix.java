package dragon.ir.classification.multiclass;

import dragon.matrix.IntFlatSparseMatrix;
/**
 * <p>Pairwise Code Matrix </p>
 * <p>Any two categories combine as a binary classifer in which one category is trained as postive examples and the other negative examples.
 * For a classification problem with n categories, there are total n(n-1)/2 binary classifiers.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AllPairCodeMatrix extends AbstractCodeMatrix implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private IntFlatSparseMatrix matrix;

    public AllPairCodeMatrix(){
    	this(1);
    }
    
    public AllPairCodeMatrix(int classNum) {
        super(classNum);
        setClassNum(classNum);
    }

    public void setClassNum(int classNum){
        int i, j;

        this.classNum =classNum;
        matrix=new IntFlatSparseMatrix();
        classifierNum=0;
        for(i=0;i<classNum-1;i++){
            for(j=i+1;j<classNum;j++){
                matrix.add(i,classifierNum,1);
                matrix.add(j,classifierNum,-1);
                classifierNum++;
            }
        }
        matrix.finalizeData();
    }

    public int getCode(int classIndex, int classifierIndex){
        return matrix.getInt(classIndex,classifierIndex);
    }

}