package dragon.ml.seqmodel.evaluate;

import dragon.ml.seqmodel.data.Dataset;

/**
 * <p>Interface of sequence labeling evaluator</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Evaluator {
    /**
     * Evalutes the machine-generated labels
     * @param human the dataset with human-annotated labels
     * @param machine the dataset with machine-annotated labels
     */
    public void evaluate(Dataset human, Dataset machine);

    /**
     * @return the number of total labels
     */
    public int totalLabels();

    /**
     * @return the number of lables set by machine
     */
    public int annotatedLabels();

    /**
     * @return the number of correct lables set by machine
     */
    public int correctAnnotatedLabels();

    public double precision();
    public double recall();
}