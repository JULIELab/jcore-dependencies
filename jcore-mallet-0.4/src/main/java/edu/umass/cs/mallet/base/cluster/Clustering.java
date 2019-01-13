/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

/** A clustering of a set of points (instances).
    @author Jerod Weinman <A HREF="mailto:weinman@cs.umass.edu">weinman@cs.umass.edu</A>
*/

package edu.umass.cs.mallet.base.cluster;

import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.InstanceList;

import java.lang.IllegalArgumentException;

public class Clustering {

    int numLabels;
    int labels[];
    InstanceList instances;

    /** Clustering constructor.
     *
     * @param instances Instances that are clustered
     * @param numLabels Number of clusters
     * @param labels Assignment of instances to clusters; many-to-one with 
     *               range [0,numLabels).
     */
    public Clustering( InstanceList instances, int numLabels, int[] labels )
    {

	if ( instances.size() != labels.length )
	    throw new IllegalArgumentException("Instance list length does not match cluster labeling");

	if (numLabels<1)
	    throw new IllegalArgumentException("Number of labels must be strictly positive.");
	
	for (int i=0 ; i<labels.length ; i++)
	    if (labels[i]<0 || labels[i]>=numLabels)
		throw new IllegalArgumentException("Label mapping must have range [0,numLabels).");

	this.instances = instances;
	this.numLabels = numLabels;
	this.labels = labels;

    }

    public int getNumClusters() { return numLabels; }

    /** Get the cluster label for a particular instance. */
    public int getLabel(int index) { return labels[index]; }

    /** Return an list of instances with a particular label. */
    public InstanceList getCluster(int label) {

	InstanceList cluster = new InstanceList(instances.getPipe() );

	for (int n=0 ; n<instances.size() ; n++ ) {

	    if ( labels[n] == label)
		cluster.add( instances.getInstance(n) );

	}

	return cluster;
    }

    /** Returns an array of instance lists corresponding to clusters. */
    public InstanceList[] getClusters() {
	
	InstanceList[] clusters = new InstanceList[numLabels];

	for (int c= 0 ; c<numLabels ; c++ )
	    clusters[c] = getCluster(c);
	
	return clusters;

    }
}
