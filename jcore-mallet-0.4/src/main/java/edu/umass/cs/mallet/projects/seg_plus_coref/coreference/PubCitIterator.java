/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Ben Wellner
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import salvo.jesus.graph.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;


public class PubCitIterator extends AbstractPipeInputIterator
{

	List nodes;
	List pubs;
	List pairArray;
	int currentIndex;
	int pairCount;

	public PubCitIterator (List nodes, List pubs) {
		this.nodes = nodes;
		this.pairArray = new ArrayList();
		for (int i=0; i < pubs.size(); i++) {
			Publication p = (Publication)pubs.get(i);
			for (int j=0; j < nodes.size(); j++) {
				Citation n = (Citation)nodes.get(j);
				if (p.hasCitation (n)) {
					pairArray.add(new NodePair(p, n, true));
				}
				else
					pairArray.add(new NodePair(p, n, false));
			}
		}
		currentIndex = 0;
		pairCount = pairArray.size();

	}

	public boolean hasNext () {
		return (currentIndex < pairCount);
	}

	public Instance nextInstance () {
		if (currentIndex < pairCount) {
	    String label;
			NodePair np = (NodePair)pairArray.get(currentIndex);
			currentIndex++;
			if (np.getIdRel())
				label = "yes";
			else
				label = "no";
			return new Instance(np, label, null, null);
		} else {return null;}
	}
	public Object next () {
		return (Object)nextInstance();
	}
	public void remove () { throw new UnsupportedOperationException(); }

}
