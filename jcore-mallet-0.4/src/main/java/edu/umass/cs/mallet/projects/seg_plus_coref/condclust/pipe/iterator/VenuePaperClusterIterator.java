/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Aron Culotta
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe.iterator;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import java.util.*;
import java.io.*;

/** Iterates over PaperVenueClusters. Each instance consists of a
 * Paper, its potential Cluster, and a Venue and its potential
 * Cluster. */
public class VenuePaperClusterIterator extends AbstractPipeInputIterator
{
	/** Iterates over TypedNodeClusterPairs created during construction */
	Iterator subIterator;

	/** To deal with class imbalance, this is the ratio of positive to
	 * negative instances desired. */	
 	double positiveInstanceRatio;
	/** Counter of how many negative instances we've seen */
	int negativeInstancesSeen;
	/** Counter of how many positive instances we've seen */
	int positiveInstancesSeen;

	HashMap paper2Venue;
	Random r;
	
	/** Randomly choose nodes and build clusters. Each time we must
	 * decide whether a node belongs in an existing cluster, we make a
	 * nodeClusterPair instance.
	 * @param clusters true clustering
	 * @param r for randomly selecting nodes to cluster
	 * @param negativeInstanceFrequency include negative instances with frequency 1/negativeInstanceFrequency
	 * @param generateSampledInstances sample positive instances from true clusters by sampling
	 * @param sampleSize number of training instances
	 */
	public VenuePaperClusterIterator (Collection paperClusters, Collection venueClusters,
																			 Random r, double positiveInstanceRatio) {
		ArrayList paperNodes = getNodesFromClusters (paperClusters);
		ArrayList venueNodes = getNodesFromClusters (venueClusters);
		this.paper2Venue = getPaper2VenueHash (paperNodes, venueNodes);
		this.r = r;
		this.negativeInstancesSeen = 0;
		this.positiveInstancesSeen = 0;
		this.positiveInstanceRatio = positiveInstanceRatio;
		this.subIterator = generateInstances (paperNodes, venueNodes, paperClusters,
																					venueClusters, r).iterator();
	}

	private Collection findClusterForNode (Object n, Collection clustering) {
		Iterator iter = clustering.iterator ();
		while (iter.hasNext()) {
			Collection c = (Collection) iter.next();
			if (c.contains (n))
				return c;
		}
		return null;
	}

	private HashMap getPaper2VenueHash (ArrayList papers, ArrayList venues) {
		HashMap hash = new HashMap ();
		for (int i=0; i < papers.size(); i++) {
			PaperCitation paper = (PaperCitation) papers.get (i);
			Object venue = findVenueForPaper (paper, venues);
			if (venue != null)
				hash.put (paper, venue);
		}
		return hash;
	}

	private Object findVenueForPaper (PaperCitation paper, ArrayList venues) {
		String venueID = paper.getField (Citation.venueID);
		if (venueID == "")
			return null;
		for (int i=0; i < venues.size(); i++) {
			VenueCitation venue = (VenueCitation) venues.get(i);
			String currVenueID = venue.getField (Citation.venueID);
			if (venueID.equals (currVenueID))
				return venue;
		}
		throw new IllegalArgumentException ("Can't find venue for paper " + paper);
	}
	
	/** Generate training instances by one random "Chinese-Restaurant
	 * Process" style clustering.
	 * @param nodes all nodes
 	 * @param clusters true clustering of nodes
	 * @param r random num generator
	 * @return NodeClusterPair list
	 */
	private ArrayList generateInstances (ArrayList paperNodes, ArrayList venueNodes,
																			 Collection paperClusters, Collection venueClusters, Random r) {
		ArrayList nodeClusterPairs = new ArrayList();
		Collection currentPaperClustering = new ArrayList();
		Collection currentVenueClustering = new ArrayList();
		while (paperNodes.size() > 0) {
			int index = r.nextInt (paperNodes.size());
			PaperCitation paper =  (PaperCitation) paperNodes.get(index);
			VenueCitation venue = getVenueOfPaper (paper);
			if (venue == null) {
				System.err.println ("Paper has no venue");
				paperNodes.remove (index);
				continue;
			}
			nodeClusterPairs.addAll (getNodeClusterPairs (paper, venue,
																										currentPaperClustering, currentVenueClustering, 
																										paperClusters, venueClusters));
			paperNodes.remove (index);
			System.err.println ("Now have " + currentPaperClustering.size() + " paper clusters and " +
													currentVenueClustering.size() + " venue clusters and " + 
													+ paperNodes.size() + " nodes remaining.");
		}
		return nodeClusterPairs;
	}
	
	/** Returns an ArrayList of VenuePaperClusters, one for each
	 * comparison made in deciding in which cluster "node" belongs, if
	 * any. Note that the nodes is then placed in the true cluster it
	 * belongs in, or a new cluster is created and added to
	 * "clustering." Note the only instances generated here are ones
	 * where the clusters for paper and venue already exist, or don't. 
	 * @param paper node being placed in a cluster
	 * @param clustering clusters to choose from to place node
	 * @param trueClustering true clustering
	 * @return list of NodeClusterPair's
	 */
	private ArrayList getNodeClusterPairs (PaperCitation paper, VenueCitation venue,
																				 Collection paperClustering, Collection venueClustering,
																				 Collection truePaperClustering, Collection trueVenueClustering) {
		if (venue == null) // let solo paperClassifier deal with these
			return null;
		ArrayList ret = new ArrayList ();
		Iterator paperIter = paperClustering.iterator();
		while (paperIter.hasNext()) {
			Collection paperCluster = (Collection) paperIter.next();
			Collection venueCluster = getVenueClusterForPaperCluster (paperCluster, venueClustering);
			boolean paperBelongs = nodeBelongsInCluster (paper, paperCluster, truePaperClustering);
			boolean venueBelongs = nodeBelongsInCluster (venue, venueCluster, trueVenueClustering);
			Collection clusterVenueBelongsIn = null;
			Collection clusterPaperBelongsIn = null;
			if (paperBelongs && !venueBelongs) {
				// this is from inconsistent labeling between papers and venues
				System.err.println ("Paper belongs but venue doesn't: " + paper);
				continue;
			}			
			if (!paperBelongs || !venueBelongs) { // adds as negative even if venue belongs
				System.err.println ("PAPER OR VENUE DO NOT BELONG");
				if (!canAddNegative())
					continue;
				VenuePaperCluster inst = new VenuePaperCluster (paper, venue, paperCluster, venueCluster, false);	 
				this.negativeInstancesSeen++;
				ret.add (inst);
			}
			else if (paperBelongs && venueBelongs) {
				System.err.println ("PAPER AND VENUE BELONG");
				VenuePaperCluster inst = new VenuePaperCluster (paper, venue, paperCluster, venueCluster, true);	 
				this.positiveInstancesSeen++;
				clusterVenueBelongsIn = venueCluster;
				clusterPaperBelongsIn = paperCluster;															 
				ret.add (inst);
			}
			// add to appropriate clusters, or make new if necessary
			if (clusterVenueBelongsIn == null) {
				clusterVenueBelongsIn = new LinkedHashSet();
				clusterVenueBelongsIn.add (venue);
				venueClustering.add (clusterVenueBelongsIn);
			}
			else 
				clusterVenueBelongsIn.add (venue);
			if (clusterPaperBelongsIn == null) {
				clusterPaperBelongsIn = new LinkedHashSet();
				clusterPaperBelongsIn.add (paper);
				paperClustering.add (clusterPaperBelongsIn);
			}
			else 
				clusterPaperBelongsIn.add (paper);
		}
		return ret;
	}		
	
	private Collection getVenueClusterForPaperCluster (Collection paperCluster, Collection venueClustering) {
		Iterator iter = paperCluster.iterator();
		PaperCitation paper = (PaperCitation) iter.next();
		VenueCitation venue = getVenueOfPaper (paper);
		if (venue == null)
			return null;
		Collection venueCluster = findClusterForNode (venue, venueClustering);
		if (venueCluster == null)
			throw new IllegalArgumentException ("Expected to find cluster for venue, but didn't: " + venue);
		return venueCluster;
	}
	
	private VenueCitation getVenueOfPaper (Object node) {
		if (!(node instanceof PaperCitation))
			throw new IllegalArgumentException ("Node is a " + node.getClass().getName() + ", not a PaperCitation");
		return (VenueCitation)paper2Venue.get (node);
	}
	
	private boolean canAddNegative () {
		
		double ratio = (negativeInstancesSeen==0) ? 1.0 :
									 (double)positiveInstancesSeen / (double)negativeInstancesSeen;
		return (ratio >= this.positiveInstanceRatio);
	}

	/** Returns true if "node" belongs in "cluster"
	 * @param node node being considered
	 * @param cluster cluster we're considering placeing "node" in
	 * @param trueClustering true clustering, to decide if node belongs in cluster
	 * @return true if "node" belongs in "cluster"
	 */
	private boolean nodeBelongsInCluster (Object node, Collection cluster, Collection trueClustering) {
		if (node == null)
			return false;
		Iterator iter = cluster.iterator();
		if (iter.hasNext()) {
			Object nodeFromCluster = iter.next();
			return nodesOccurInSameCluster (node, nodeFromCluster, trueClustering);
		}
		else
			throw new IllegalArgumentException ("Empty cluster");
	}

	/** True if "n1" and "n2" occur in a cluster in "clustering" */
	private boolean nodesOccurInSameCluster (Object n1, Object n2, Collection clustering) {
		Iterator iter = clustering.iterator();
		while (iter.hasNext()) {
			Collection cluster = (Collection) iter.next();
			if (cluster.contains (n1) && cluster.contains (n2))
				return true;
		}
		return false;
	}
	
	/** Flatten clustering to a list of nodes. */
	private ArrayList getNodesFromClusters (Collection clusters) {
		ArrayList ret = new ArrayList ();
		Iterator clusterIter = clusters.iterator();
		while (clusterIter.hasNext()) {
			Collection cluster = (Collection) clusterIter.next();
			ret.addAll (cluster);
		}
		return ret;
	}
	
	public boolean hasNext () {
		return subIterator.hasNext();
	}
	
	public Instance nextInstance () {
		if (subIterator.hasNext()) {
			NodeClusterPair p = (NodeClusterPair)subIterator.next();
			return new Instance (p, p.getLabel() ? "yes" : "no", null, null);
		}
		else
			return null;
	}
	
	public Object next () {
			return (Object)nextInstance();
	}
	
	public void remove () { throw new UnsupportedOperationException(); }	
}
