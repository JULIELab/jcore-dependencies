package dragon.nlp.compare;

import java.util.Comparator;
/**
 * <p>Comparing weight of two objects </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class WeightComparator implements Comparator, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private double direction;

    public WeightComparator() {
        direction = -1.0;
    }

    public WeightComparator(boolean reversed) {
        if (reversed)
            direction = -1.0;
        else
            direction = 1.0;
    }

    public int compare(Object firstObj, Object secondObj) {
        double weight1, weight2;
        weight1 = ( (WeightSortable) firstObj).getWeight() * direction;
        weight2 = ( (WeightSortable) secondObj).getWeight() * direction;
        if (weight1 < weight2)
            return -1;
        else if (weight1 == weight2)
            return 0;
        else
            return 1;
    }
}