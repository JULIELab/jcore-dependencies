package dragon.nlp;

import dragon.nlp.compare.*;
/**
 * <p>This is a light data structure for pair data</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimplePair implements IndexSortable, Comparable{
    private static long hashCapacity=16385;
    private int index;
    private int first, second;


    public SimplePair(int index, int firstElement, int secondElement) {
        this.index=index;
        this.first=firstElement;
        this.second=secondElement;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index =index;
    }

    public int getFirstElement(){
        return first;
    }

    public int getSecondElement(){
        return second;
    }

    public int compareTo(Object obj){
        int indexObj;

        indexObj=((SimplePair)obj).getFirstElement();
        if(first==indexObj){
            indexObj=((SimplePair)obj).getSecondElement();
            if(second==indexObj)
                return 0;
            else if(second>indexObj)
                return 1;
            else
                return -1;
        }
        else if(first>indexObj)
            return 1;
        else
            return -1;
    }

    public boolean equals(Object obj){
        int indexObj;
        indexObj = ((SimplePair)obj).getFirstElement();
        if(first==indexObj){
             indexObj=((SimplePair)obj).getSecondElement();
             if(second==indexObj)
                return true;
            else
                return false;
        }
        return false;
    }

    public int hashCode(){
        long code;

        code=first*hashCapacity+second;
        return (int)code;
    }

    static public void setHashCapacity(int capacity){
        hashCapacity =capacity;
    }

    static public int getHashCapacity(){
        return (int)hashCapacity;
    }
}
