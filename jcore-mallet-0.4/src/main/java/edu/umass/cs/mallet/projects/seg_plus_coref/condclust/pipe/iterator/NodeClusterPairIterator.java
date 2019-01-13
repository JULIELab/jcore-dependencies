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
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import java.util.*;
import java.io.*;

/** Generates instances of NodeClusterPairs by one of two ways: (1)
 * randomly clustering the data following the Chinese-restaurant style
 * generative process, or (2) given the true clustering, generate
 * NodeClusterPairs by sampling from the true clusters.  Assumes for
 * now that all clusters are pure (i.e. always make correct clustering
 * decisions). */
public class NodeClusterPairIterator extends AbstractPipeInputIterator
{
	/** Iterates over NodeClusterPairs created during construction */
	Iterator subIterator;

	/** To deal with class imbalance, this is the ratio of positive to
	 * negative instances desired. */	
 	double positiveInstanceRatio;

	/** Counter of how many negative instances we've seen */
	int negativeInstancesSeen;
	/** Counter of how many positive instances we've seen */
	int positiveInstancesSeen;

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
	public NodeClusterPairIterator (Collection clusters, Random r,
																	double positiveInstanceRatio, boolean generateSampledInstances,
																	int sampleSize) {
		ArrayList nodes = getNodesFromClusters (clusters);
		this.r = r;
		this.negativeInstancesSeen = 0;
		this.positiveInstancesSeen = 0;
		this.positiveInstanceRatio = positiveInstanceRatio;
		if (generateSampledInstances)
			this.subIterator = generateSampledInstances (nodes, clusters, r, sampleSize).iterator();
		else
			this.subIterator = generateCRPInstances (nodes, clusters, r).iterator();
	}

	public NodeClusterPairIterator (Collection clusters, Random r,
																	double positiveInstanceRatio, boolean generateSampledInstances) {
		this (clusters, r, positiveInstanceRatio, generateSampledInstances, clusters.size()*3);
	}

	public NodeClusterPairIterator (List data) {
		subIterator = data.iterator();
	}

	/** Generate instances by sampling NodeClusterPairs from the true clustering.
	 * @param nodes all nodes
	 * @param clusters true clustering of nodes
	 * @return NodeClusterPair list
	 */
	private ArrayList generateSampledInstances (ArrayList nodes, Collection clusters, Random r, int sampleSize) {
		ArrayList nodeClusterPairs = new ArrayList();
		// to sample w/o replacement
		while (nodeClusterPairs.size() < sampleSize) {
			Object node = getRandomNode (nodes, r);
			boolean positiveSample = !canAddNegative();
			if (positiveSample) {
				Collection trueCluster = findClusterForNode (node, clusters);
				if (trueCluster == null)
					throw new IllegalArgumentException ("No true cluster for node!");
				if (trueCluster.size() < 2)
					continue;
				int sizeToSample = r.nextInt (trueCluster.size()-1) + 1;
				Collection sampledCluster = sampleSubCluster (trueCluster, node, sizeToSample);
				if (sampledCluster.size() != sizeToSample)
					throw new IllegalArgumentException ("sampledCluster.size: " + sampledCluster.size() +
																							" != sizeToSample: " + sizeToSample);
				//System.err.println ("Sampled positive cluster of size " + sampledCluster.size());
				if (!nodeBelongsInCluster (node, sampledCluster, clusters)) 
					throw new IllegalStateException ("Node does not belong in cluster, but we're generating a training instance that says it does");
					
				nodeClusterPairs.add (new NodeClusterPair (node, sampledCluster, true));								
				positiveInstancesSeen++;
			}
			else {
				Collection falseCluster = findRandomClusterWithoutNode (node, clusters);
				int sizeToSample = r.nextInt (falseCluster.size()) + 1;
				Collection sampledCluster = sampleSubCluster (falseCluster, null, sizeToSample);
				if (sampledCluster.size() != sizeToSample)
					throw new IllegalArgumentException ("sampledCluster.size: " + sampledCluster.size() +
																							" != sizeToSample: " + sizeToSample);
				//System.err.println ("Sampled negative cluster of size " + sampledCluster.size());
				nodeClusterPairs.add (new NodeClusterPair (node, sampledCluster, false));												
				negativeInstancesSeen++;
			}
		}
		return nodeClusterPairs;		
	}

	private Object getRandomNode (ArrayList nodes, Random r) {
		return nodes.get (r.nextInt (nodes.size()-1));
	}
	
	private Collection findRandomClusterWithoutNode (Object n, Collection clustering) {
		if (clustering.size() == 1)
			throw new IllegalArgumentException ("Only works on clustering.size > 2");
		Collection[] clusters = (Collection[])clustering.toArray (new Collection[]{});
		int i = this.r.nextInt (clustering.size()-1);
		if (clusters[i].contains (n))
			return findRandomClusterWithoutNode (n, clustering);
		else
			return clusters[i];		
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

	/** Create a subCluster of "trueCluster" containing "sizeToSample"
	 * nodes, and not including "node"*/
	private Collection sampleSubCluster (Collection trueCluster, Object node, int sizeToSample) {
		ArrayList ret = new ArrayList (sizeToSample);
		ArrayList clusterNodes = new ArrayList (trueCluster.size());
		Iterator iter = trueCluster.iterator ();
		while (iter.hasNext()) {
			Object currNode = iter.next();			
			if (!currNode.equals (node))
				clusterNodes.add (currNode);
		}		
		if (sizeToSample > clusterNodes.size()) 
			throw new IllegalArgumentException ("sizeToSample: " + sizeToSample +
																					" is greater than clusterNodes.size: " + clusterNodes.size());
		Collections.shuffle (clusterNodes, this.r);
		for (int i=0; i < sizeToSample; i++) 
			ret.add (clusterNodes.get(i));
		return ret;
	}
	
	/** Generate training instances by one random "Chinese-Restaurant
	 * Process" style clustering.
	 * @param nodes all nodes
 	 * @param clusters true clustering of nodes
	 * @param r random num generator
	 * @return NodeClusterPair list
	 */
	private ArrayList generateCRPInstances (ArrayList nodes, Collection clusters, Random r) {
		ArrayList nodeClusterPairs = new ArrayList();
		Collection currentClustering = new ArrayList();
		while (nodes.size() > 0) {
			int index = r.nextInt (nodes.size());
			nodeClusterPairs.addAll (getNodeClusterPairs (nodes.get(index), currentClustering,
																										clusters));
			nodes.remove (index);
			System.err.println ("Now have " + currentClustering.size() + " clusters and "
													+ nodes.size() + " nodes remaining.");
		}
		return nodeClusterPairs;
	}
	
	/** Returns an ArrayList of NodeClusterPairs, one for each
	 * comparison made in deciding in which cluster "node" belongs, if
	 * any. Note that the nodes is then placed in the true cluster it
	 * belongs in, or a new cluster is created and added to
	 * "clustering."
	 * @param node node being placed in a cluster
	 * @param clustering clusters to choose from to place node
	 * @param trueClustering true clustering
	 * @return list of NodeClusterPair's
	 */
	private ArrayList getNodeClusterPairs (Object node, Collection clustering,
																				 Collection trueClustering) {
		ArrayList ret = new ArrayList ();
		Iterator clusterIter = clustering.iterator();
		boolean placedInCluster = false;
		while (clusterIter.hasNext()) {
			Collection cluster = (Collection) clusterIter.next();
			boolean belongs = nodeBelongsInCluster (node, cluster, trueClustering);
			if (!belongs && canAddNegative()) {
				this.negativeInstancesSeen++;
				ret.add (new NodeClusterPair (node, cluster, belongs));
			}
			else if (belongs) {
				this.positiveInstancesSeen++;
				ret.add (new NodeClusterPair (node, cluster, belongs));
				cluster.add (node);
				placedInCluster = true;
			}
		}
		if (!placedInCluster) {// create a new cluster for node
			Collection newC = new LinkedHashSet ();
			newC.add (node);
			clustering.add (newC);
		}
		return ret;
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
