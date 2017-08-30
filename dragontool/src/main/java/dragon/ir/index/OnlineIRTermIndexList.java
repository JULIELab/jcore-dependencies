package dragon.ir.index;

import java.util.ArrayList;
/**
 * <p>The class is used to add or get a given IR term </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineIRTermIndexList implements IRTermIndexList, IRSignatureIndexList{
    private ArrayList indexList;

    public OnlineIRTermIndexList() {
        indexList=new ArrayList(2000);
    }

    public IRSignature getIRSignature(int index){
        return get(index);
    }

    public IRTerm get(int index){
        return index<indexList.size()?(IRTerm)indexList.get(index):null;
    }

    public boolean add(IRTerm curTerm){
        IRTerm oldTerm;

        if(curTerm.getIndex()<indexList.size()){
            oldTerm=(IRTerm)indexList.get(curTerm.getIndex());
            oldTerm.addFrequency(curTerm.getFrequency());
            oldTerm.setDocFrequency(oldTerm.getDocFrequency()+curTerm.getDocFrequency());
        }
        else
        {
            for(int i=indexList.size();i<curTerm.getIndex();i++){
                indexList.add(new IRTerm(i,0,0));
            }
            curTerm=curTerm.copy();
            indexList.add(curTerm);
        }
        return true;
    }

    public void close(){
        indexList.clear();
    }

    public int size(){
        return indexList.size();
    }
}