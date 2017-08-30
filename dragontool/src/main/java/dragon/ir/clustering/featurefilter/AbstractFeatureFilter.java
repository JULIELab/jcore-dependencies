package dragon.ir.clustering.featurefilter;

import dragon.ir.index.*;
import dragon.util.MathUtil;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractFeatureFilter implements FeatureFilter{
    protected int[] featureMap;
    protected int selectedFeatureNum;

    protected abstract int[] getSelectedFeatures(IndexReader indexReader, IRDoc[] docSet);

    public void initialize(IndexReader indexReader, IRDoc[] docSet){
        setSelectedFeatures(getSelectedFeatures(indexReader,docSet));
    }

    protected void setSelectedFeatures(int[] selectedFeatures){
        int i, oldFeatureNum;

        if(selectedFeatures==null)
            return;
        oldFeatureNum=selectedFeatures[selectedFeatures.length-1]+1;
        featureMap=new int[oldFeatureNum];
        MathUtil.initArray(featureMap,-1);
        for(i=0;i<selectedFeatures.length;i++)
            featureMap[selectedFeatures[i]]=i;
        selectedFeatureNum=selectedFeatures.length;
    }

    public boolean isSelected(int originalFeatureIndex){
        if(originalFeatureIndex>=featureMap.length)
            return false;
        else
            return featureMap[originalFeatureIndex]!=-1;
    }

    public int map(int originalFeatureIndex){
        if(originalFeatureIndex>=featureMap.length)
            return -1;
        else
            return featureMap[originalFeatureIndex];
    }

    public int getSelectedFeatureNum(){
        return selectedFeatureNum;
    }
}