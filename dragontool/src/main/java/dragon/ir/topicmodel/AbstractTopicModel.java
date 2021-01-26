package dragon.ir.topicmodel;

import dragon.ir.index.IndexReader;

/**
 * <p>Abstract class of topic model</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractTopicModel extends AbstractModel implements TopicModel{
    protected int themeNum, termNum, docNum;
    protected double[][] arrThemeTerm, arrDocTheme;
    protected IndexReader indexReader;

    public AbstractTopicModel(IndexReader indexReader) {
        this.indexReader =indexReader;
    }

    public int getTopicNum(){
        return themeNum;
    }

    public double[] getTopic(int topicIndex){
        return arrThemeTerm[topicIndex];
    }

    public int getDocNum(){
        return docNum;
    }

    public double[] getDocTopics(int docIndex){
        return arrDocTheme[docIndex];
    }

    public int getTermNum(){
        return termNum;
    }

    public String getTermName(int termIndex){
        return indexReader.getTermKey(termIndex);
    }
}