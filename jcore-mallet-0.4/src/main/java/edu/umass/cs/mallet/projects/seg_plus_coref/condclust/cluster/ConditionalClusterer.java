/* Copyright (C) 2002 Dept. of Computer Science, Univ. of Massachusetts, Amherst

   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet

   This program toolkit free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  For more
   details see the GNU General Public License and the file README-LEGAL.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
   02111-1307, USA. */


/**
	 @author Aron Culotta
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.cluster;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.*;
import edu.umass.cs.mallet.base.cluster.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import java.util.*;

/** Trains the conditional clusterer to predict "yes" or "no" for a
 * NodeClusterPair; i.e. does this nodes belong in this cluster?*/
public class ConditionalClusterer {

	Classifier classifier;
	Pipe pipe;
	double threshold;
	/**  cache previous similarity calculations to save time */
	LRUCache simCache;  
	/** measure cache performance */
	int cacheAccesses;
	int cacheHits;

	/** cache clusters already evaluated */
	HashMap evalCache;
	
	private static double MIN = Double.NEGATIVE_INFINITY;
	private static double MAX = Double.POSITIVE_INFINITY;
	
	public ConditionalClusterer (Pipe _pipe, Classifier _classifier, double _threshold) {
		this.pipe = _pipe;
		this.classifier = _classifier;
		this.threshold = _threshold;
		this.simCache = new LRUCache(10000, 100);
		this.cacheAccesses = 0;
		this.cacheHits = 0;
		this.evalCache = new HashMap ();
	}

	public ConditionalClusterer (Pipe _pipe, Classifier _classifier) {
		this (_pipe, _classifier, 0.0);
	}


	public Classifier getClassifier () {return this.classifier;}

	
	/** Cluster papers and venues jointly. */
	public Collection clusterPapersAndVenues (ArrayList _papers, ArrayList _venues,
																						Collection paperTrueClustering, Collection venueTrueClustering,
																						Classifier paperClusterClassifier, Classifier venueClusterClassifier,
																						Random r) {
		ArrayList papers = (ArrayList)_papers.clone();
		ArrayList venues = (ArrayList)_venues.clone();
		Collection paperClustering = new ArrayList ();
		Collection venueClustering = new ArrayList ();
		HashMap paper2Venue = getPaper2VenueHash (papers, venues);
		while (papers.size() > 0) {
			int index = r.nextInt (papers.size());
			PaperCitation paper =  (PaperCitation) papers.get(index);
			VenueCitation venue = (VenueCitation) paper2Venue.get (paper);
			if (venue == null) {
				placePaperInBestClusterWithoutVenue (paper, paperClustering, venueClustering, paperClusterClassifier,
																						 paper2Venue);
				papers.remove (index);
				continue;
			}
			Collection closestPaperCluster = null;
			Collection closestVenueCluster = null;
			double maxVal = this.MIN;
			Iterator paperClusterIterator = paperClustering.iterator();
			while (paperClusterIterator.hasNext()) {
				Collection paperCluster = (Collection) paperClusterIterator.next();
				Collection venueCluster = getVenueClusterForPaperCluster (paperCluster, venueClustering, paper2Venue);
				if (venueCluster == null) // this paper cluster has no venue
					continue;
				VenuePaperCluster vpc = new VenuePaperCluster (paper, venue, paperCluster, venueCluster);
				Instance inst = new Instance (vpc, "unknown", vpc, classifier.getInstancePipe());
				Labeling labeling = classifier.classify (inst).getLabeling();
				double val = (labeling.labelAtLocation(0).equals("yes")) ?
										 (labeling.valueAtLocation(0) - labeling.valueAtLocation(1)) :
										 (labeling.valueAtLocation(1) - labeling.valueAtLocation(0));
				if (val > maxVal) {
					closestPaperCluster = paperCluster;
					closestVenueCluster = venueCluster;
					maxVal = val;
				}
			}
			// if classifier says "no" to all cluster pairs, add the paper
			// to a new cluster, and the venue to the closest cluster (or
			// its own cluster)
			if (closestPaperCluster == null || maxVal < threshold) {
				createSoloCluster (paper, paperClustering);
				placeNodeInClosestCluster (venue, venueClustering, venueClusterClassifier);
				papers.remove (index);
			}
			else {
				closestPaperCluster.add (paper);
				closestVenueCluster.add (venue);
				papers.remove (index);
			}
		}
		paperClustering.addAll (venueClustering);
		return paperClustering;
	}

	private void placePaperInBestClusterWithoutVenue (PaperCitation paper, Collection paperClustering,
																										Collection venueClustering,
																										Classifier paperClassifier, HashMap paper2Venue) {
		Iterator iter = paperClustering.iterator();
		Collection closestCluster = null;
		double closestValue = -9999999.9;		
		while (iter.hasNext()) {
			Collection cluster = (Collection)iter.next();
			if (getVenueClusterForPaperCluster (cluster, venueClustering, paper2Venue) == null)
				continue;
			double val = getSimilarityToCluster (paper, cluster, paperClassifier);
			if (val > closestValue) {
				closestValue = val;
				closestCluster = cluster;
			}
		}
		if (closestCluster != null && closestValue > threshold) { // add to existing cluster
			System.err.println ("Adding node to preexisting cluster with value " + closestValue);
			closestCluster.add (paper);
		}
		else { // create separate cluster
			Collection newC = new LinkedHashSet ();
			newC.add (paper);
			paperClustering.add (newC);
		}
	}

	
	private void createSoloCluster (Object node, Collection clustering) {
		Collection cl = new LinkedHashSet ();
		cl.add (node);
		clustering.add (cl);
	}
	
	private Collection getVenueClusterForPaperCluster (Collection paperCluster, Collection venueClustering,
																										 HashMap paper2Venue) {
		Iterator iter = paperCluster.iterator();
		PaperCitation paper = (PaperCitation) iter.next();
		VenueCitation venue = (VenueCitation) paper2Venue.get (paper);
		if (venue == null)
			return null;
		Collection venueCluster = findClusterForNode (venue, venueClustering);
		if (venueCluster == null)
			throw new IllegalArgumentException ("Expected to find cluster for venue, but didn't: " + venue);
		return venueCluster;
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
	

	
	/** Greedily cluster by adding node to Clustering that is (a)
	 * closest to an existing cluster, or (b) farthest from all existing
	 * clusters (i.e. closest to being a new cluster) */
	public Collection cluster (ArrayList _nodes, Collection trueClustering) {
		ArrayList nodes = (ArrayList)_nodes.clone();
		Collection clustering = new ArrayList ();
		this.evalCache = new HashMap ();
		this.simCache = new LRUCache (10000, 100);
		// initialize with one singleton cluster, randomly chosen for now,
		// but could imagine picking "easiest" node (most similar, most
		// dissimilar to others?)
		Random r =  new Random(1);
		int index = r.nextInt (nodes.size());
		Collection cl = new LinkedHashSet ();
		cl.add (nodes.get(index));
		clustering.add (cl);
		nodes.remove (index);
		while (nodes.size() > 0) {
			Object closestNode = null;
			Collection closestCluster = null;
			// the maximum confidence that "closestNode" should join
			// "closestCluster"
			double maxAddToClusterConfidence = this.MIN;
			Object farthestNode = null;
			// the minimum confidence that "farthestNode" should be its own
			// cluster
			double minMakeNewClusterConfidence = this.MAX;
			
			for (int i=0; i < nodes.size(); i++) {
				double addToClusterConfidence = this.MIN;
				Collection clusterToAppend = null;
				double makeNewClusterConfidence = this.MAX;
				Iterator iter = clustering.iterator ();
				while (iter.hasNext()) {
					Collection cluster = (Collection) iter.next();
					double sim = getSimilarityToCluster (nodes.get(i), cluster, this.classifier);
					double posConfidence = sim - threshold;
					double negConfidence = -posConfidence;
					if (posConfidence >= 0 && posConfidence > addToClusterConfidence) {
						addToClusterConfidence = posConfidence;
						clusterToAppend = cluster;
						//System.err.println ("AddToClusterConfidence for node " + i + " is " + addToClusterConfidence);
					}
					if (negConfidence > 0 && negConfidence < makeNewClusterConfidence) {
						makeNewClusterConfidence = negConfidence;
					}
				}
				if (addToClusterConfidence > maxAddToClusterConfidence) {
					maxAddToClusterConfidence = addToClusterConfidence;
					closestNode = nodes.get(i);
					closestCluster = clusterToAppend;
				}
				if (makeNewClusterConfidence != this.MAX && makeNewClusterConfidence < minMakeNewClusterConfidence) {
					minMakeNewClusterConfidence = makeNewClusterConfidence;
					farthestNode = nodes.get(i);
				}				
			}
			
			// decide whether to add the closestNode to closestCluster, or
			// fartestNode to a new cluster
			if (closestNode == null && minMakeNewClusterConfidence == this.MAX)
				throw new IllegalStateException ("We found neither a cluster to merge with nor evidence that this should be a separate cluster. Something is wrong.");
			if (closestNode == null || minMakeNewClusterConfidence > maxAddToClusterConfidence) {
				// make new cluster
				Collection newC = new LinkedHashSet ();
				newC.add (farthestNode);
				System.err.println ("Adding new cluster with confidence " + minMakeNewClusterConfidence);
				clustering.add (newC);
				if (!nodes.remove (farthestNode))
					throw new IllegalArgumentException ("FarthestNode not in nodes list");
			}
			else {
				if (closestNode == null)
					throw new IllegalArgumentException ("ClosestNode is null!");
				closestCluster.add (closestNode);
				if (!nodes.remove (closestNode))
					throw new IllegalArgumentException ("ClosestNode not in nodes list");
				System.err.println ("Adding node to cluster with confidence " + maxAddToClusterConfidence);
			}
			System.err.println ("Predicting " + clustering.size() + " clusters, " +
													nodes.size() + " nodes remaining.");
			double cacheHitRatio = (double)cacheHits / cacheAccesses;
			System.err.println ("Cache Hit Ratio: " + cacheHitRatio);
			if (trueClustering != null) {
				evaluateClustering (clustering, trueClustering);
			}
		}
		return clustering;
	}
	
	/** Returns the similarity of "node" to "cluster," as given by the
	 * posterior of "classifier."*/
	private double getSimilarityToCluster (Object node, Collection cluster, Classifier theclassifier) {
	  this.cacheAccesses++;
		String key = String.valueOf (node.hashCode()) + "__" + String.valueOf (cluster.hashCode());
		Double value = this.simCache == null ? null : (Double) this.simCache.get (key);
		if (value == null) {
			NodeClusterPair pair = new NodeClusterPair (node, cluster);
			Instance inst = new Instance (pair, "unknown", null, pair, theclassifier.getInstancePipe());
			Classification classification = theclassifier.classify (inst);
			Labeling labeling = classification.getLabeling ();
			double val = 0.0;
			if (labeling.labelAtLocation(0).toString().equals("yes")) {
				if (hasNegativeEdge ((NodeClusterPair)inst.getSource())) {
					val = this.MIN;
				}
				else 
					val = labeling.valueAtLocation(0) - labeling.valueAtLocation(1);
			}
			else
				val = labeling.valueAtLocation(1) - labeling.valueAtLocation(0);
			value = new Double (val);
			if (this.simCache != null)
				this.simCache.put (key, value);
		}
		else {
			cacheHits++;
			//System.err.println ("Cache size: " + simCache.size() +
			//										" Cache hit rate: " + ((double)cacheHits / cacheAccesses));
		}
		
		return value.doubleValue();
	}

	private boolean hasNegativeEdge (NodeClusterPair p) {
		double val = p.getFeatureValue ("ClusterContainsAtLeast1NegativeNodes");
		if (val > 0)
			System.err.println ("HAS NEGATIVE EDGE");
		return (val > 0);
	}
	
	/** Pick a random ordering to cluster nodes, using the learned
	 * classifier to make yes/no decisions. */
	public Collection clusterRandom (ArrayList _nodes, Collection trueClustering, Random r) {
		this.evalCache = new HashMap ();
		ArrayList nodes = (ArrayList)_nodes.clone();
		Collection clustering = new ArrayList ();
		// don't use cache since we never repeat comparisons
		simCache = null;
		while (nodes.size() > 0) {
			int index = r.nextInt (nodes.size());
			clustering = placeNodeInClosestCluster (nodes.get(index), clustering, this.classifier);
			nodes.remove (index);
			System.err.println ("Predicting " + clustering.size() + " clusters and "
													+ nodes.size() + " nodes remaining.");
			if (trueClustering != null) {
				evaluateClustering (clustering, trueClustering);
			}			
		}
		return clustering;
	}

	/** Place "node" in the closest cluster in "clustering" that is
	 * above "threshold." If posterior is below "threshold," place in
	 * new cluster. */
	private Collection placeNodeInClosestCluster (Object node, Collection clustering, Classifier theclassifier) {
		Iterator iter = clustering.iterator();
		Collection closestCluster = null;
		double closestValue = -9999999.9;		
		while (iter.hasNext()) {
			Collection cluster = (Collection)iter.next();
			double val = getSimilarityToCluster (node, cluster, theclassifier);
			if (val > closestValue) {
				closestValue = val;
				closestCluster = cluster;
			}
		}
		if (closestCluster != null && closestValue > threshold) { // add to existing cluster
			System.err.println ("Adding node to preexisting cluster with value " + closestValue);
			closestCluster.add (node);
		}
		else { // create separate cluster
			Collection newC = new LinkedHashSet ();
			newC.add (node);
			clustering.add (newC);
		}
		return clustering;
	}

	private void evaluateClustering (Collection predicted, Collection truth) {
		// this is no good
		//CitationUtils.evaluateClustering (predicted, truth, "INTERMEDIATE RESULTS");
		Iterator clusterIter = predicted.iterator();
		int totalTP = 0;
		int totalFP = 0;
		int ci = 0;
		while (clusterIter.hasNext()) {
			Collection c = (Collection) clusterIter.next();
			Double cachedAccuracy = (Double) evalCache.get (c);
			if (cachedAccuracy != null) {
				System.err.println ("Cluster " + ci + " Accuracy: " + cachedAccuracy);
				ci++;
				continue;
			}
			Object[] nodes = c.toArray ();
			if (nodes.length == 1) {
				System.err.println ("Cluster " + ci + " has one node\n");
				ci++;
				continue;
			}
				
			int tp = 0;
			int fp = 0;
			for (int i=0; i < nodes.length; i++) {
				for (int j=i+1; j < nodes.length; j++) {
					if (inSameCluster (nodes[i], nodes[j], truth)) 
						tp++;
					else {
						fp++;
						System.err.println ("FP:\nN1: " + nodes[i] + "\nN2:\n" + nodes[j]);
					}
				}
			}
			totalTP += tp;
			totalFP += fp;
			double accuracy = (tp + fp == 0) ? 0 : (double)tp / (tp + fp);
			System.err.println ("Cluster " + ci + " Accuracy: " + accuracy);
			evalCache.put (c, new Double (accuracy));
			ci++;
		}
		double accuracy = (totalTP + totalFP == 0) ? 0 : (double)totalTP / (totalTP + totalFP);
		System.err.println ("OVERALL PAIR ACCURACY: " + accuracy);
	}

	private boolean inSameCluster (Object n1, Object n2, Collection c) {
		Iterator iter = c.iterator ();
		while (iter.hasNext()) {
			Collection cl = (Collection)iter.next();
			boolean n1here = cl.contains (n1);
			boolean n2here = cl.contains (n2);
			if (n1here && n2here)
				return true;
			else if (n1here || n2here)
				return false;			
		}
		return false;
	}
}
