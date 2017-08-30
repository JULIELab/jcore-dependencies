package dragon.ir.classification.multiclass;

/**
 * <p>Loss Function Interface</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface LossFunction {
    /**
     * Gets the loss value
     * @param val input value
     * @return the loss value
     */
    public double loss(double val);
}