package dragon.nlp.compare;

import dragon.nlp.Concept;
import java.util.Comparator;

/**
 * <p>Compare concept entry ID of two objects </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ConceptEntryIDComparator implements Comparator, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public int compare(Object firstObj, Object secondObj){
        Concept first, second;
        String cui1, cui2;

        first=(Concept)firstObj;
        cui1=first.getEntryID();
        second=(Concept)secondObj;
        cui2=second.getEntryID();
        if(cui1!=null && cui2!=null)
            return cui1.compareTo(cui2);
        else if(cui1==null && cui2==null)
            return first.getName().compareToIgnoreCase(second.getName());
        else if(cui1==null)
            return -1;
        else
            return 1;
    }

    public boolean equals(Object obj){
        return false;
    }
}
