package dragon.ir.summarize;

import dragon.ir.index.IndexReader;

import java.util.ArrayList;
/**
 * <p>Interface of structural summarizer </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface StructureSummarizer {
    /**
     * @return the index reader the current summarizer is working on
     */
    public IndexReader getIndexReader();

    /**
     * @param docSet a list of IRDoc objects
     * @return a structured summary
     */
    public TopicSummary summarize(ArrayList docSet);

    /**
     * The interpretation of the length is subject to the implementations.
     * @param docSet a list of IRDoc objects
     * @param maxLength the length of the summary
     * @return a structured summary
     */
    public TopicSummary summarize(ArrayList docSet, int maxLength);
}