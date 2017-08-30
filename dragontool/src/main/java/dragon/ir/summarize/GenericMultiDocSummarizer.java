package dragon.ir.summarize;

import dragon.onlinedb.CollectionReader;

/**
 * <p>Interface of Generic Multi-Document Summarizer </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface GenericMultiDocSummarizer {
    /**
     * @param collectionReader the collection reader
     * @param maxLength the length of the summary
     * @return the summary in natural language.
     */
    public String summarize(CollectionReader collectionReader, int maxLength);
}