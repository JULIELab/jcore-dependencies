/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe;

import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Labeling;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.NodeClusterPair;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.Citation;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.NodePair;

import java.util.Collection;

/** Feature is average within-class similarity. Also add feature that
 * is Node's similarity to the Prototype of this cluster, where the
 * Prototype is defined as the node that has the highest similarity to
 * all other nodes in the cluster. */
public class ClusterHomogeneity extends Pipe
{
	Classifier classifier;
	
	public ClusterHomogeneity (Classifier _classifier)	{
		this.classifier = _classifier;
	}

	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Collection cluster = (Collection)pair.getCluster();
		Citation[] nodes = (Citation[])cluster.toArray (new Citation[] {});
		double[][] similarity = new double[nodes.length][nodes.length];
		if (cluster.size() > 1) { // don't include feature for singleton clusters
			for (int i=0; i < nodes.length; i++) {
				Citation ci = nodes[i];
				similarity[i][i] = 1.0;
				for (int j=i+1; j < nodes.length; j++) {
					Citation cj = nodes[j];
					similarity[i][j] = similarity[j][i] = getSimilarity (ci, cj);
				}
			}
			double avgSimilarity = getAverage (similarity, nodes.length);
			Citation prototype = getCitationClosestToAll (nodes, similarity);
			Citation node = (Citation)pair.getNode();
			double prototypeSimilarity = getSimilarity (prototype, node);
			if (prototypeSimilarity > 0.9)
				pair.setFeatureValue ("CH_PrototypeNodeSimilarityHigh", 1.0);
			else if (prototypeSimilarity > 0.75)
				pair.setFeatureValue ("CH_PrototypeNodeSimilarityMed", 1.0);
			else if (prototypeSimilarity > 0.5)
				pair.setFeatureValue ("CH_PrototypeNodeSimilarityWeak", 1.0);
			else if (prototypeSimilarity > 0.3)
				pair.setFeatureValue ("CH_PrototypeNodeSimilarityMin", 1.0);
			else
				pair.setFeatureValue ("CH_PrototypeNodeSimilarityNone", 1.0);

			if (avgSimilarity > 0.9)
				pair.setFeatureValue ("WithinClusterSimilarityHigh", 1.0);
			else if (avgSimilarity > 0.75)
				pair.setFeatureValue ("WithinClusterSimilarityMed", 1.0);
			else if (avgSimilarity > 0.5)
				pair.setFeatureValue ("WithinClusterSimilarityWeak", 1.0);
			else if (avgSimilarity > 0.3)
				pair.setFeatureValue ("WithinClusterSimilarityMin", 1.0);
			else
				pair.setFeatureValue ("WithinClusterSimilarityNone", 1.0);			
		}
		return carrier;
	}

	private double getAverage (double[][] m, int len) {
		double sum = 0.0;
		for (int i=0; i < len; i++) {
			for (int j=0; j < len; j++) {
				sum += m[i][j];
			}
		}
		return sum / ((double)len*len);
	}

	private double getSimilarity (Citation ci, Citation cj) {
		NodePair np = new NodePair (ci, cj);
		Instance inst = new Instance (np, "unknown", null, np, classifier.getInstancePipe());
		Labeling labeling = classifier.classify(inst).getLabeling();
		double val = 0.0;
		if (labeling.labelAtLocation(0).toString().equals("no")) 
			val =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
		else 
			val =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
		return val;
	}
	/** Finds the citation that has the highest similarity to all other citations*/
	private Citation getCitationClosestToAll (Citation[] nodes, double[][] similarity) {
		double maxSim = -9999999.9;
		int maxIndex = -1;
		for (int i=0; i < nodes.length; i++) {
			double sim = 0.0;
			for (int j=0; j < nodes.length; j++) {
				sim += similarity[i][j];
			}
			if (sim > maxSim) {
				maxSim = sim;
				maxIndex = i;
			}
		}
		return nodes[maxIndex];
	}
}
