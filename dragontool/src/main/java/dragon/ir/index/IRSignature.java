package dragon.ir.index;

/**
 * <p>Interface of IRSignature</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IRSignature {
    public int getIndex();
    public void setIndex(int index);
    public int getDocFrequency();
    public void setDocFrequency(int docFrequency);
    public int getFrequency();
    public void setFrequency(int frequency);
}