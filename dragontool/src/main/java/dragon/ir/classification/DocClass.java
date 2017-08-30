package dragon.ir.classification;

import dragon.ir.index.IRDoc;
import dragon.nlp.compare.IndexComparator;
import dragon.util.SortedArray;
/**
 * <p>The basic data structure for a class for documents </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocClass {
    private int classID;
    private String className;
    private SortedArray list;

    public DocClass(int classID) {
        this.classID =classID;
        className="Class #"+classID;
        list=new SortedArray(new IndexComparator());
    }

    public boolean addDoc(IRDoc doc){
        return list.add(doc);
    }

    public int getDocNum(){
        return list.size();
    }

    public IRDoc getDoc(int index){
        return (IRDoc)list.get(index);
    }

    public boolean contains(IRDoc curDoc){
        return list.contains(curDoc);
    }

    public int getClassID(){
        return classID;
    }

    public String getClassName(){
        return className;
    }

    public void setClassName(String className){
        this.className =className;
    }

    public void removeAll(){
        list.clear();
    }
}