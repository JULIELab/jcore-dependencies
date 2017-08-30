package dragon.ml.seqmodel.feature;

/**
 * <p>Interface of Feature Dictionary</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface FeatureDictionary {
    public int getStateNum();
    public int getIndex(Object feature);
    public boolean contain(Object feature);
    public int getCount(Object feature);
    public int getCount(int featureIndex);
    public int getCount(int featureIndex, int label);
    public int getStateCount(int state);
    public int getTotalCount();
    public int size();
    public int getNextStateWithFeature(int index, int prevLabel);
    public int addFeature(Object feature, int label);
    public void finalize();
    public boolean read(String filename);
    public boolean write(String filename);
}