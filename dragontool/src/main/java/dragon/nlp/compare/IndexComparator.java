package dragon.nlp.compare;

import java.util.Comparator;

/**
 * <p>Compare index of two objects </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IndexComparator implements Comparator, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int direction;

    public IndexComparator(){
        direction=1;
    }

    public IndexComparator(boolean reversed){
        if (reversed)
            direction = -1;
        else
            direction = 1;
    }

    public int compare(Object firstObj, Object secondObj){
        int index1, index2;
        index1=((IndexSortable)firstObj).getIndex()*direction;
        index2=((IndexSortable)secondObj).getIndex()*direction;
        if(index1<index2)
            return -1;
        else if(index1>index2)
            return 1;
        else
            return 0;
    }

    public boolean equals(Object obj){
        return false;
    }


}