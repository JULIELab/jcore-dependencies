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

public class TechPipe extends Pipe
{
	StringDistance distMetric;
	double threshold = 0.5;

	public TechPipe(double threshold)
	{
		this.threshold = threshold;
	}

	public TechPipe(StringDistance dm) 
	{
		this.distMetric = dm;
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)((Node)pair.getObject1()).getObject(); 
		Citation s2 = (Citation)((Node)pair.getObject2()).getObject();

		String ss1 = s1.getField(Citation.tech);
		String ss2 = s2.getField(Citation.tech);

		if(!ss1.equals("") && !ss2.equals("")){
//			System.out.println(ss1);	
//			System.out.println(ss2);			

			double dist = distMetric.score(ss1, ss2);	

			pair.setFeatureValue("TechSimilarity", dist);
		}
		

		return carrier;
	}


}
