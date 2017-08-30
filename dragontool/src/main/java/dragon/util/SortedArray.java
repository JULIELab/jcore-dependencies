package dragon.util;

import java.util.*;
/**
 * <p>A cute sorted array class which implements binary tree searching </p>
 * <p> It only store object with unique value. For store and sort dublicating data,
 * you can first store data to java.util.ArrayList and then call java.util.Collections to sort </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SortedArray extends ArrayList{
	private static final long serialVersionUID = 1L;
	private Comparator comparator;
    private int insertedPos;

    public SortedArray(int capacity, Comparator comparator) {
        super(capacity);
        this.comparator=comparator;
    }

    public SortedArray(int capacity) {
        super(capacity);
        this.comparator=null;

    }

    public SortedArray(Comparator comparator) {
        super();
        this.comparator=comparator;
    }

    public SortedArray() {
        super();
        this.comparator=null;
    }

    public int insertedPos(){
        return insertedPos;
    }

    public boolean add(Object key){
        int pos;

        if(comparator==null)
            pos=Collections.binarySearch(this,key);
        else
            pos=Collections.binarySearch(this,key,comparator);
        if(pos<0){
            insertedPos = pos * ( -1) - 1;
            super.add(insertedPos,key);
            return true;
        }
        else{
            insertedPos = pos;
            return false;
        }
    }

    public int binarySearch(Object key){
        if(comparator==null)
            return Collections.binarySearch(this,key);
        else
           return Collections.binarySearch(this,key,comparator);
    }

    public int binarySearch(Object key, int start){
        return binarySearch(this,key,start,this.size()-1,comparator);
    }

    public int binarySearch(Object key, int start, int end){
        return binarySearch(this,key,start,end, comparator);
    }

    public boolean contains(Object key){
        int pos;

        if(comparator==null)
            pos=Collections.binarySearch(this,key);
        else
            pos=Collections.binarySearch(this,key,comparator);
        return pos>=0;
    }

    public SortedArray copy(Comparator comparator){
        SortedArray newList;

        newList=new SortedArray();
        newList.addAll(this);
        newList.setComparator(comparator);
        return newList;
    }

    public SortedArray copy(){
        return copy(null);
    }

    public Comparator getComparator(){
        return comparator;
    }

    public void setComparator(Comparator comparator){
        this.comparator =comparator;
        if(comparator==null)
            Collections.sort(this);
        else
            Collections.sort(this,comparator);
    }

    public static int binarySearch(List list, Object obj){
        return binarySearch(list,obj,0,list.size()-1,null);
    }

    public static int binarySearch(List list, Object obj, Comparator comparator){
        return binarySearch(list,obj,0,list.size()-1,comparator);
    }

    public static int binarySearch(List list, Object obj, int start, int end){
        return binarySearch(list,obj,start,end,null);
    }

    public static int binarySearch(List list, Object obj, int start, int end, Comparator comparator){
        int middle, retvalue;

        while(start<=end)
        {
            middle=(start+end)/2;
            if(comparator==null)
                retvalue=((Comparable)obj).compareTo(list.get(middle));
            else
                retvalue=comparator.compare(obj,list.get(middle));
            if(retvalue==0)
                return middle;
            else if(retvalue>0)
                start=middle+1;
            else
                end=middle-1;
        }
        return -(start+1);
    }

}