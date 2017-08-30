package dragon.ir.index.sentence;

import java.util.TreeMap;
/**
 * <p>Base sentence information class </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSentenceBase {
    private TreeMap map;

    public OnlineSentenceBase() {
        map=new TreeMap();
    }

    public boolean add(String sentence, String sentKey){
        if(map.containsKey(sentKey))
            return false;
        map.put(sentKey,sentence);
        return true;
    }

    public String get(String sentKey){
        return (String)map.get(sentKey);
    }
}