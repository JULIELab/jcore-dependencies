package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class GlobalPipe extends Pipe
{

	StringDistance distMetric;
	boolean useNBest = false;

	public GlobalPipe(StringDistance distMetric) {
		this.distMetric = distMetric;
	}
	
	public GlobalPipe(StringDistance distMetric, boolean useNBest) {
		this.distMetric = distMetric;
		this.useNBest = useNBest;
	}

	private double computeValue (Citation c1, Citation c2) {
		String ss1 = c1.getUnderlyingString();
		String ss2 = c2.getUnderlyingString();
		double dist = 0.0;

		if(!ss1.equals("") && !ss2.equals(""))
			dist = distMetric.score(ss1, ss2);
		return dist;
	}

	private void pipeNBest (NodePair pair) {
		Citation c1 = (Citation)pair.getObject1();
		Citation c2 = (Citation)pair.getObject2();
		double sum = 0.0;
		int num = 0;
		for (Iterator i1 = c1.getNBest().iterator(); i1.hasNext();) {
			Citation nth1 = (Citation)i1.next();
			for (Iterator i2 = c2.getNBest().iterator(); i2.hasNext();) {
				Citation nth2 = (Citation)i2.next();
				sum += computeValue (nth1, nth2);
				num++;
			}
		}
		double v = sum/(double)num;
		pair.setFeatureValue("GlobalDistance", v);
	}

	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		if (useNBest) {
			pipeNBest (pair);
		} else {
			Citation c1 = (Citation)pair.getObject1();
			Citation c2 = (Citation)pair.getObject2();
			String ss1 = c1.getUnderlyingString();
			String ss2 = c2.getUnderlyingString();
			
			if(!ss1.equals("") && !ss2.equals("")){
				double dist = distMetric.score(ss1, ss2);
				if (dist >= 0.9)
					pair.setFeatureValue("GlobalDistanceHIGH", 1.0);
				else if (dist > 0.75)
					pair.setFeatureValue("GlobalDistanceMED", 1.0);
				else if (dist > 0.5)
					pair.setFeatureValue("GlobalDistanceWEAK", 1.0);
				else if (dist > 0.3)
					pair.setFeatureValue("GlobalDistancelMIN", 1.0);
				else if (dist < 0.3)
					pair.setFeatureValue("GlobalDistanceNONE", 1.0);
			}
		}
		return carrier;
	}
}
