package dragon.ir.summarize;

import dragon.ir.index.IndexReader;

import java.util.ArrayList;
/**
 * <p>Abstract class of structure summarization given a document set </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractStructureSum {
    protected IndexReader indexReader;

    public abstract TopicSummary summarize(ArrayList docSet, int maxLength);

    public AbstractStructureSum(IndexReader indexReader) {
        this.indexReader =indexReader;
    }

    public IndexReader getIndexReader(){
        return indexReader;
    }

    public TopicSummary summarize(ArrayList docSet){
        return summarize(docSet, Integer.MAX_VALUE);
    }
}