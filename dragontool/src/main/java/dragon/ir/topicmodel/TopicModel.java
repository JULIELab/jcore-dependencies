package dragon.ir.topicmodel;

/**
 * <p>Interface of topic models</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface TopicModel {
    /**
     * Estimate the topic models
     * @param topicNum the number of topics
     * @return true if estimating successfully
     */
    public boolean estimateModel(int topicNum);
    public int getTopicNum();

    /**
     * @param topicIndex the index of the topic
     * @return the distribution (over words) of the given topic
     */
    public double[] getTopic(int topicIndex);

    /**
     * @return the number of documents
     */
    public int getDocNum();

    /**
     * @param docIndex the index of the document
     * @return the distribution (over topics) of the given document
     */
    public double[] getDocTopics(int docIndex);

    /**
     * @return the number of topical terms
     */
    public int getTermNum();

    /**
     * @param termIndex the index of the topical term
     * @return the name of the given topical term
     */
    public String getTermName(int termIndex);
}