/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class StringDistances extends Pipe
{
	AbstractStringDistance nw;

	public StringDistances (Alphabet dataDict)
	{
		super(dataDict, null);
	}
	
	public StringDistances ()
	{
		super(Alphabet.class, null);
	}

	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		String s1 = (String)((Node)pair.getObject1()).getString(); // assume nodes are strings
		String s2 = (String)((Node)pair.getObject2()).getString();
//		NeedlemanWunsch nw = new NeedlemanWunsch(); // for edit distance

//		nw = new NeedlemanWunsch();
//		nw = new CharJaccard();
		nw = new Jaccard();
//		double dist = nw.score(new StringWrapper(s1), new StringWrapper(s2));
		double dist = nw.score(s1, s2);

//		System.out.println("Setting distance on " + s1 + " and " + s2 + " to " + dist);
		pair.setFeatureValue ("editDistance", dist);
		return carrier;
	}
	
}
