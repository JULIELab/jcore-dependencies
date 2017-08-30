package dragon.nlp;

import dragon.nlp.compare.*;
/**
 * <p>This is a light data structor for data element that can be used for simple pair</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleElement implements IndexSortable, Comparable{
    private int index;
    private String key;

    public SimpleElement(String key, int index) {
        this.key = key;
        this.index = index;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key){
        this.key =key;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index){
        this.index =index;
    }

    public int compareTo(Object obj) {
        String objKey;

        objKey = ( (SimpleElement) obj).getKey();
        return key.compareToIgnoreCase(objKey);
    }
}