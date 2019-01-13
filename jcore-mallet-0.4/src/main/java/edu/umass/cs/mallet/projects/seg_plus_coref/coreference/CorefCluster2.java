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

import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.graphs.*;
import salvo.jesus.graph.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;


/*

This version is different from CorefCluster.java in that it keeps a
publication object as the object of a Node (instead of a list of Citations).
When two publications are merged, the field values are updated appropriately
in the newly merged Publication.  In the graph, the edges weights to the
new publication node are computed by re-applying the classifier to obtain
the edge-weights.  

An object of this class will allow an InstanceList as well as a List of
mentions to be passed to a method that will return a list of lists
representing the partitioning of the mentions into clusters/equiv. classes.
There should be exactly M choose 2 instances in the InstanceList where M is
the size of the List of mentions (assuming a complete graph).
*/
public class CorefCluster2
{
	private final double NegativeInfinite = -1000000000;
	MaxEnt meClassifier = null;
	Pipe   instancePipe = null;

	public CorefCluster2 () {}

	public CorefCluster2 (MaxEnt classifier, Pipe instancePipe) {
		this.meClassifier = classifier;
		this.instancePipe = instancePipe;
	}

	/*
    Initialize a list of lists where each inner list is a list with a single element.
	*/

	public void train (InstanceList ilist, List mentions) {
		// just to plain MaxEnt training for now
		MaxEnt me = (MaxEnt)(new MaxEntTrainer().train (ilist, null, null, null, null));
		this.meClassifier = me;
	}

	public MaxEnt getClassifier() {return meClassifier;}

	/* performance time method */
	public Collection clusterMentions (InstanceList ilist, List mentions) {
		if (meClassifier != null) {
			// Change: mhay 1/10/04
			// must add vertices explicitly, because the ilist may not contain
			// every vertex (i.e. singletons from canopies pre-process)
			WeightedGraph graph = new WeightedGraphImpl();
			System.out.println("The graph has " + graph.getVertexSet().size() + " vertices");
			System.out.println(graph);
			HashMap alreadyAddedVertices = new HashMap(); // keep track of
																											// vertices that have
																											// already been added
			for (int i=0; i<ilist.size(); i++) {
				constructEdgesUsingTrainedClusterer(graph, ilist.getInstance(i),
																						alreadyAddedVertices, meClassifier);
			}
			addVerticesToGraph(graph, mentions, alreadyAddedVertices);
			System.out.println("The graph has " + graph.getVertexSet().size() + " vertices");
			return typicalClusterPartition (graph);
		}
		else { return null; }

	}
	
	public void addVerticesToGraph(WeightedGraph graph,
																					List mentions, HashMap alreadyAddedVertices) {
		for (int i=0; i < mentions.size(); i++) {
			Object o = mentions.get(i);
			if (alreadyAddedVertices.get(o) == null) { // add only if it hasn't been added
				VertexImpl v = new VertexImpl(o);
				try {
					graph.add(v); // add the vertex
				} catch (Exception e) {e.printStackTrace();}
			}
		}
	}

	public Collection typicalClusterPartition (WeightedGraph graph) {

		/*
		Iterator i0 = ((Set)graph.getVertexSet()).iterator();
		while (i0.hasNext()) {
			VertexImpl v = (VertexImpl)i0.next();
			System.out.println("Map: " + v.getObject() + " -> " +
												 ((Citation)((Node)v.getObject()).getObject()).getBaseString() );
												 }*/
		//System.out.println("Top Graph: " + graph);
		while (true) {
			double bestEdgeVal = -100000000;
			WeightedEdge bestEdge = null;
			//System.out.println("Top Graph: " + graph);
			Set edgeSet = graph.getEdgeSet();
			Iterator i1 = edgeSet.iterator();
			// get highest edge value in this loop
			while (i1.hasNext()) {
				WeightedEdge e1 = (WeightedEdge)i1.next();
				if (e1.getWeight() > bestEdgeVal) {
					bestEdgeVal = e1.getWeight();
					bestEdge = e1;
				}
			}
			if (bestEdgeVal < 0.0)
				break;
			else {
				if (bestEdge != null) {
					VertexImpl v1 = (VertexImpl)bestEdge.getVertexA();
					VertexImpl v2 = (VertexImpl)bestEdge.getVertexB();
					/*
					System.out.println("Best edge val: " + bestEdgeVal);
					System.out.println("Merging vertices: " + v1.getObject()
					+ " and " + v2.getObject()); */
					mergeVertices(graph, v1, v2);
				}
			}
		}
		System.out.println("Final graph now has " + graph.getVertexSet().size() + " nodes");
		return getCollectionOfOriginalObjects ((Collection)graph.getVertexSet());
	}

	/**
	 * Construct a Collection of Collections where the objects in the
	 * collections are the original objects (i.e. the object of the vertices)
	 */
	private Collection getCollectionOfOriginalObjects (Collection vertices) {
		Collection collection = new LinkedHashSet();
		Iterator i1 = vertices.iterator();
		while (i1.hasNext()) {
			VertexImpl v = (VertexImpl)i1.next();
			Object o = v.getObject(); // underlying object
			if (o != null) {
				Collection c1 = new LinkedHashSet();
				if (o instanceof Collection) {
					Iterator i2 = ((Collection)o).iterator();
					while (i2.hasNext()) {
						c1.add(i2.next());   // add the underlying object
					}
				} else {
					// in this case, the vertex is a singleton, but we wrap it in a
					// Collection so that we always have a collection of collections
					c1.add(o);
				}
				collection.add(c1); // add the cluster to the collection of clusters
			}
		}
		return collection;
	}

	/**
	 * The graph may not be fully connected.  If an edge does not exist,
	 * that corresponds to an edge weight of negative inifinite.  So,
	 * before merging two vertices, one must add in any negative weights.
	 * For example, if v1 has an edge to v3 but v2 does not, then the
	 * merged v1-v2 should have a negative weighted edge to v3.
	 * @param g
	 * @param v1
	 * @param v2
	 */
	private void addNegativeWeights(WeightedGraph g, Vertex v1, Vertex v2) {
		List adjacentToV1;
		List adjacentToV2;

		adjacentToV1 = g.getAdjacentVertices(v1);
		adjacentToV2 = g.getAdjacentVertices(v2);

		// Check that v1 is connected to all of v2's adjacent vertices
		for (int i=0; i < adjacentToV2.size(); i++) {
			Vertex v = (Vertex)adjacentToV2.get(i);
			//System.out.println("v1: " + v1 + " v: " + v);
			if (v == v1) {
				continue;
			}
			if (!adjacentToV1.contains(v)) {
				try {
					System.out.println("Adding negative infinite edge: " + v1 + " to " + v);
					g.addEdge(v, v1, NegativeInfinite);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		// Check that v2 is connected to all of v1's adjacent vertices
		for (int i=0; i < adjacentToV1.size(); i++) {
			Vertex v = (Vertex)adjacentToV1.get(i);
			//System.out.println("v2: " + v2 + " v " + v + " " + g.isConnected(v, v2));
			if (v == v2) {
				continue;
			}
			if (!adjacentToV2.contains(v)) {
				try {
					System.out.println("Adding negative infinite edge: " + v2 + " to " + v);
					g.addEdge(v, v2, NegativeInfinite);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
	}

	public void mergeVertices (WeightedGraph g, VertexImpl v1, VertexImpl v2) {
		// Change: mhay 1/10/04
		addNegativeWeights(g, v1, v2);

		Publication p1 = (Publication)((Node)v1.getObject()).getObject();
		Publication p2 = (Publication)((Node)v2.getObject()).getObject();

		p1.mergeNewPublication (p2);

		VertexImpl newVertex = new VertexImpl(p1);
		List edges1 = (List)g.getEdges(v1);
		Iterator i1 = edges1.iterator();

		HashMap hm = new HashMap();
		while (i1.hasNext()) {
			WeightedEdge e = (WeightedEdge)i1.next();
			if ((e.getVertexA() == v1) && (e.getVertexB() != v2))
				hm.put((Object)e.getVertexB(), new Double(e.getWeight()));
			else if (e.getVertexA() != v2)
				hm.put((Object)e.getVertexA(), new Double(e.getWeight()));
		}
		try {
			g.remove(v1); // this also removes all edges incident with this vertex
		} catch (Exception ex) {ex.printStackTrace();}

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

				// create a new instance from new node pair
				Instance e_inst = new Instance (new NodePair (new Node(p1), new Node(p2)),
																				null, null, null, this.instancePipe);
				Classification classification = (Classification)meClassifier.classify(e_inst);
				Labeling labeling = classification.getLabeling();
				double edgeVal;
				//classifier.getUnnormalizedClassificationScores(instPair, scores);

				/** NOTE:  THIS ASSUMES THERE ARE JUST TWO LABELS - IE CLASSIFIER IS
				 * BINARY */
				if (labeling.labelAtLocation(0).toString().equals("no")) {
					edgeVal =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
				} else {
					edgeVal =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
				}
				
				WeightedEdge ne = new WeightedEdgeImpl(newVertex, cv, edgeVal);
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
	}

	public static void constructEdgesUsingTrainedClusterer (WeightedGraph graph,
																													Instance instPair,
																													HashMap alreadyAdded,
																													MaxEnt classifier)
	{
		NodePair mentionPair = (NodePair)instPair.getSource();
		Object node1 = mentionPair.getObject1();
		Object node2 = mentionPair.getObject2();
		VertexImpl v1 = (VertexImpl)alreadyAdded.get(node1);
		VertexImpl v2 = (VertexImpl)alreadyAdded.get(node2);

		if (v1 == null) {
			v1 = new VertexImpl(node1);
			alreadyAdded.put(node1,v1);
		}
		if (v2 == null) {
			v2 = new VertexImpl(node2);
			alreadyAdded.put(node2,v2);
		}
		
		double  edgeVal = 0.0;
		double scores[] = new double[2];  // assume binary classification
		// Include the feature weights according to each label
		Classification classification = (Classification)classifier.classify(instPair);
		Labeling labeling = classification.getLabeling();
		//classifier.getUnnormalizedClassificationScores(instPair, scores);

		/** NOTE:  THIS ASSUMES THERE ARE JUST TWO LABELS - IE CLASSIFIER IS
		 * BINARY */
		if (labeling.labelAtLocation(0).toString().equals("no")) {
			edgeVal =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
		} else {
			edgeVal =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
		}

//		if(edgeVal > 0)
//			System.out.println( ((Node)antecedent).getString() + "-" + ((Node)referent).getString() + " - " + edgeVal);


//		edgeVal = mentionPair.getFeatureValue("editDistance");

		try {
			if (node1 != null && node2 != null) {
				graph.addEdge (v1, v2, edgeVal);
			}
		} catch (Exception e) {e.printStackTrace();}
	}


}
