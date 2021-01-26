package dragon.ml.seqmodel.crf;

import dragon.matrix.DoubleDenseMatrix;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;
import dragon.util.MathUtil;

/**
 * <p>Abstract class for training conditional random field</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractTrainer extends AbstractCRF implements Trainer{
    protected static double xtol = 1.0e-16; // machine precision
    protected boolean doScaling;
    protected int maxIteration;

    public AbstractTrainer(ModelGraph model, FeatureGenerator featureGen) {
        super(model,featureGen);
        doScaling=true;
        maxIteration=100;
    }

    public boolean needScaling(){
        return doScaling;
    }

    public void setScalingOption(boolean option){
        this.doScaling =option;
    }

    public int getMaxIteration(){
        return maxIteration;
    }

    public void setMaxIteration(int maxIteration){
        this.maxIteration =maxIteration;
    }

    protected void genStateVector(DoubleDenseMatrix transMatrix, double[] oldStateVector, double[] newStateVector, boolean transpose) {
        // new = M *old + new
        int i, j, c, r;

        for (j = 0; j < transMatrix.columns(); j++) {
            for (i = edgeGen.first(j); i < transMatrix.rows(); i = edgeGen.next(j, i)) {
                r = i;
                c = j;
                if (transpose) {
                    r = j;
                    c = i;
                }
                newStateVector[r] += transMatrix.getDouble(i, j) * oldStateVector[c];
            }
        }
    }

    protected void genStateVectorLog(DoubleDenseMatrix transMatrix, double[] oldStateVector, double[] newStateVector, boolean transpose){
        // new = M *old + new
        // in log domain this becomes:

        for (int j = 0; j <transMatrix.columns(); j++) {
            for (int i = edgeGen.first(j); i < transMatrix.rows(); i = edgeGen.next(j,i)) {
                int r = i;
                int c = j;
                if (transpose) {
                    r = j;
                    c = i;
                }
                newStateVector[r]=MathUtil.logSumExp(newStateVector[r], transMatrix.getDouble(i,j)+oldStateVector[c]);
            }
        }
    }


}