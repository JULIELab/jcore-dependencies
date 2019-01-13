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


public class TUI
{

	public static void main (String[] args) {

	    CorefCluster cl = new CorefCluster ();

	    ArrayList nodes = new ArrayList();

/*
	    nodes.add (new Node("john"));
	    nodes.add (new Node("john"));
	    nodes.add (new Node("bill"));
	    nodes.add (new Node("William"));

*/
/*
	    nodes.add( new Node("Locating", "1"));
	    nodes.add( new Node("Locating", "1"));

	    nodes.add( new Node("MUSE CSP", "2"));
	    nodes.add( new Node("MUSE CSP", "2"));
			
	    nodes.add( new Node("data-flow analysis", "3"));
	    nodes.add( new Node("data-flow analysis", "3"));
	    nodes.add( new Node("Data-AEow Analysis", "3"));



	    Pipe instancePipe = new SerialPipes (new Pipe[] {
				new Target2Label (),
				new StringDistances (),
				new NodePair2FeatureVector (),
	    });
	    InstanceList ilist = new InstanceList (instancePipe);
	    ilist.add (new NodePairIterator (nodes) );
	    cl.train(ilist, nodes);
	    Collection s = cl.clusterMentions(ilist, nodes);
	    System.out.println("Final Sets: " + s);
*/	    
	}


}
