package dragon.nlp.tool.xtract;

import dragon.nlp.Sentence;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface WordPairGenerator {
    public int generate(Sentence sent);
    public WordPairStat getWordPairs(int index);
    public void setMaxSpan(int maxSpan);
}