package dragon.ir.classification.multiclass;

/**
 * <p>Interface for Multi-class Decoder</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface MultiClassDecoder {
    /**
     * Predicts the label of the example given the code matrix and the results of all binary classifiers
     * @param matrix the code matrix which describes the relationship between binary classifiers and categories
     * @param binClassifierResults the predictions of all binary classifiers
     * @return the category the example should be associated with
     */
    public int decode(CodeMatrix matrix, double[] binClassifierResults);
    
    /**
     * Rank all class labels
     * @return the ranking of class labels. The first and last element of the returned array contain the most possible 
     * label and the least possible label, respectively.
     */
    public int[] rank();
}