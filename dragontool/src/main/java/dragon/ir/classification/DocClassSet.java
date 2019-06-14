package dragon.ir.classification;

import dragon.ir.index.IRDoc;
/**
 * <p>The basic data structure for a set of classes for documents</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocClassSet {
    private int classNum;
    private DocClass[] arrClass;

    public DocClassSet(int classNum) {
        int i;

        this.classNum =classNum;
        arrClass=new DocClass[classNum];
        for(i=0;i<classNum;i++)
            arrClass[i]=new DocClass(i);
    }

    public DocClass getDocClass(int classID){
        return arrClass[classID];
    }

    public int getClassNum(){
        return classNum;
    }

    public boolean addDoc(int classID, IRDoc curDoc){
        if(classID>=classNum || classID<0)
            return false;
        return arrClass[classID].addDoc(curDoc);
    }
}