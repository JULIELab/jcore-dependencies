package dragon.ir.clustering;

import dragon.ir.index.*;
/**
 * <p>Data structure for a set of document clusters. </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocClusterSet {
    private int clusterNum;
    private DocCluster[] arrCluster;

    public DocClusterSet(int clusterNum) {
        int i;

        this.clusterNum =clusterNum;
        arrCluster=new DocCluster[clusterNum];
        for(i=0;i<clusterNum;i++)
            arrCluster[i]=new DocCluster(i);
    }

    public DocCluster getDocCluster(int clusterID){
        return arrCluster[clusterID];
    }

    public void setDocCluster(DocCluster docCluster, int clusterID){
        docCluster.setClusterID(clusterID);
        arrCluster[clusterID]=docCluster;
    }

    public int getClusterNum(){
        return clusterNum;
    }

    public boolean addDoc(int clusterID, IRDoc curDoc){
        if(clusterID>=clusterNum || clusterID<0)
            return false;
        else
            return arrCluster[clusterID].addDoc(curDoc);
    }

    public boolean removeDoc(int clusterID, IRDoc curDoc){
        if(clusterID>=clusterNum || clusterID<0)
            return false;
        else
            return arrCluster[clusterID].removeDoc(curDoc);
    }
}
