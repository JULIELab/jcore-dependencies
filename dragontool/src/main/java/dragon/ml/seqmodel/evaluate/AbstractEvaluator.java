package dragon.ml.seqmodel.evaluate;

/**
 * <p>Abstract class of evaluation</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractEvaluator implements Evaluator{
    protected int totalLabels;
    protected int annotatedLabels;
    protected int correctAnnotatedLabels;

    public AbstractEvaluator() {
    }

    public int totalLabels(){
        return totalLabels;
    }

    public int annotatedLabels(){
        return annotatedLabels;
    }

    public int correctAnnotatedLabels(){
        return correctAnnotatedLabels;
    }

    public double precision(){
        return (double)correctAnnotatedLabels/annotatedLabels;
    }
    public double recall(){
        return (double)correctAnnotatedLabels/totalLabels;
    }
}