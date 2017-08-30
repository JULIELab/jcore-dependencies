package dragon.ir.index;

/**
 * <p>Interface of IRSignature Index List</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IRSignatureIndexList {
    public int size();
    public IRSignature getIRSignature(int index);
    public void close();
}