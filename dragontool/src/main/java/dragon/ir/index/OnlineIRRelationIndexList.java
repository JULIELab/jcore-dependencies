package dragon.ir.index;

import java.util.ArrayList;

/**
 * <p>The class is used to add or get a given IR relation </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineIRRelationIndexList implements IRRelationIndexList, IRSignatureIndexList{
    private ArrayList indexList;

    public OnlineIRRelationIndexList() {
        indexList=new ArrayList(5000);
    }

    public IRSignature getIRSignature(int index){
        return get(index);
    }

    public IRRelation get(int index) {
        return index<indexList.size()?(IRRelation)indexList.get(index):null;
    }

    public boolean add(IRRelation curRelation) {
        IRRelation oldRelation;

        if (curRelation.getIndex()<indexList.size()) {
            oldRelation = (IRRelation) indexList.get(curRelation.getIndex());
            oldRelation.addFrequency(curRelation.getFrequency());
            oldRelation.setDocFrequency(oldRelation.getDocFrequency() + curRelation.getDocFrequency());
        }
        else {
            for(int i=indexList.size();i<curRelation.getIndex();i++){
                indexList.add(new IRRelation(i,0,0,-1,-1));
            }
            curRelation=curRelation.copy();
            indexList.add(curRelation);
        }
        return true;
    }

    public int size() {
        return indexList.size();
    }

    public void close() {
        indexList.clear();
    }
}