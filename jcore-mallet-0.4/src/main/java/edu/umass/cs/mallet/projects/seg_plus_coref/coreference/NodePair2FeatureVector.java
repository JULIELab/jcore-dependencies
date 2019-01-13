/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


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


public class NodePair2FeatureVector extends Pipe
{
	boolean binary, augmentable;
	
	public NodePair2FeatureVector (boolean binary, boolean augmentable) {
		super( Alphabet.class, null );
		this.binary = binary;
		this.augmentable = augmentable;
	}
	
	public NodePair2FeatureVector ()
	{
		this (true, false);
	}


	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		//Citation s1 = (Citation)pair.getObject1();
		//Citation s2 = (Citation)pair.getObject2();
		
		carrier.setSource(pair);
		if (!augmentable)
			carrier.setData( new FeatureVector( (Alphabet)getDataAlphabet(),
																					pair.getFeatures(), binary ) );
		else
			carrier.setData( new AugmentableFeatureVector( (Alphabet)getDataAlphabet(),
																										 pair.getFeatures(), binary) );
		return carrier;
	}

}
