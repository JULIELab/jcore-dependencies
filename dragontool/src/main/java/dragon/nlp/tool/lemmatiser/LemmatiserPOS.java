package dragon.nlp.tool.lemmatiser;

import dragon.util.SortedArray;
/**
 * <p>Part of speech lemmtiser </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LemmatiserPOS {
    private Operation[] operations;
    private int pos;
    private SortedArray indexList;

    public LemmatiserPOS(int POS, Operation[] operations) {
        this.operations =operations;
        this.pos=POS;
        indexList=null;
    }

    public LemmatiserPOS(int POS, Operation[] operations, SortedArray indexList) {
        this.operations =operations;
        this.pos=POS;
        this.indexList=indexList;
    }

    public String lemmatise(String derivation){
        int i;
        boolean indexChecked;
        String base;

        indexChecked=false;
        for(i=0;i<operations.length;i++){
            base=operations[i].execute(derivation);
            if(base!=null){
                indexChecked=true;
                if(!operations[i].getIndexLookupOption() || indexList == null || indexList.contains(base))
                    return base;
            }
        }
        if(indexChecked)
            return null;
        else{
            if(indexList!=null && indexList.contains(derivation))
                return derivation;
            else
                return null;
        }
    }

    public int getPOSIndex(){
        return pos;
    }

    public SortedArray getIndexList(){
        return indexList;
    }
}