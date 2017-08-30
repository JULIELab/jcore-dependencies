package dragon.ir.classification.multiclass;

/**
 * <p>Abstract Code Matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractCodeMatrix implements CodeMatrix, java.io.Serializable {
    protected int classNum;
    protected int classifierNum;

    public AbstractCodeMatrix(int classNum) {
        this.classNum =classNum;
    }

    public int getClassNum(){
        return classNum;
    }

    public int getClassifierNum(){
        return classifierNum;
    }
}