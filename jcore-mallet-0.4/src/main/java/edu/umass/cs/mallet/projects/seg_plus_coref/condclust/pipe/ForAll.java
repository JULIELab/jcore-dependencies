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
import edu.umass.cs.mallet.base.pipe.*;
import java.util.*;

/** Sets a feature for each element of "fields" that is true if it is
 * an exact string match for Node and for all Nodes in the Cluster */
public class ForAll extends Pipe
{
	String[] fields;
	
	public ForAll (String[] _fields)	
	{
		this.fields = _fields;
	}

	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Citation node = (Citation)pair.getNode();
		Collection cluster = (Collection)pair.getCluster();
		for (int i=0; i < fields.length; i++) {
			String nodeValue = node.getField(fields[i]);
			if (matchesAllInCluster (fields[i], nodeValue, cluster)) 
				pair.setFeatureValue ("Same_"+fields[i]+"_forAll", 1.0);				
		}
		return carrier;
	}

	/** Returns true if field=value for all nodes in "cluster" */
	private boolean matchesAllInCluster (String field, String value, Collection cluster) {
		Iterator iter = cluster.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof Citation))
				System.err.println ("Type of object is " + o.getClass().getName());
			Citation c = (Citation) o;
			if (!c.getField(field).equals(value)) 
				return false;
		}
		return true;
	}
}
