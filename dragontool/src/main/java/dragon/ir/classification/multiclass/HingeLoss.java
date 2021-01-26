package dragon.ir.classification.multiclass;

/**
 * <p>Hinge Loss Function</p>
 * <p>The hinge loss function is defined as max{1-x, 0} for an input x</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class HingeLoss implements LossFunction, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public double loss(double val){
        return Math.max(1-val,0);
    }
}