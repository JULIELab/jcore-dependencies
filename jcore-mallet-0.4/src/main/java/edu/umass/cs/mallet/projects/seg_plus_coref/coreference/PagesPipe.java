package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

public class PagesPipe extends Pipe
{
	StringDistance distMetric;
	double threshold = 0.5;

	public PagesPipe(double threshold)
	{
		this.threshold = threshold;
	}

	public PagesPipe(StringDistance dm) 
	{
		this.distMetric = dm;
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)pair.getObject1();
		Citation s2 = (Citation)pair.getObject2();

		String ss1 = s1.getField(Citation.pages);
		String ss2 = s2.getField(Citation.pages);

		if(!ss1.equals("") && !ss2.equals("")){
//			System.out.println(ss1);	
//			System.out.println(ss2);			

			double dist = distMetric.score(ss1, ss2);	

			pair.setFeatureValue("PagesSimilarity", dist);
		}
		

		return carrier;
	}


}
