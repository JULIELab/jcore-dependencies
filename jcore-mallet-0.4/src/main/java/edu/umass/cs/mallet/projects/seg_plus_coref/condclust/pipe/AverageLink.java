/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import java.util.*;

/** Feature is similarity between node and closest node in cluster, as
 * determined by the classifier*/
public class AverageLink extends Pipe
{
	Classifier classifier;
	
	public AverageLink (Classifier _classifier)	
	{
		this.classifier = _classifier;
	}

	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Citation node = (Citation)pair.getNode();
		Collection cluster = (Collection)pair.getCluster();
		Iterator iter = cluster.iterator ();
		double total = 0.0;		
		while (iter.hasNext()) {
			Citation c = (Citation) iter.next();
			NodePair np = new NodePair (c, node);
			Instance inst = new Instance (np, "unknown", null, np, classifier.getInstancePipe());
			Classification classification = classifier.classify (inst);
			Labeling labeling = classification.getLabeling();
			double val = 0.0;
			if (labeling.labelAtLocation(0).toString().equals("no")) 
				val =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
			else 
				val =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
			total += val;
		}
 		double average = total / (double)cluster.size();
		if (average > 0.9)
			pair.setFeatureValue ("AverageNodeSimilarityHigh", 1.0);
		else if (average > 0.75)
			pair.setFeatureValue ("AverageNodeSimilarityMed", 1.0);
		else if (average > 0.5)
			pair.setFeatureValue ("AverageNodeSimilarityWeak", 1.0);
		else if (average > 0.3)
			pair.setFeatureValue ("AverageNodeSimilarityMin", 1.0);
		else
			pair.setFeatureValue ("AverageNodeSimilarityNone", 1.0);
		//pair.setFeatureValue ("AverageNodeSimilarity", average);				
 		return carrier;
	}
}
