/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import java.util.*;

public class NodeClusterPair2FeatureVector extends Pipe
{
	public NodeClusterPair2FeatureVector (Alphabet dataDict)
	{
		super (dataDict, null);
	}

	public NodeClusterPair2FeatureVector ()
	{
		super (Alphabet.class, null);
	}

	public Instance pipe (Instance carrier) {
		NodeClusterPair pair = (NodeClusterPair)carrier.getData();
		carrier.setSource(pair);
		AugmentableFeatureVector vec =
			new AugmentableFeatureVector((Alphabet)getDataAlphabet(), pair.getFeatures(), false);
		carrier.setData(vec);
		return carrier;
	}
}
