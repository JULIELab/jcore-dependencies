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

public class TitlePipe extends Pipe
{
	StringDistance distMetric;
	double threshold = 0.9;

	boolean useNBest = false;
	
	public TitlePipe(double threshold)
	{
		this.threshold = threshold;
		this.distMetric = new NeedlemanWunsch();
	}

	public TitlePipe(StringDistance distMetric, boolean useNBest) {
		this.distMetric = distMetric;
	}

	public TitlePipe(StringDistance distMetric) {
		this.distMetric = distMetric;
	}

	public TitlePipe(StringDistance distMetric, double threshold, boolean useNBest) 
	{
		this.threshold = threshold;
		this.distMetric = distMetric;
	}

	private double computeValue (Citation c1, Citation c2) {
		String ss1 = c1.getField("title");
		String ss2 = c2.getField("title");
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
		pair.setFeatureValue("TitleSimilarity", v);
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation c1 = (Citation)pair.getObject1();
		Citation c2 = (Citation)pair.getObject2();
		String ss1 = c1.getField(Citation.title);
		String ss2 = c2.getField(Citation.title);
	
		if(!ss1.equals("") && !ss2.equals("")){

			double dist = distMetric.score(ss1, ss2);
			pair.setFeatureValue("TitleSimilarity", dist);
			

/*			
			if(dist >= threshold){
				//System.out.println("Distance for pair: " + ss1 + " :: " + ss2);
				//System.out.println("  --> " + dist);
				//String explain = distMetric.explainScore(ss1,ss2);
				//System.out.println(" Explanation: " + explain);
				
				pair.setFeatureValue("TitleSimilar", 1.0);
			}
*/
			//		System.out.println(dist);
		}

		return carrier;
	}


}
