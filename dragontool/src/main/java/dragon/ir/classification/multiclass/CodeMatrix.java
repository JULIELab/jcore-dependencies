package dragon.ir.classification.multiclass;

/**
 * <p>Code Matrix Interface</p>
 * <p>The code matrix handle the problem of how to build a set of binary classifiers for multi-class classification. A row of the code
 * matrix corresponds to a class label and the column a binary classsifer. The cell value at row i and column j can be -1, 0, or +1. -1
 * means the training examples of category i will be used as negative examples for the j-th binary classifier. +1 * means the training
 * examples of category i will be used as positive examples for the j-th binary classifier. O means not used for training. <br><br>
 * More details can be found in the following paper:<br>
 * Allwein, E.L., Schapire, R.E., and Singer, Y., <em>Reducing multiclass to binary: A unifying approach for margin classifiers</em>,
 * Journal of Machine Learning Research, 1:113:C141, 2000.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface CodeMatrix {
    /**
     * Gets the number of categories
     * @return the number of categories
     */
    public int getClassNum();

    /**
     * Sets the number of categories
     * @param classNum the number of categories
     */
    public void setClassNum(int classNum);

    /**
     * Gets the number of binary classifiers
     * @return the number of binary classifiers
     */
    public int getClassifierNum();

    /**
     * Gets the code value for specified category and binary classifier
     * @param classIndex the index of the category
     * @param classifierIndex the index of the binary classifier
     * @return -1, 0, +1. -1 means the training examples of category i will be used as negative examples for the j-th binary classifier.
     * +1 means the training examples of category i will be used as positive examples for the j-th binary classifier. O means not used
     * for training.
     */
    public int getCode(int classIndex, int classifierIndex);
}