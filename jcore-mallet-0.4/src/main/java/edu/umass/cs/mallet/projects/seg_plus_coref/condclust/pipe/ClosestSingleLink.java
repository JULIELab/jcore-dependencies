/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe;

import edu.umass.cs.mallet.base.classify.Classification;
import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Labeling;
import edu.umass.cs.mallet.base.util.PropertyList;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.NodeClusterPair;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.Citation;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.NodePair;

import java.util.Collection;
import java.util.Iterator;

/** Feature is similarity between node and closest node in cluster, as
 * determined by the classifier*/
public class ClosestSingleLink extends Pipe
{
	/** Determines distance between two nodes*/
	Classifier classifier;
	/** True if we should include the features from the closest NodePair */
	boolean includePairwiseFeatures;
	
	public ClosestSingleLink (Classifier _classifier, boolean _includePairwiseFeatures)	{
		this.classifier = _classifier;
		this.includePairwiseFeatures = _includePairwiseFeatures;
	}

	public ClosestSingleLink (Classifier _classifier) {
		this (_classifier, true);
	}
	
	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Citation node = (Citation)pair.getNode();
		Collection cluster = (Collection)pair.getCluster();
		Iterator iter = cluster.iterator ();
		double maxVal = -99999999.9;
		NodePair closestPair = null;
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
			if (val > maxVal) {
				maxVal = val;
				closestPair = np;
			}
		}
		if (maxVal > 0.9)
			pair.setFeatureValue ("ClosestNodeSimilarityHigh", 1.0);
		else if (maxVal > 0.75)
			pair.setFeatureValue ("ClosestNodeSimilarityMed", 1.0);
		else if (maxVal > 0.5)
			pair.setFeatureValue ("ClosestNodeSimilarityWeak", 1.0);
		else if (maxVal > 0.3)
			pair.setFeatureValue ("ClosestNodeSimilarityMin", 1.0);
		else
			pair.setFeatureValue ("ClosestNodeSimilarityNone", 1.0);
		// add features from closest NodePair
		if (includePairwiseFeatures && closestPair != null) {
			PropertyList.Iterator pliter = closestPair.getFeatures().iterator();
			while (pliter.hasNext()) {
				pliter.next();
				double v = pliter.getNumericValue();
				String key = pliter.getKey().toString();
				key = "ClosestNodeFeature_" + key;
				pair.setFeatureValue (key, v);
			}
		}
 		return carrier;
	}
}
