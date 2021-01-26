package dragon.nlp.tool.lemmatiser;

import dragon.util.SortedArray;

/**
 * <p>The verb operation removes the suffix such as -s, -es, -ed, -ing</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class VerbOperation implements Operation{
    private SortedArray verbIndexList;

    public VerbOperation(SortedArray verbIndexList) {
        this.verbIndexList =verbIndexList;
    }

    public boolean getIndexLookupOption(){
        return false;
    }

    public String execute(String derivation){
        if(verbIndexList==null) return null;

        if(derivation.endsWith("ed")){
           derivation=derivation.substring(0,derivation.length()-2);
           if(verbIndexList.binarySearch(derivation)>0)
               return derivation;
           else
               return null;
        }
        else if(derivation.endsWith("ing")){
           derivation=derivation.substring(0,derivation.length()-3);
           if(verbIndexList.binarySearch(derivation)>0)
               return derivation;
           else
               return null;
        }
        return null;
    }

}