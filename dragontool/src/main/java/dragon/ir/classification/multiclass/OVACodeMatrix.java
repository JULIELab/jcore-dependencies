package dragon.ir.classification.multiclass;

/**
 * <p>One-versus-all Code Matrix</p>
 * <p>For a classification problem with n categories, there are total n binary classifiers. For the k-th binary classifier, examples of
 * category k will be used as postive examples and all other examples used as negative examples.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OVACodeMatrix extends AbstractCodeMatrix implements java.io.Serializable{
	private static final long serialVersionUID = 1L;

	public OVACodeMatrix(){
    	this(1);
    }
    
	public OVACodeMatrix(int classNum) {
        super(classNum);
        setClassNum(classNum);
    }

    public int getCode(int classIndex, int classifierIndex){
        if(classIndex==classifierIndex)
            return 1;
        else
            return -1;
    }

    public void setClassNum(int classNum){
        this.classifierNum =classNum;
        this.classNum =classNum;
    }

}