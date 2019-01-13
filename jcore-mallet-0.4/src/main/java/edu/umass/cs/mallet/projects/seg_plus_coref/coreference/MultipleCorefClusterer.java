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

package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.graphs.*;
import salvo.jesus.graph.*;
import salvo.jesus.graph.VertexImpl;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.util.logging.*;
import java.lang.*;
import java.io.*;

/** Clusters multiple objects simultaneously (e.g. Authors, venues,
 * and papers). Trains a separate MaxEnt classifier to weight edges
 * between objects of the same type. Partitions resulting graph such
 * that constraints between objects are respected (e.g. clustering two
 * authors requires their venues be in the same cluster)*/
public class MultipleCorefClusterer extends CorefClusterAdv {
	private static Logger logger = MalletLogger.getLogger (MultipleCorefClusterer.class.getName());

	/** Classifiers which determine the edge weights between two nodes
	 * of the same type */
	MaxEnt[] classifiers;

	/** Pipes to extract features from pairs of nodes */
	Pipe[] pipes;

	/** Maps a citation type to which index into the instanceList has
	 * this type*/
	HashMap type2index;

	/** Maps a paperVertex to corresponding venueVertex. Enforces that
	 * all papers in a cluster (paperVertex) have venues in the same
	 * cluster (venueVertex) */
	HashMap paperVertex2VenueVertex;
	
	/** Constants for the types of citation nodes */
	public static final String PAPER = "paper";
	public static final String VENUE = "venue";
	public static final String AUTHOR = "author";
		
	public MultipleCorefClusterer (Pipe[] _pipes) {
		this.pipes = _pipes;
		type2index = new HashMap();
		paperVertex2VenueVertex = new HashMap ();
	}

	/** Train the underlying classifiers with "ilists" as
	 * trainingData */
	public void train (InstanceList[] ilists) {
		setIndices (ilists);
		classifiers = new MaxEnt[ilists.length];
		for (int i=0; i < ilists.length; i++) {
			logger.info("Training Coreference Classifiers["+i+"]: ");
			classifiers[i] = trainClassifier (ilists[i]);
		}
		this.meClassifier = classifiers[0];
	}

	/** Sets the mapping from citation type to index */
	public void setIndices (InstanceList[] ilists) {
		for (int i=0; i < ilists.length; i++) 
			setIndex (getType (ilists[i]), i);
	}

	/** Adds to "type2index" hash the mapping type->i */
	private void setIndex (String type, int i) {
		if (type.equals (this.PAPER))			
			type2index.put (this.PAPER, new Integer (i));
		else if (type.equals (this.VENUE))			
			type2index.put (this.VENUE, new Integer (i));
		else if (type.equals (this.AUTHOR))			
			type2index.put (this.AUTHOR, new Integer (i));
		else
			throw new IllegalArgumentException ("Unknown citation type: " + type);		
	}

	/** Citation type in this ilist */
	private String getType (InstanceList ilist) {
			NodePair mentionPair = (NodePair)ilist.getInstance(0).getSource();			
			Citation c = (Citation)mentionPair.getObject1();
			return getType (c);
	}
	
 	/** Citation type in VertexImpl */
	private String getType (Collection c) {
		Iterator liter = c.iterator();
		String type = null;
		while (liter.hasNext()) {
			String currType = getType ((Citation)liter.next());
			if (type != null && !type.equals(currType))
				throw new IllegalArgumentException ("SERIOUS ERROR: Cluster has nodes of type " +
																						type + " AND type " + currType);
			type = currType;
		}
		return type;
	}

	/** Citation type in VertexImpl */
	private String getType (VertexImpl v) {
		Object o = v.getObject();
		List l = null;
		if (!(o instanceof List)) {
			l = new ArrayList ();
			l.add (o);
		}
		else
			l = (List) o;
		return getType(l);
	}
	
	/** Returns the type of citation of c */
	private String getType (Citation c) {
		if (c instanceof PaperCitation)
			return this.PAPER;
		else if (c instanceof VenueCitation)
			return this.VENUE;
		else if (c instanceof AuthorCitation)
			return this.AUTHOR;
		else
			throw new IllegalArgumentException ("Unknown citation type: " + c.getClass().getName());
	}
	
	public void testClassifiers (InstanceList[] ilists) {
		if (!typeOrdersMatch (ilists))
			throw new IllegalArgumentException ("ilists types in testing  not in same order as in training");
		for (int i=0; i < ilists.length; i++)
			testClassifier (ilists[i], this.classifiers[i]);
	}

	/** True if the citation type in ilists[i] equals the citation type
	 * seen in ilist[i] during training */
	private boolean typeOrdersMatch (InstanceList[] ilists) {
		for (int i=0; i < ilists.length; i++) {
			Integer t = (Integer) type2index.get (getType (ilists[i]));
			if (t == null || !t.equals (new Integer (i)))
				return false;
		}
		return true;
	}

	/** Returns a list of collections representing the clustering of "ilists" */
	public Collection[] clusterMentions (InstanceList[] ilists, List[] mentions,
																			 int optimalBest, boolean stochastic) {
		if (!typeOrdersMatch (ilists))
			throw new IllegalArgumentException ("ilists types in clustering not in same order as in training");			
		if (classifiers == null)
			throw new IllegalStateException ("Must train classifiers before clustering");
		if (optimalBest > 0) {
			throw new UnsupportedOperationException ("Not yet implemented for nBest clustering");
		}
		else {
			if (fullPartition) {
				wgraph = createMultipleTypeGraph (ilists, mentions);
				logger.info ("Created Multi-Graph with " + wgraph.getVerticesCount() + " vertices and " +
										 wgraph.getEdgesCount() + " edges");
				if (type2index.get (this.VENUE) != null &&
						type2index.get (this.PAPER) != null)
					this.paperVertex2VenueVertex = getPaper2VenueHash (wgraph);
				Collection clustering = partitionGraph (wgraph);
				return splitClusteringByType (clustering);
			}
			else if (stochastic) {
				throw new UnsupportedOperationException ("Not yet implemented for stochastic clustering");
			}
			else {
				wgraph = createMultipleTypeGraph (ilists, mentions);
				logger.info ("Created Multi-Graph with " + wgraph.getVerticesCount() + " vertices and " + wgraph.getEdgesCount() + " edges");
				if (type2index.get (this.VENUE) != null && type2index.get(this.PAPER) != null)
					this.paperVertex2VenueVertex = getPaper2VenueHash (wgraph);
				Collection clustering = typicalClusterPartition (wgraph);
				logger.info ("Resulting clustering of all types has " + clustering.size() + " clusters");
				Collection[] ret =  splitClusteringByType (clustering);
				for (int cint=0; cint < ret.length; cint++) 
					logger.info ("clustering of type " + cint + " has " + ret[cint].size() + " clusters.");
				return ret;
			}
		}
	}

	private Collection[] splitClusteringByType (Collection clustering) {
		ArrayList[] ret = new ArrayList[type2index.size()];
		for (int i=0; i < ret.length; i++)
			ret[i] = new ArrayList();
		Iterator iter = clustering.iterator();
		System.err.println ("Cluster types: " + type2index);
		while (iter.hasNext()) {
 			Collection c = (Collection) iter.next();
			String type = getType (c);
			int index = ((Integer)type2index.get(type)).intValue();
			if (index >= type2index.size())
				throw new IllegalArgumentException ("index " + index + " greater than number of citation types");
			ret[index].add (c);
		}
		return ret;
	}

	private List getVenueVertices (WeightedGraph graph) {
		Iterator iter = graph.getVerticesIterator ();
		ArrayList venueVertices = new ArrayList ();
		while (iter.hasNext ()) {
			VertexImpl v = (VertexImpl)iter.next();
			if (getType(v).equals(this.VENUE))
				venueVertices.add (v);
		}
		return venueVertices;
	}
	
	/** Map each paper vertex to its corresponding venue vertex (if one exists) */
	private HashMap getPaper2VenueHash (WeightedGraph graph) {
		logger.info ("creating paperVertex2VenueVertex hash...");
		HashMap hash = new HashMap ();
		Iterator iter = graph.getVerticesIterator ();
		ArrayList paperVertices = new ArrayList ();
		ArrayList venueVertices = new ArrayList ();
		while (iter.hasNext ()) {
			VertexImpl v = (VertexImpl)iter.next();
			if (getType(v).equals(this.PAPER))
				paperVertices.add (v);
			else if (getType(v).equals(this.VENUE))
				venueVertices.add (v);
		}
		logger.info ("found " + paperVertices.size() + " paper vertices and " +
								 venueVertices.size() + " venue vertices"); 
		for (int i=0; i < paperVertices.size(); i++) {
			VertexImpl v = (VertexImpl)paperVertices.get (i);
			Object o = v.getObject();
			List l = null;
			if (!(o instanceof List)) {
				l = new ArrayList ();
				l.add (o);
			}
			else
				l = (List) o;
			Iterator liter = l.iterator();
			VertexImpl venueVertex = null;			
			while (liter.hasNext()) {
				Citation c = (Citation) liter.next();
				VertexImpl currVenue = findVenueVertexForPaperCitation (c, venueVertices);
				logger.info ("Venue for citation " + c + "\nis\n" + currVenue);
				if (venueVertex != null && !currVenue.equals(venueVertex))
					throw new IllegalArgumentException ("Coreferent papers have NON-coreferent venues in cluster " + v);
				venueVertex = currVenue;
			}
			if (venueVertex == null)
				logger.warning ("Can't find venue vertex for citation " + v);
			else
				hash.put (v, venueVertex);
		}
		return hash;
	}

	/** Given PaperCitation c, find the VenueCitation vertex of c's venue */
	private VertexImpl findVenueVertexForPaperCitation (Citation c, List venueVertices) {
		if (!getType(c).equals(this.PAPER))
			throw new IllegalArgumentException ("Citation has type " + getType(c) + ", not " + this.PAPER);
		String venueID = c.getField (Citation.venueID);
		if  (venueID == "") // no venue for this paper
			return null;
		VertexImpl ret = null;
		for (int i=0; i < venueVertices.size(); i++) {
			VertexImpl v = (VertexImpl)venueVertices.get(i);
			Object o = v.getObject();
			List l = null;
			if (o instanceof Citation) {
				l = new ArrayList();
				l.add ((Citation) o);
			}
			else 
				l = (List) o;
			Iterator iter = l.iterator();
			while (iter.hasNext()) {
				Citation vc = (Citation) iter.next();
				String currVenueID = vc.getField (Citation.venueID);
				if (currVenueID == "")
					throw new IllegalArgumentException ("VenueCitation has no venueID: " + vc);
				if (currVenueID.equals(venueID)) { // found it
					logger.info ("Found venue id " + venueID);
					l.remove (v);
					ret = v;
					return v;
				}
			}			
		}
		if (ret == null)
			throw new IllegalArgumentException ("Can't find venue vertex for citation " + c);
		return null;
	}
	
	/** Create a graph where each citation type has its own connected
	 * components. Within each connected component, edge weights are
	 * determined by MaxEnt posterior. */
	private WeightedGraph createMultipleTypeGraph (InstanceList[] ilists, List[] mentions) {
		WeightedGraph graph = new WeightedGraphImpl ();
		for (int i=0; i < ilists.length; i++)
			graph = createGraph (ilists[i], mentions[i], graph, classifiers[i]);
		return graph;
	}

	/** Find the vertex in g containing the Venue of paperVertex */
  private VertexImpl findVenuesOfPapers (VertexImpl paperVertex) {
		String t = getType (paperVertex);
		if (!t.equals (this.PAPER)) 
			throw new IllegalArgumentException ("paperVertex is not a paper, it's a " + t);
		return (VertexImpl) paperVertex2VenueVertex.get (paperVertex);
	}
	
	/** This is where we enforce constraints between object
	  types. Constraints are:
		
		If v1,v2 are Citation objects, find their corresponding
	  VenueCitations and AuthorCitations and merge them.

		We do this by first simply merging the paper vertices, then
		merging their corresponding venue and author vertices.
	*/
	public void mergeVertices (WeightedGraph g, VertexImpl v1, VertexImpl v2) {
		VertexImpl mergedVertex = mergeSameTypeVertices (g, v1, v2);
		Object o1 = v1.getObject ();
		Object o2 = v2.getObject ();
		Citation c1 = (o1 instanceof List) ? (Citation)((List)o1).get(0) : (Citation)o1;
		Citation c2 = (o2 instanceof List) ? (Citation)((List)o2).get(0) : (Citation)o2;
		if (getType(c1).equals (this.PAPER) && type2index.get(this.VENUE) != null &&
				type2index.get(this.PAPER) != null) {
		 	if (getType(c2).equals (this.PAPER)) {
				//	VertexImpl venues1 = findVenuesOfPapers (v1);
				//VertexImpl venues2 = findVenuesOfPapers (v2);
				List venueVertices = getVenueVertices (g);
				VertexImpl venues1 = findVenueVertexForPaperCitation (c1, venueVertices);
				VertexImpl venues2 = findVenueVertexForPaperCitation (c2, venueVertices);
				if (venues1 != null && venues2 != null && !venues1.equals(venues2)) {
					logger.info ("Merging venues of papers:\nCitation1:  " + idstrings(v1) +
											 "\nVenue: " + idstrings(venues1) +
											 "\nCitation2: " + idstrings(v2) + "\nVenue: " + idstrings(venues2));
				 	VertexImpl mergedVenueVertex = mergeSameTypeVertices (g, venues1, venues2);
					// update hash with mergedVertex
					paperVertex2VenueVertex.put (mergedVertex, mergedVenueVertex); 					
					paperVertex2VenueVertex.remove (v1);
					paperVertex2VenueVertex.remove (v2);
				}
		 	}
			else {
			 	throw new IllegalArgumentException ("Vertices not of same type: v1: " + c1.getClass().getName() +
																						" v2: " + c2.getClass().getName());
			}
		}
	}

	private String idstrings (VertexImpl v) {
		String ret = "";
		Object o = v.getObject();
		ArrayList l = new ArrayList();
		if (o instanceof Citation)
			l.add(o);
		else l = (ArrayList) o;
		for (int i=0; i < l.size(); i++) {
			Citation c = (Citation) l.get(i);
			ret += "pid: " + c.getField(Citation.paperID) + " pcid: " + c.getField(Citation.paperCluster) +
						 " vid: " + c.getField(Citation.venueID) + " vcid: " + c.getField(Citation.venueCluster) + "\n";
		}
		return ret;
	}

	private VertexImpl mergeSameTypeVertices (WeightedGraph g, VertexImpl v1, VertexImpl v2) {
		// xxx warning: mostly copy and pasted from CorefClusterAdv.mergeVertices
		logger.info ("Merging nodes of type " + getType (v1));
		Object o1 = v1.getObject();
		Object o2 = v2.getObject();
		List l1 = new ArrayList();

		if (o1 instanceof List)
			l1.addAll ((List)o1);
		else l1.add (o1);
		if (o2 instanceof List)
			l1.addAll ((List)o2);
		else l1.add (o2);

		/*	if ((o1 instanceof List) && (o2 instanceof List)) {
	    l1.addAll((List)o1);
	    l1.addAll((List)o2);
		} else if (o1 instanceof List) {
	    l1.addAll((List)o1);
	    l1.add(o2);
		} else if (o2 instanceof List) {
	    l1.addAll((List)o2);
	    l1.add(o1);
		} else {
	    l1.add(o1);
	    l1.add(o2);
		}
		*/

		VertexImpl newVertex = new VertexImpl(l1);
		try {
	    g.add(newVertex);
		} catch (Exception e) {
	    e.printStackTrace();
		}
		List edges1 = (List) g.getEdges(v1);
		Iterator i1 = edges1.iterator();

		HashMap hm = new HashMap();
		while (i1.hasNext()) {
	    WeightedEdge e = (WeightedEdge) i1.next();
	    if ((e.getVertexA() == v1) && (e.getVertexB() != v2))
				hm.put((Object) e.getVertexB(), new Double(e.getWeight()));
	    else if (e.getVertexA() != v2)
				hm.put((Object) e.getVertexA(), new Double(e.getWeight()));
		}
		try {
	    g.remove(v1); // this also removes all edges incident with this vertex
		} catch (Exception ex) {
	    ex.printStackTrace();
		}

		//		System.out.println("Hashmap for vertex: " + v1 + " is: " + hm);
		List edges2 = (List)g.getEdges(v2);
		Iterator i2 = edges2.iterator();
		Vertex cv = null;
				
		if (edges2.size() > 0) {
	    while (i2.hasNext()) {
				WeightedEdge e = (WeightedEdge)i2.next();
				if (e.getVertexA() == v2)
					cv = e.getVertexB();
				else
					cv = e.getVertexA();

				double w2 = ((Double)hm.get(cv)).doubleValue();
				double w1 = e.getWeight();
				//double weight = (w1 > w2) ? w1 : w2;   // max
				double weight = (w1 + w2)/2;   // avg
				if (w1 == NegativeInfinite || w2 == NegativeInfinite) {
					weight = NegativeInfinite; // precaution: avoid creeping away from Infinite
				}
				WeightedEdge ne = new WeightedEdgeImpl(newVertex, cv, weight);
				try {
					g.addEdge(ne);
				} catch (Exception ex) { ex.printStackTrace(); }
	    }
		} else { // in this case, no edges are left, just add new Vertex
	    try {
				g.add(newVertex);
	    }	catch (Exception ex) { ex.printStackTrace(); }
		}
		try {
	    g.remove(v2);
		}	catch (Exception ex) { ex.printStackTrace(); }		
		//		System.out.println("After adding new edges: " + g);
		return newVertex;
	}

	/** Partition the graph by greed agglomerative merging. Copied from
	 * CorefClusterAdv.java. Added bookkeeping for
	 * paperVertex2VenueVertex hash. */
	public Collection partitionGraph (WeightedGraph origGraph) {

		if (trueNumStop)
			throw new UnsupportedOperationException ("trueNumStop=true unsupported");
		java.util.Random rand = new java.util.Random();
		
		double bestCost = -100000000000.0;
		double curCost = bestCost;
		Collection bestPartitioning = null;
		
		// evalFreq is the frequency with which evaluations occur
		// in the early stages, it is silly to keep doing a complete
		// evaluation of the objective fn

		
		for (int i=0; i < MAX_ITERS; i++) {
	    //System.out.println("Iteration " + i);
	    double cost = -100000000.0;
	    double bCost = cost;

	    int evalFreq = 10;

	    // this is a counter that will increment each time
	    // a new edge is tried and the result is a graph
	    // with a reduced total objective value
	    int numReductions = 0;			double treeCost = 0.0;

	    int iter = 0;
	    Collection curPartitioning = null;
	    Collection localBestPartitioning = null;
	    WeightedGraph graph = copyGraph(origGraph);
	    WeightedGraph graph1 = copyGraph(graph);
	    while (true) {
				HashMap previousHash = (HashMap)this.paperVertex2VenueVertex.clone();
				Collection c0 = (Collection)graph.getEdgeSet();
				List sortedEdges = Collections.list(Collections.enumeration(c0));
				System.out.println("Size of sorted edges: " + sortedEdges.size());
				if (sortedEdges.size() > 0) {
					EdgeComparator comp = new EdgeComparator();
					Collections.sort(sortedEdges, comp);
					double minVal = ((WeightedEdge)sortedEdges.get(sortedEdges.size()-1)).getWeight();
					double totalVal = 0.0;
					Iterator il = (Iterator)sortedEdges.iterator();
					while (il.hasNext()) {
						totalVal += ((WeightedEdge)il.next()).getWeight();
					}
					totalVal += sortedEdges.size()*(-minVal);

					//WeightedEdge chosenEdge = chooseEdge(sortedEdges, minVal,
					//totalVal, rand);
					//WeightedEdge chosenEdge = chooseEdge2(sortedEdges, minVal,
					//totalVal, rand);
					WeightedEdge chosenEdge = chooseEdge2(sortedEdges, minVal, totalVal, rand);
					if (chosenEdge != null) {
						VertexImpl v1 = (VertexImpl)chosenEdge.getVertexA();
						VertexImpl v2 = (VertexImpl)chosenEdge.getVertexB();

						/*
							System.out.println("Best edge val: " + chosenEdge.getWeight());
							System.out.println("Merging vertices: " + v1.getObject()
							+ " and " + v2.getObject()); */
						mergeVertices(graph, v1, v2);
					}
				} else // edges has size zero
					break;
				if ((evalFreq > 0) && ((iter % evalFreq) == 0)) {
					curPartitioning = getCollectionOfOriginalObjects ((Collection)graph.getVertexSet());
					cost = evaluatePartitioning(curPartitioning, origGraph);

					if (keyPartitioning != null && curPartitioning != null) {
						PairEvaluate pairEval = new PairEvaluate (keyPartitioning, curPartitioning);
						pairEval.evaluate();
						treeCost = 0.0;
						if (treeModel != null)
							treeCost = treeModel.computeTreeObjFn (curPartitioning);

						System.out.println(cost + "," + evaluateAgainstKey (curPartitioning)
															 + "," + pairEval.getF1() + "," + treeCost + "," +
															 curPartitioning.size());
					} else {
						if (keyPartitioning == null)
							System.out.println(" keyPart is NULL!!");
						if (curPartitioning == null)
							System.out.println(" curPart is NULL!!");
					}
					
					//System.out.println("Cost at iteration " + iter + " is: " + cost);
					// in this case, we've gone too far and have lowered the FN value
					if (!trueNumStop && cost <= bCost) {
						//System.out.println("Graph val reduced to " + cost + " from " + prevCost);
						//System.out.println("Due to merging on edge with weight: " +
						//chosenEdge.getWeight());
						graph = graph1; // set graph to what is was before the merge
						this.paperVertex2VenueVertex = previousHash; 
						numReductions++; // increment the number of reductions

						//reset the evaluation frequency
						evalFreq = (int)Math.ceil(evalFreq/2);
					}
					else {
						numReductions = 0;
						localBestPartitioning = curPartitioning;
						bCost = cost; // let prevCost now equal the new higher cost
					}
					// only copy graph if we allow back-tracking in search
					if (!trueNumStop)
						graph1 = copyGraph(graph);  // make a copy of the graph
				}

				iter++;
				// we stop when we've tried to increase the obj fn value
				// MAX_REDUCTIONS times
				if (!trueNumStop && (numReductions > MAX_REDUCTIONS))
					break;

				if (trueNumStop && (graph.getVertexSet().size() <= keyPartitioning.size())) {
					localBestPartitioning = curPartitioning;
					break;
				}
	    }
	    curPartitioning = getCollectionOfOriginalObjects ((Collection)graph.getVertexSet());			
	    if (localBestPartitioning == null)
				localBestPartitioning = curPartitioning;
			
	    //System.out.println("best cost was: " + bCost);
	    curCost = evaluatePartitioning(localBestPartitioning, origGraph);
	    //System.out.println("curCost is: " + curCost);
	    //double curAgree = evaluatePartitioningAgree(localBestPartitioning, origGraph);
	    //double curDisAgree = evaluatePartitioningDisAgree(localBestPartitioning, origGraph);
			if (keyPartitioning != null) {
				PairEvaluate pairEval = new PairEvaluate (keyPartitioning, localBestPartitioning);
				pairEval.evaluate();
			}
	    if (treeModel != null)
				treeCost = treeModel.computeTreeObjFn (curPartitioning);

	    if (curCost > bestCost) {
				bestCost = curCost;
				bestPartitioning = localBestPartitioning;

	    }
			System.out.println ("Cost: " + curCost);
	    //System.out.println(curCost + "," + evaluateAgainstKey (localBestPartitioning)
			//									 + "," + pairEval.getF1() + "," + treeCost + "," + keyPartitioning.size());

		}
		return bestPartitioning;

	}
}
