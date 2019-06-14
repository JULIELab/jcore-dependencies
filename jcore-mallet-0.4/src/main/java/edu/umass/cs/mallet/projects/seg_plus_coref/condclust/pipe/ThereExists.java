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
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.Citation;

import java.util.Collection;
import java.util.Iterator;

/** Sets a feature for each element of "fields" that is true if it is
 * an exact string match for Node and for some Node in the Cluster */
public class ThereExists extends Pipe
{
	String[] fields;
	
	public ThereExists (String[] _fields)	
	{
		this.fields = _fields;
	}

	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Citation node = (Citation)pair.getNode();
		Collection cluster = (Collection)pair.getCluster();
		for (int i=0; i < fields.length; i++) {
			String nodeValue = node.getField(fields[i]);
			if (matchesAtLeastNInCluster (fields[i], nodeValue, cluster, 1)) 
				pair.setFeatureValue ("Same_"+fields[i]+"_thereExists", 1.0);				
		}
		return carrier;
	}

	/** Returns true if field=value for all nodes in "cluster" */
	private boolean matchesAtLeastNInCluster (String field, String value, Collection cluster, int N) {
		Iterator iter = cluster.iterator();
		int numMatches = 0;
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof Citation))
				System.err.println ("Type of object is " + o.getClass().getName());
			Citation c = (Citation) o;
			if (c.getField(field).equals(value)) 
				numMatches++;
		}
		return (numMatches >= N);
	}
}
