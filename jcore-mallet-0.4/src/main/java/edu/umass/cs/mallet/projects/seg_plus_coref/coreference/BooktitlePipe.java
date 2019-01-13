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

public class BooktitlePipe extends Pipe
{
	StringDistance distMetric;
	double threshold = 0.5;

	public BooktitlePipe(double threshold)
	{
		this.threshold = threshold;
	}

	public BooktitlePipe(StringDistance dm) 
	{
		this.distMetric = dm;
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)pair.getObject1();
		Citation s2 = (Citation)pair.getObject2();

		String ss1 = s1.getField(Citation.booktitle);
		String ss2 = s2.getField(Citation.booktitle);

		if(!ss1.equals("") && !ss2.equals("")){
//			System.out.println(ss1);	
//			System.out.println(ss2);			

			double dist = distMetric.score(ss1, ss2);	

			pair.setFeatureValue("BooktitleSimilarity", dist);
		}
		

		return carrier;
	}


}
