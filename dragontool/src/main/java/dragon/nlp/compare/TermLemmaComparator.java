package dragon.nlp.compare;

import dragon.nlp.Term;
import java.util.Comparator;
/**
 * <p>Compare lemma of two terms </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TermLemmaComparator implements Comparator, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public int compare(Object firstObj, Object secondObj){
        String first, second;

        first=((Term)firstObj).toLemmaString();
        second=((Term)secondObj).toLemmaString();
        return first.compareTo(second);
    }

    public boolean equals(Object obj){
        return false;
    }

}
