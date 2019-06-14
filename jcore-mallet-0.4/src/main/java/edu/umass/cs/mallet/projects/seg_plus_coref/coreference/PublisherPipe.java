package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

public class PublisherPipe extends Pipe
{
	StringDistance distMetric;
	double threshold = 0.5;

	public PublisherPipe(double threshold)
	{
		this.threshold = threshold;
	}

	public PublisherPipe(StringDistance dm) 
	{
		this.distMetric = dm;
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)((Node)pair.getObject1()).getObject(); 
		Citation s2 = (Citation)((Node)pair.getObject2()).getObject();

		String ss1 = s1.getField(Citation.publisher);
		String ss2 = s2.getField(Citation.publisher);

		if(!ss1.equals("") && !ss2.equals("")){
//			System.out.println(ss1);	
//			System.out.println(ss2);			

			double dist = distMetric.score(ss1, ss2);	

			pair.setFeatureValue("PublisherSimilarity", dist);
		}
		

		return carrier;
	}


}
