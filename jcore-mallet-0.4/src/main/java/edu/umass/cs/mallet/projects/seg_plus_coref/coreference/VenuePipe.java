package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

public class VenuePipe extends Pipe
{
	StringDistance distMetric;
	double threshold = 0.5;

	public VenuePipe(double threshold)
	{
		this.threshold = threshold;
	}

	public VenuePipe(StringDistance dm) 
	{
		this.distMetric = dm;
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)pair.getObject1();
		Citation s2 = (Citation)pair.getObject2();

		String ss1 = s1.getField(Citation.venue);
		String ss2 = s2.getField(Citation.venue);

		if(!ss1.equals("") && !ss2.equals("")){

			double dist = distMetric.score(ss1, ss2);	

			pair.setFeatureValue( "VenueSimilarity"+getFeatureNameFromScore( dist ), 1.0 );
		}
		return carrier;
	}


	private String getFeatureNameFromScore (double dist) {
		if (dist >= 0.9)
			return "HIGH";
		else if (dist > 0.75)
			return "MED";
		else if (dist > 0.5)
			return "WEAK";
		else if (dist > 0.3)
			return "MIN";
		else 
			return "NONE";
	}

}
