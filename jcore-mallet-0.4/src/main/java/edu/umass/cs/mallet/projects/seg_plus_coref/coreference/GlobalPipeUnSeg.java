package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

import java.util.Iterator;

public class GlobalPipeUnSeg extends Pipe
{

	StringDistance distMetric;
	boolean useNBest = false;

	public GlobalPipeUnSeg(StringDistance distMetric) {
		this.distMetric = distMetric;
	}
	
	public GlobalPipeUnSeg(StringDistance distMetric, boolean useNBest) {
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
		pair.setFeatureValue("SoftTFIDFGlobal", v);
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
				if (true) {
					pair.setFeatureValue("SoftTFIDFGlobal", dist);
				}
				if (false) {
					if (dist >= 0.9)
						pair.setFeatureValue("SoftTFIDFGlobalHIGH", dist);
					else if (dist > 0.75)
						pair.setFeatureValue("SoftTFIDFGlobalMED", dist);
					else if (dist > 0.5)
						pair.setFeatureValue("SoftTFIDFGlobalWEAK", dist);
					else if (dist > 0.3)
						pair.setFeatureValue("SoftTFIDFGlobalMIN", dist);
					else if (dist < 0.3)
						pair.setFeatureValue("SoftTFIDFGlobalNONE", dist);
				}
			}
		}
		return carrier;
	}
}
