package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.Citation;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.NodePair;

/**
 * FieldStringDistancePipe
 * User: mhay
 * Email: mhay@cs.umass.edu
 * Date: Feb 18, 2004 4:40:24 PM
 */
public class YearsWithinFivePipe extends Pipe {

    public Instance pipe (Instance carrier) {
        NodePair pair = (NodePair)carrier.getData();
        Citation c1 = (Citation)pair.getObject1();
        Citation c2 = (Citation)pair.getObject2();

        String y1 = c1.getField(Citation.date);
        String y2 = c2.getField(Citation.date);

        if (y1.length() == 0 || y2.length() == 0) {
            return carrier;
        }

        int y1Int = (new Integer(y1)).intValue();
        int y2Int = (new Integer(y2)).intValue();

	if (!(y1Int == y2Int))
	    pair.setFeatureValue("YearsDoNotMatch", 1.0);

	if (Math.abs(y1Int - y2Int) < 1) 
	    pair.setFeatureValue("YearsMatch", 1.0);
	else if (Math.abs(y1Int - y2Int) < 2)
	    pair.setFeatureValue("Years1Apart", 1.0);
	else if (Math.abs(y1Int - y2Int) < 4)
	    pair.setFeatureValue("Years2to3Apart", 1.0);
        else if (Math.abs(y1Int - y2Int) > 3)
	    pair.setFeatureValue("YearsMoreThanThree", 1.0);
        return carrier;
    }
}
