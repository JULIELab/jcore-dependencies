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

import edu.umass.cs.mallet.base.classify.Classification;
import edu.umass.cs.mallet.base.classify.MaxEnt;
import edu.umass.cs.mallet.base.classify.MaxEntTrainer;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.Labeling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/*
	This class represents a "Tree Model" and will compute the tree model
	objective function for a set of clusters
 */

public class TreeModel {

	MaxEnt treeModel = null;
	InstanceList ilist = null;
	Pipe instancePipe = null;
	boolean multiTree = false;

	// create model and train
	public TreeModel (Pipe instancePipe, ArrayList nodes, ArrayList pubs) {
		this.instancePipe = instancePipe;
		ilist = new InstanceList (instancePipe);
		ilist.add (new PubCitIterator (nodes, pubs) );
		instancePipe.getDataAlphabet().stopGrowth();
		System.out.println(" >>>> Training Tree Model <<<< ");
		treeModel = (MaxEnt)(new MaxEntTrainer().train (ilist, null, null, null, null));
	}

	// in the case where we have 3 separate training clusters
	// this is bad form, but we're low on time
	public TreeModel (Pipe instancePipe, ArrayList nodes1, ArrayList nodes2, ArrayList nodes3,
										ArrayList pubs1, ArrayList pubs2, ArrayList pubs3) {
		this.instancePipe = instancePipe;
		ilist = new InstanceList (instancePipe);
		ilist.add (new PubCitIterator (nodes1, pubs1) );
		ilist.add (new PubCitIterator (nodes2, pubs2) );
		ilist.add (new PubCitIterator (nodes3, pubs3) );		
		instancePipe.getDataAlphabet().stopGrowth();
		treeModel = (MaxEnt)(new MaxEntTrainer().train (ilist, null, null, null, null));
	}

	public void setMultiTree (boolean mt) {
		this.multiTree = mt;
	}


	public double computeTreeObjFn (Collection collection) {
		return computeTreeObjFn (collection, false);
	}
	public double computeTreeObjFn (Collection collection, boolean verbose) {
		Iterator i1 = collection.iterator();
		ArrayList pubs = new ArrayList ();
		while (i1.hasNext()) {
	    ArrayList l1 = Collections.list(Collections.enumeration(((Collection)i1.next())));
	    Publication p = new Publication ((Citation)l1.get(0));
	    for (int i = 1; i < l1.size(); i++) {
				p.addNewCitation ((Citation)l1.get(i));
	    }
	    pubs.add(p);
		}
		if (multiTree) {
	    ArrayList allCitations = new ArrayList();
	    for (Iterator i2 = collection.iterator(); i2.hasNext();) {
				Collection c1 = (Collection)i2.next();
				for (Iterator i3 = c1.iterator(); i3.hasNext();) {
					allCitations.add(i3.next());
				}
	    }
	    return computeTreeObjFnPubs2 (pubs, allCitations);
		}
		else
	    return computeTreeObjFnPubs (pubs, verbose);
	}

	public double computeTreeObjFnPubs2 (ArrayList pubs, ArrayList allCitations) {
		double objVal = 0.0;
		for (int i=0; i < pubs.size(); i++) {
	    Publication p = (Publication)pubs.get(i);
	    Collection citations = p.getCitations();
	    Iterator it = allCitations.iterator();
	    while (it.hasNext()) {
				Citation c = (Citation)it.next();
				//System.out.println("pipe data alphabet: " + instancePipe.getDataAlphabet());
				//System.out.println("pipe target alphabet: " + instancePipe.getTargetAlphabet());
				Classification cl = treeModel.classify(new NodePair(p,c));
				Labeling labeling = cl.getLabeling();
				double toAdd = 0.0;
				if (labeling.labelAtLocation(0).toString().equals("no")) {
					if (citations.contains(c))
						toAdd =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
					else
						toAdd = labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
				} else {
					if (citations.contains(c))
						toAdd = labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
					else
						toAdd =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
				}
				objVal += toAdd;
				//System.out.println("e-val for " + p.print() + " :: " + c.print() +  " -> " + toAdd);
	    }
		}
		return objVal;
	}

	public double computeTreeObjFnPubs (ArrayList pubs, boolean verbose) {
		double objVal = 0.0;
		for (int i=0; i < pubs.size(); i++) {
	    Publication p = (Publication)pubs.get(i);
	    Iterator it = p.getCitations().iterator();
	    if (verbose) {
				System.out.println("\n\n PUBLICATION: ");
				System.out.println("  String: " + p.getString());
	    }
	    while (it.hasNext()) {
				Citation c = (Citation)it.next();
				//System.out.println("pipe data alphabet: " + instancePipe.getDataAlphabet());
				//System.out.println("pipe target alphabet: " + instancePipe.getTargetAlphabet());
				Classification cl = treeModel.classify(new NodePair(p,c));
				Labeling labeling = cl.getLabeling();
				double toAdd = 0.0;
				if (labeling.labelAtLocation(0).toString().equals("no")) {
					toAdd =  labeling.valueAtLocation(1);
				} else {
					toAdd =  labeling.valueAtLocation(0);
				}
				objVal += toAdd;
				if (verbose) {
					System.out.println("\n  CITATION: " + c.print() + " -> " + toAdd);
					System.out.println(c.getString());
				}
	    }
		}
		return objVal;
	}

}
