package dragon.ir.topicmodel;

import dragon.ir.index.IndexReader;
/**
 * <p>Abstract class of two dimensional topic model </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractTwoDimensionModel extends AbstractModel implements TwoDimensionModel{
    protected IndexReader viewIndexReader, topicIndexReader;
    protected int docNum, viewNum, themeNum, viewTermNum, themeTermNum;
    protected double[][] arrViewProb, arrDocView;
    protected double[][][] arrThemeProb, arrDocTheme;
    protected double[][] arrCommonThemeProb;

    public AbstractTwoDimensionModel(IndexReader viewIndexReader, IndexReader topicIndexReader) {
        this.viewIndexReader =viewIndexReader;
        this.topicIndexReader =topicIndexReader;
        docNum=Math.min(viewIndexReader.getCollection().getDocNum(),topicIndexReader.getCollection().getDocNum());
        viewTermNum=viewIndexReader.getCollection().getTermNum();
        themeTermNum=topicIndexReader.getCollection().getTermNum();
    }

    public int getViewNum(){
        return viewNum;
    }

    public int getTopicNum(){
        return themeNum;
    }

    public double[] getView(int viewIndex){
        return arrViewProb[viewIndex];
    }

    public double[] getCommonTopic(int topicIndex){
        return arrCommonThemeProb[topicIndex];
    }

    public double[] getViewTopic(int viewIndex, int topicIndex){
        return arrThemeProb[viewIndex][topicIndex];
    }

    public int getDocNum(){
        return docNum;
    }

    public double[] getDocViews(int docIndex){
        return arrDocView[docIndex];
    }

    public double[] getDocTopics(int docIndex,int viewIndex){
        return arrDocTheme[docIndex][viewIndex];
    }

    public int getViewTermNum(){
        return viewTermNum;
    }

    public int getTopicTermNum(){
        return themeTermNum;
    }

    public String getViewTermName(int termIndex){
        return viewIndexReader.getTermKey(termIndex);
    }

    public String getTopicTermName(int termIndex){
        return topicIndexReader.getTermKey(termIndex);
    }
}