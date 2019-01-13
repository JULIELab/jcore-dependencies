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
import com.wcohen.secondstring.StringDistance;
import com.wcohen.secondstring.NeedlemanWunsch;

/** More specific "does there exist a node such that" questions */
public class ThereExistsMatch extends Pipe
{
	StringDistance distanceMeasure;
	
	public ThereExistsMatch (StringDistance _distanceMeasure)	
	{
		this.distanceMeasure = _distanceMeasure;
		if (!(distanceMeasure instanceof NeedlemanWunsch)) {
			throw new IllegalArgumentException ("Only NeedlemanWunsch supported now");
		}
	}

	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		Citation node = (Citation)pair.getNode();
		String nodeString = node.getUnderlyingString();
		Collection cluster = (Collection)pair.getCluster();
		Iterator iter = cluster.iterator ();
		boolean exactMatch = false;
		boolean approxMatch = false;
		while (iter.hasNext()) {
			Citation member = (Citation) iter.next();
			String wholeString = member.getUnderlyingString ();
			if (wholeString.equals (nodeString)) 
				exactMatch = true;
			if (exactMatch)
				break;
			/*double dist = 1 - ((Math.abs(distanceMeasure.score(wholeString, nodeString)) / (double)(wholeString.length() + nodeString.length())));
			if (dist >= 0.8)
			approxMatch = true;
			if (exactMatch && approxMatch)
				break;
			*/
		}
		if (exactMatch)
				pair.setFeatureValue ("ThereExistsExactMatch", 1.0);				
		if (approxMatch)
				pair.setFeatureValue ("ThereExistsHighApproxMatch", 1.0);							
		return carrier;
	}
}
