/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.NodeClusterPair;

import java.util.Collection;

/** Feature is size of cluster...to penalize large clusters.*/
public class ClusterSize extends Pipe
{
	public ClusterSize ()	
	{	}

	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Collection cluster = (Collection)pair.getCluster();
		pair.setFeatureValue ("LogOfClusterSize=", Math.log(cluster.size()));				
		return carrier;
	}
}
