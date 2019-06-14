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
import java.util.Iterator;

/** Feature is true if there exist at least N nodes in Cluster that
 * have a negative edge weight with Node*/
public class NNegativeNodes extends Pipe
{
	/** Determines distance between two nodes*/
	Classifier classifier;
	/** number of negative nodes we must see for this feature to fire */
	int n;
	
	public NNegativeNodes (Classifier _classifier, int _n)	{
		this.classifier = _classifier;
		this.n = _n;
	}

	public NNegativeNodes (Classifier _classifier) {
		this (_classifier, 1);
	}
	
	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Citation node = (Citation)pair.getNode();
		Collection cluster = (Collection)pair.getCluster();
		Iterator iter = cluster.iterator ();
		int numNeg = 0;
		while (iter.hasNext()) {
			Citation c = (Citation) iter.next();
			NodePair np = new NodePair (c, node);
			Instance inst = new Instance (np, "unknown", null, np, classifier.getInstancePipe());
			Labeling labeling = classifier.classify (inst).getLabeling();
			if (labeling.labelAtLocation(0).toString().equals("no")) {
				if (++numNeg >= n)
					break;
			}
		}
		if (numNeg >= n)
			pair.setFeatureValue ("ClusterContainsAtLeast"+n+"NegativeNodes", 1.0);
 		return carrier;
	}
}
