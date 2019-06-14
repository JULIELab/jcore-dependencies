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

/** Subsumes ClosestSingleLink, AverageLink, FarthestLink to save computation */
public class AllLinks extends Pipe
{
	/** Determines distance between two nodes*/
	Classifier classifier;
	/** True if we should include the features from the closest/farthest NodePair */
	boolean includePairwiseFeatures;

	/** How many negative links we need to see b/t node and cluster for
	 * feature to fire */
	int minNumNeg;
	
	public AllLinks (Classifier _classifier, boolean _includePairwiseFeatures, int n)	{
		this.classifier = _classifier;
		this.includePairwiseFeatures = _includePairwiseFeatures;
		this.minNumNeg = n;
	}

	public AllLinks (Classifier _classifier, boolean _includePairwiseFeatures)	{
		this (_classifier, _includePairwiseFeatures, 1);
	}

	public AllLinks (Classifier _classifier) {
		this (_classifier, true);
	}
	
	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Citation node = (Citation)pair.getNode();
		Collection cluster = (Collection)pair.getCluster();
		Iterator iter = cluster.iterator ();
		double maxVal = -99999999.9;
		double minVal = 99999999.9;
		double total = 0.0;
		NodePair closestPair = null;
		NodePair farthestPair = null;
		int numNeg = 0;
		while (iter.hasNext()) {
			Citation c = (Citation) iter.next();
			NodePair np = new NodePair (c, node);
			Instance inst = new Instance (np, "unknown", null, np, classifier.getInstancePipe());
			Classification classification = classifier.classify (inst);
			Labeling labeling = classification.getLabeling();
			double val = 0.0;
			if (labeling.labelAtLocation(0).toString().equals("no")) {
				val =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
				numNeg++;
			}
			else 
				val =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
			if (val > maxVal) {
				maxVal = val;
				closestPair = np;
			}
			if (val < minVal) {
				minVal = val;
				farthestPair = np;
			}
			total += val;
		}

		// add NNegativeNodes feature
		if (numNeg >= minNumNeg)
			pair.setFeatureValue ("ClusterContainsAtLeast"+minNumNeg+"NegativeNodes", 1.0);
		
		// add features from closest NodePair
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

		// add features from farthest NodePair
		if (minVal > 0.9)
			pair.setFeatureValue ("FarthestNodeSimilarityHigh", 1.0);
		else if (minVal > 0.75)
			pair.setFeatureValue ("FarthestNodeSimilarityMed", 1.0);
		else if (minVal > 0.5)
			pair.setFeatureValue ("FarthestNodeSimilarityWeak", 1.0);
		else if (minVal > 0.3)
			pair.setFeatureValue ("FarthestNodeSimilarityMin", 1.0);
		else
			pair.setFeatureValue ("FarthestNodeSimilarityNone", 1.0);

		if (includePairwiseFeatures && farthestPair != null) {
			PropertyList.Iterator pliter = farthestPair.getFeatures().iterator();
			while (pliter.hasNext()) {
				pliter.next();
				double v = pliter.getNumericValue();
				String key = pliter.getKey().toString();
				key = "FarthestNodeFeature_" + key;
				pair.setFeatureValue (key, v);
			}
		}

		// add features from average similarity
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
				
 		return carrier;
	}
}
