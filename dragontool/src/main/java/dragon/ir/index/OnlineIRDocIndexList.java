package dragon.ir.index;

import java.util.ArrayList;

/**
 * <p>The class is used to add or get a given IRDoc </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineIRDocIndexList implements IRDocIndexList{
    private ArrayList indexList;
    private int lastDocIndex;

    public OnlineIRDocIndexList() {
        indexList=new ArrayList(200);
        lastDocIndex=-1;
    }

    public IRDoc get(int index){
        return index<indexList.size()?(IRDoc)indexList.get(index):null;
    }

    public boolean add(IRDoc curDoc){
        int i;

        if(curDoc.getIndex()<=lastDocIndex)
            return false;
        for(i=lastDocIndex+1;i<curDoc.getIndex();i++){
            indexList.add(new IRDoc(i));
        }
        indexList.add(curDoc.copy());
        lastDocIndex=curDoc.getIndex();
        return true;
    }

    public void close(){
        indexList.clear();
    }

    public int size(){
        return indexList.size();
    }
}