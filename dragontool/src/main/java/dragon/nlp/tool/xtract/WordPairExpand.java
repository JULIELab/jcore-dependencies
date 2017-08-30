package dragon.nlp.tool.xtract;

import java.util.ArrayList;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface WordPairExpand {
    /**
     * Expand the input word pair to a real phrase
     * @param wordPairStat the word pair object
     * @param span the distance between two words
     * @return a list of Token objects. Each token contain the extracted phrase and its frequency in the corpus
     */
    public ArrayList expand(WordPairStat wordPairStat, int span);
}