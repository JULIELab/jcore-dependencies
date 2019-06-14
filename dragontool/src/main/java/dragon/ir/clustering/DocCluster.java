package dragon.ir.clustering;

import dragon.ir.index.IRDoc;
import dragon.nlp.compare.IndexComparator;
import dragon.util.SortedArray;

import java.util.ArrayList;

/**
 * <p>Data structure for document cluster</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocCluster {
    private int clusterID;
    private String clusterName;
    private SortedArray list;

    public DocCluster(int clusterID) {
        this.clusterID =clusterID;
        this.clusterName =String.valueOf(clusterID);
        list=new SortedArray(new IndexComparator());
    }

    public boolean addDoc(IRDoc doc){
        doc.setCategory(clusterID);
        return list.add(doc);
    }

    public boolean removeDoc(IRDoc doc){
        int pos;

        pos=list.binarySearch(doc);
        if(pos<0)
            return false;
        else{
            list.remove(pos);
            doc.setCategory(-1);
            return true;
        }
    }
    public void removeAll(){
        list.clear();
    }

    public int getDocNum(){
        return list.size();
    }

    public IRDoc getDoc(int index){
        return (IRDoc)list.get(index);
    }

    public boolean containDoc(IRDoc doc){
        return list.contains(doc);
    }

    public ArrayList getDocSet(){
        return list;
    }

    public int getClusterID(){
        return clusterID;
    }

    public void setClusterID(int clusterID){
        this.clusterID =clusterID;
    }

    public String getClusterName(){
        return clusterName;
    }

    public void setClusterName(String clusterName){
        this.clusterName =clusterName;
    }
}
