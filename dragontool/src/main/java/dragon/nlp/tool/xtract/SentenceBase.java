package dragon.nlp.tool.xtract;

import dragon.matrix.*;
import dragon.nlp.*;
/**
 * <p>Sentence base class provides functions of adding and saving sententce to matrix</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SentenceBase {
    private SparseMatrixFactory factory;
    private IntFlatSparseMatrix cacheMatrix;
    private String indexFile;
    private int sentenceNum, threshold;

    public SentenceBase(String indexFile, String matrixFile) {
        this.indexFile =indexFile;
        factory=new SparseMatrixFactory(matrixFile,4);
        cacheMatrix=new IntFlatSparseMatrix();
        sentenceNum=factory.rows();
        threshold=500000;
    }

    public int addSentence(Sentence sent){
        Word cur;
        int sentIndex;

        sentIndex=sentenceNum;
        sentenceNum++;
        cur=sent.getFirstWord();
        while(cur!=null){
            cacheMatrix.add(sentIndex,cur.getIndex(),cur.getPOSIndex());
            cur=cur.next;
        }

        if(cacheMatrix.getNonZeroNum()>=threshold)
        {
            cacheMatrix.finalizeData(false);
            factory.add(cacheMatrix);
            cacheMatrix.close();
        }
        return sentIndex;
    }

    public void close(){
        cacheMatrix.finalizeData(false);
        factory.add(cacheMatrix);
        cacheMatrix.close();
        factory.genIndexFile(indexFile);
    }
}