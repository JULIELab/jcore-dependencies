package dragon.ir.topicmodel;

/**
 * <p>Interface of two-dimensional topic models (in the progress)</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface TwoDimensionModel {
    public boolean estimateModel(int viewNum, int topicNum);
    public int getViewNum();
    public int getTopicNum();
    public double[] getView(int viewIndex);
    public double[] getCommonTopic(int topicIndex);
    public double[] getViewTopic(int viewIndex, int topicIndex);
    public int getDocNum();
    public double[] getDocViews(int docIndex);
    public double[] getDocTopics(int docIndex,int viewIndex);
    public int getViewTermNum();
    public int getTopicTermNum();
    public String getViewTermName(int termIndex);
    public String getTopicTermName(int termIndex);
}