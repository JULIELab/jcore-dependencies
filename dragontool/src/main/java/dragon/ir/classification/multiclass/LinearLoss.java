package dragon.ir.classification.multiclass;

/**
 * <p>Linear Loss Function</p>
 * <p>The linear loss is defined as -x for an input x</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LinearLoss implements LossFunction, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public double loss(double val){
        return -val;
    }


}