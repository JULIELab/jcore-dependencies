package dragon.ir.kngbase;

import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.SimpleElementList;
/**
 * <p>Basic knowledge base</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicKnowledgeBase implements KnowledgeBase {
    private DoubleSparseMatrix transMatrix;
    private SimpleElementList rowKeyList, columnKeyList;

    public BasicKnowledgeBase(DoubleSparseMatrix transMatrix,  SimpleElementList rowKeyList, SimpleElementList columnKeyList) {
        this.transMatrix =transMatrix;
        this.rowKeyList =rowKeyList;
        this.columnKeyList =columnKeyList;
    }

    public DoubleSparseMatrix getKnowledgeMatrix(){
        return transMatrix;
    }

    public SimpleElementList getRowKeyList(){
        return rowKeyList;
    }

    public SimpleElementList getColumnKeyList(){
        return columnKeyList;
    }
}