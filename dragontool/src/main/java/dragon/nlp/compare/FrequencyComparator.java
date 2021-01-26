package dragon.nlp.compare;

import java.util.Comparator;

/**
 * <p>Comparing frequency of two objects</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FrequencyComparator implements Comparator, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int direction;

    public FrequencyComparator() {
        direction = -1;
    }

    public FrequencyComparator(boolean reversed) {
        if (reversed)
            direction = -1;
        else
            direction = 1;
    }

    public int compare(Object firstObj, Object secondObj){
        int freq1, freq2;
        freq1=((FrequencySortable)firstObj).getFrequency()*direction;
        freq2=((FrequencySortable)secondObj).getFrequency()*direction;
        if(freq1<freq2)
            return -1;
        else if(freq1>freq2)
            return 1;
        else
            return 0;
    }

    public boolean equals(Object obj){
        return false;
    }



}