package dragon.ir.clustering.featurefilter;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IRTerm;
import dragon.ir.index.IndexReader;
import dragon.util.MathUtil;

import java.util.ArrayList;

/**
 * <p>Unsupervised Feature Selector which exclude features with its document frequency less than a given threshold</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocFrequencyFilter extends AbstractFeatureFilter {
    private int minDocFrequency;

    public DocFrequencyFilter(int minDocFrequency) {
        this.minDocFrequency =minDocFrequency;
    }

    protected int[] getSelectedFeatures(IndexReader indexReader, IRDoc[] docSet){
        ArrayList list;
        IRTerm curTerm;
        int[] featureMap, arrDocFreq;
        int i,termNum;

        termNum=indexReader.getCollection().getTermNum();
        if(docSet!=null && docSet.length<indexReader.getCollection().getDocNum()*0.67)
            arrDocFreq=computeTermCount(indexReader,docSet);
        else
            arrDocFreq=null;
        list=new ArrayList(termNum);
        for(i=0;i<termNum;i++){
            if(arrDocFreq!=null && arrDocFreq[i]==0)
                continue;
            curTerm=indexReader.getIRTerm(i);
            if(curTerm.getDocFrequency()>=minDocFrequency)
                list.add(curTerm);
        }
        featureMap=new int[list.size()];
        for(i=0;i<featureMap.length;i++)
            featureMap[i]=((IRTerm)list.get(i)).getIndex();
        return featureMap;
    }

    private int[] computeTermCount(IndexReader indexReader, IRDoc[] arrDoc){
        int[] arrIndex, buf;
        int j,k;

        buf=new int[indexReader.getCollection().getTermNum()];
        MathUtil.initArray(buf,0);
        for(j=0;j<arrDoc.length;j++){
            arrIndex = indexReader.getTermIndexList(arrDoc[j].getIndex());
            for (k = 0; k <arrIndex.length; k++)
                    buf[arrIndex[k]]+=1;
        }
        return buf;
    }
}