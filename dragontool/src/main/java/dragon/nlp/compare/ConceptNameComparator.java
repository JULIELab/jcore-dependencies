package dragon.nlp.compare;

import dragon.nlp.Concept;
import java.util.Comparator;
/**
 * <p>Compare concept names of two objects</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ConceptNameComparator implements Comparator, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public int compare(Object firstObj, Object secondObj){
        String first, second;

        first=((Concept)firstObj).getName();
        second=((Concept)secondObj).getName();
        return first.compareToIgnoreCase(second);
    }

    public boolean equals(Object obj){
        return false;
    }

}
