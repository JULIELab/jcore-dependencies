package dragon.ir.kngbase;

import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.SimpleElementList;

/**
 * <p>Interface of knowledge base</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface KnowledgeBase {
    /**
     * A knowledge matrix saves the semantic relationships between various concepts. For example, a row may stand for a multiword phrase,
     * e.g. space program and a column denote a signle word. Thus a cell in the matrix may be interpreted as the probability of the phrase
     *  beging translated to the single word semantically.
     * @return the knowledge matrix
     */
    public DoubleSparseMatrix getKnowledgeMatrix();

    /**
     * @return a list of concept names corresponding to each row
     */
    public SimpleElementList getRowKeyList();

    /**
     * @return a list of concept names corresponding to each column
     */
    public SimpleElementList getColumnKeyList();
}