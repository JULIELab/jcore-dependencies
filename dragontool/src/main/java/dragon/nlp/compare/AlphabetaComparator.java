package dragon.nlp.compare;

import java.util.Comparator;
/**
 * <p>Compare two objects alphabetically </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AlphabetaComparator implements Comparator, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int direction;

    public AlphabetaComparator(){
        direction=1;
    }

    public AlphabetaComparator(boolean reversed){
        if (reversed)
            direction = -1;
        else
            direction = 1;

    }

    public int compare(Object firstObj, Object secondObj){
        String first, second;

        first=firstObj.toString();
        second=secondObj.toString();
        return first.compareTo(second)*direction;
    }

    public boolean equals(Object obj){
        return false;
    }

}
