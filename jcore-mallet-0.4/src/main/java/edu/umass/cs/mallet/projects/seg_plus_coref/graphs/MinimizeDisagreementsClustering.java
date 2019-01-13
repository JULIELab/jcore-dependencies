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
	 @author Ben Wellner
 */
package edu.umass.cs.mallet.projects.seg_plus_coref.graphs;

import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
import java.util.*;
import java.io.*;
import java.text.*;
import java.lang.reflect.Array;
import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import salvo.jesus.graph.visual.layout.*;

public class MinimizeDisagreementsClustering
{


	final static double THRESHOLD = 7.0;

	WeightedGraph origGraph;
	SortedSet   origVertices;  // set of vertices in graph - unmodifiable
	Set   clusters; // set of sets of vertices
	Random randGen;    // will use this quite a bit in this class
	int   graphSize;
	Object []vertexArray;
	double delta;
	double removeDelta;
	double addDelta;
	int goodsCnt = 0;
	int removesCnt = 0;
	HashMap vertexMap;  // maps graph copys vertices to original graphs vertices
	Comparator comparator;
	MappedGraph  mappedGraph = null;
    
	public int getGoods ()
	{
		return goodsCnt;
	}
    
	public int getRemoves ()
	{
		return removesCnt;
	}

	public MinimizeDisagreementsClustering (MappedGraph g, double d)
	{
		this (g.getGraph(), d);
		mappedGraph = g;
	}

	public MinimizeDisagreementsClustering (WeightedGraph g, double d)
	{
		origGraph = g;
		comparator = getComparator(g);
		origVertices = sortSet(origGraph.getVertexSet(),comparator);
		randGen = new Random();
		graphSize = origVertices.size();
		delta = d;
		removeDelta = delta * 3;
		addDelta = delta * 7;
	}

	private Comparator getComparator (Graph g)
	{
		Iterator i = g.getVerticesIterator();
		VertexImpl v = null;
		if (i.hasNext()) {
	    v = (VertexImpl)i.next();
		} else {
	    System.out.println("Warning: No Comparator");
		}
		if ((v != null) && (v.getObject() instanceof Mention))
	    return new CompareMentionVertices();
		else 
	    return new CompareVertices();
	}

	private HashMap createVertexMap (Set v1, Set v2)
	{
		HashMap map = new HashMap();
		Object[] a1 = v1.toArray();
		Object[] a2 = v2.toArray();
		for (int i=0; i < Array.getLength(a1); i++) {
	    map.put(a1[i],a2[i]);
		}
		return map;
	}

	public Object deepCopy(Object oldObj) throws Exception
	{
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try
		{
			ByteArrayOutputStream bos =
		    new ByteArrayOutputStream(); // A
			oos = new ObjectOutputStream(bos); // B
			// serialize and pass the object
			oos.writeObject(oldObj);   // C
			oos.flush();               // D
			ByteArrayInputStream bin =
		    new ByteArrayInputStream(bos.toByteArray()); // E
			ois = new ObjectInputStream(bin);                  // F
			// return the new object
			return ois.readObject(); // G
		}
		catch(Exception e)
		{
			System.out.println("Exception in ObjectCloner = " + e);
			throw(e);
		}
		finally
		{
			if(oos != null)
		    oos.close();
			if(ois != null)
		    ois.close();
		}
	}

	private boolean allIntegers (Set vertices)
	{
		Iterator i = vertices.iterator();
		while (i.hasNext()) {
	    VertexImpl v = (VertexImpl)i.next();
	    if (!(v.getObject() instanceof Integer))
				return false;
		}
		return true;
	}


	/***** 
				 Greedy aglomerative clustering implemented here....
	*/

	public Clustering getClusteringGreedily ()
	{
		double cost = 10000.0;
		boolean notFinished = true;
		WeightedGraph curGraph = null; // make copy of original graph

		try {
	    if (allIntegers (origGraph.getVertexSet()))
				System.out.println("All vertex objects are integers");
	    else
				System.out.println("At least one vertex object is a non-integer");
	    curGraph = (WeightedGraph)deepCopy(origGraph); // copy graph, since we muck with it
		} catch (Exception e) {e.printStackTrace();}
		vertexMap = createVertexMap(sortSet(curGraph.getVertexSet(), comparator),
																origVertices); // create map between copy and original	

		Set curSet = curGraph.getVertexSet();
		Iterator i = curSet.iterator();
		Clustering curClustering = new Clustering();
		while (i.hasNext()) {
	    Cluster cl = new Cluster();
	    cl.add(i.next());
	    curClustering.add(cl);
		}

		System.out.println("Initial clustering: ");
		curClustering.print();

		while (cost > THRESHOLD) {
	    cost = nextBestClustering(curClustering, curGraph);
	    System.out.println("GRAPH COST: " + cost);
	    curClustering.print();
		}
		if (mappedGraph != null) {
	    System.out.println("Remapping clusters:");
	    return remapClusters(curClustering);
		}
		else 
	    return curClustering;
	}
    
	public double nextBestClustering (Set curClustering, WeightedGraph graph)
	{
		double bestScore = 0.0;
		Cluster best1 = null;
		Cluster best2 = null;
		Object cArray[] = curClustering.toArray();
		for (int i=0; i<curClustering.size(); i++) {
	    for (int j=0; j < i; j++) {
				double curScore = evaluatePair((Cluster)cArray[i],(Cluster)cArray[j], graph);
				if (curScore > bestScore) {
					bestScore = curScore;
					best1 = (Cluster)cArray[i];
					best2 = (Cluster)cArray[j];
				}
	    }
		}
		curClustering.remove(best1);
		curClustering.remove(best2);
		if ((best1 != null) && (best2 != null))
	    System.out.println("Merging clusters: " + best1 + " and " + best2);
		curClustering.add(mergeClusters(best1,best2));
		return bestScore;
	}

	public double evaluatePair (Cluster c1, Cluster c2, WeightedGraph graph)
	{
		double total = 0.0;
		int numEdges = 0;
		Iterator i1 = c1.iterator();
		while (i1.hasNext()) {
	    List edges = graph.getEdges((Vertex)i1.next());
	    Iterator e1 = edges.iterator();
	    while (e1.hasNext()) {
				WeightedEdge edge = (WeightedEdge)e1.next();
				if ((c2.contains(edge.getVertexA())) || (c2.contains(edge.getVertexB()))) {
					numEdges++;
					total += edge.getWeight();
				}
	    }
		}
		return total/(double)numEdges;
	}

	public Cluster mergeClusters (Cluster c1, Cluster c2) 
	{
		Cluster newCluster = new Cluster();
		Iterator i1 = c1.iterator();
		while (i1.hasNext()) {
	    newCluster.add(i1.next());
		}
		Iterator i2 = c1.iterator();
		while (i2.hasNext()) {
	    newCluster.add(i2.next());
		}
		return newCluster;
	}
    

	// this is the old case (where the threshold wasn't a parameter)
	// default threshold to 0.0 here
	public Clustering getClustering (List selectVertices) {
		return getClustering (selectVertices, 0.0);
	}
	    
	public Clustering getClustering (List selectVertices, double threshold)
	{
		Clustering clusters = new Clustering();
		WeightedGraph curGraph = null; // make copy of original graph
		List newSelectVertices = new Stack();

		try {
	    if (allIntegers (origGraph.getVertexSet()))
				System.out.println("All vertex objects are integers");
	    else
				System.out.println("At least one vertex object is a non-integer");
	    curGraph = (WeightedGraph)deepCopy(origGraph); // copy graph, since we muck with it
		} catch (Exception e) {e.printStackTrace();}
		vertexMap = createVertexMap(sortSet(curGraph.getVertexSet(), comparator),
																origVertices); // create map between copy and original	
		while (!(curGraph.getVertexSet().isEmpty()))
		{
			Vertex v = null;
			//Vertex v = selectRandomVertex(curGraph);
			if ((selectVertices != null)) {
		    while ((!selectVertices.isEmpty()) && (!curGraph.getVertexSet().contains(v))) {
					v = (Vertex)selectVertices.remove(0);
		    }
		    if (v == null)
					v = selectHeaviestVertex(curGraph);
			}
			else {
		    v = selectHeaviestVertex(curGraph);
			}
			newSelectVertices.add(v);
			Set nPlus = getNPlus(curGraph, v);
			nPlus.add(v); // initial cluster must include selected vertex (of course)
			Set cluster = sortSet(findOptimalCluster (curGraph, 
																								(Set)sortSet(nPlus, comparator),
																								threshold), 
														comparator);
			if (!(cluster.isEmpty())) {
		    Iterator i = cluster.iterator();
		    while (i.hasNext()) {
					try {
						curGraph.remove((Vertex)i.next()); // remove created cluster from graph
					} catch (Exception e) {e.printStackTrace();}
		    }
		    Cluster toAdd = mapCluster(vertexMap, cluster);
		    clusters.add(toAdd);
			} else {
		    Iterator iter = curGraph.getVertexSet().iterator();
		    while (iter.hasNext()) {
					Cluster s = new Cluster();
					s.add(iter.next());
					clusters.add(s);
		    }
		    break;
			}
		}
		if (mappedGraph != null) {
	    Clustering cl = remapClusters (clusters);
	    cl.setSelectVertices (newSelectVertices);
	    return cl;
		}
		else {
	    clusters.setSelectVertices (newSelectVertices);
	    return clusters;
		}
	}

	// this method remaps clusters back into the original objects associated with a MappedGraph
	//  ... yes there are way too many hashtables in this object . . .
	private Clustering remapClusters (Clustering clusters)
	{
		Clustering set = new Clustering();
		Iterator i = clusters.iterator();
		while (i.hasNext()) {
	    Cluster cluster = (Cluster)i.next();
	    LinkedHashSet s1 = new LinkedHashSet();
	    Iterator i1 = cluster.iterator();
	    while (i1.hasNext()) {
				Vertex v = (Vertex)i1.next();
				s1.add(mappedGraph.getObjectFromVertex (v));
	    }
	    set.add(s1);
		}
		return set;
	}

	private Cluster mapCluster (HashMap map, Set cluster)
	{
		Cluster realCluster = new Cluster ();
		Iterator i = cluster.iterator();
		while (i.hasNext()) {
	    realCluster.add(map.get(i.next()));
		}
		return realCluster;
	}

	// method will select vertex in Graph that has the highest sum of incident edges (> 0)
	// could also choose vertex that has the highest variance over incident edges 
	// i.e. most of the values are close to 1 or -1 (not 0) - this is the vertex we
	// are most "confident about"
	private Vertex selectHeaviestVertex (Graph g)
	{
		double curHeaviestWeight = -1.0;
		Vertex curHeaviest = null;
		Set set = sortSet(g.getVertexSet(), comparator);
		Iterator i = set.iterator();	
		while (i.hasNext()) {
	    Vertex v = (Vertex)i.next();
	    double vWeight = getVWeight (v, g);
	    if (vWeight > curHeaviestWeight) {
				curHeaviestWeight = vWeight;
				curHeaviest = v;
	    }
		}
		return curHeaviest;
	}

	public SortedSet sortSet (Set origSet, Comparator c)
	{
		TreeSet tSet = null;
		if (c != null) {
	    tSet = new TreeSet(c);
		} else {
	    tSet = new TreeSet(); // assume elements have a natural order
		}
		Iterator i = origSet.iterator();
		while (i.hasNext()) {
	    tSet.add(i.next());
		}
		return tSet;  // sorted using comparator
	}

	private double getVWeight (Vertex v, Graph g)
	{
		double posWeight = 0.0;
		Iterator i = g.getEdges(v).iterator();
		while (i.hasNext()) {
	    double w = ((WeightedEdgeImpl)i.next()).getWeight();
	    if (w > 0.0)
				posWeight += w;
		}
		return posWeight;
	}
    
	// XXX OPTIMIZE
	private Vertex selectRandomVertex(Graph g)
	{
		Set vs = g.getVertexSet();
		Object []o = vs.toArray();
		int rval = randGen.nextInt(g.getVerticesCount());
		return (Vertex)o[rval]; // for speed
	}
    
	// set of neighbors with positive edge value AND this vertex
	private Set getNPlus (Graph curGraph, Vertex v)
	{
		LinkedHashSet nplus = new LinkedHashSet();		
		List edges = curGraph.getEdges(v);
		WeightedEdge curEdge = null;
	
		Iterator iter = edges.iterator();
		while (iter.hasNext())
		{
			curEdge = (WeightedEdge)iter.next();
			if (curEdge.getWeight() > 0) {
		    nplus.add(curEdge.getVertexA());
		    nplus.add(curEdge.getVertexB());
			}
		}
		nplus.remove(v);  // ensure v is not in there
		return nplus;
	}

	private Cluster findOptimalCluster (Graph curGraph, Set curCluster, double threshold)
	{
		Vertex curWorst = null;  // the current worst vertex
		boolean stillBads = true;
		double worst = 0.0;  // keeps track of the value of the worst vertex
		Cluster copyCluster = new Cluster();
		Iterator i = curCluster.iterator();
		while (i.hasNext()) {
	    copyCluster.add(i.next());
		}

		while (stillBads) {
	    stillBads = false;
	    Iterator iter = copyCluster.iterator();
	    while (iter.hasNext()) {
				Vertex v = (Vertex)iter.next();
				double w = howGood(curGraph, v, copyCluster);
				if (w < threshold) {
					curWorst = v;
					worst = w;
					stillBads = true;
				}
	    }
	    copyCluster.remove(curWorst);
		}
		return copyCluster;
	}

	/*
	// old one
	private Set findOptimalCluster (Graph curGraph, Set curCluster)
	{
	SortedSet allVertices = sortSet(curGraph.getVertexSet(),comparator);
	System.out.println("AllVertices: ");
	Set c1 = removeDeltaBads (curGraph, curCluster); // actual reduced set
	System.out.println("Reduced set: " + c1);
	Set c2 = addDeltaGoods (allVertices, c1, curGraph); // set to add to reduced one
	System.out.println("goods to add back: " + c2);
	Set c3 = setAdd (c1, c2);
	return c3;
	}
	*/    
	private Set removeDeltaBads (Graph curGraph, Set curCluster)
	{
		boolean allClear = false;
		while (!allClear)
		{
			boolean iterate = true;
			Iterator i = curCluster.iterator();
			Vertex v = null;
			while (iterate && i.hasNext()) {
		    v = (Vertex)i.next();
		    if (!(deltaGood (curGraph, v, curCluster, removeDelta))) {
					removesCnt++;
					System.out.println("Removing " + v + " from " + curCluster + " via" + curGraph.getVertexSet());
					iterate = false; // stop iterating
		    }
			}
			if (iterate && !i.hasNext())
		    allClear = true;
			if (v != null)
		    curCluster.remove(v);
		}
		return curCluster;
	}
    
	private Set addDeltaGoods (Set allVertices, Set curCluster, Graph curGraph)
	{
		Set goods = new LinkedHashSet ();
		Iterator i = allVertices.iterator();
		while (i.hasNext())
		{
			Vertex v = (Vertex)i.next();
			if (deltaGood (curGraph, v, curCluster, addDelta)) {
		    goods.add(v);
			}
		}
		return goods;
	}
    
	// New version of delta good takes into account edge weights
	// XXXX NOTE: we may have recalibrate delta value because of lower values
	/*
    private boolean deltaGood (Graph curGraph, Vertex v, Set cluster, double curDelta)
    {
		System.out.println("Checking d-good on: " + v + " wrt " + cluster + " in " +curGraph.getVertexSet());
		int clusterSize = cluster.size();
		Set nplus = getNPlus (curGraph, v);
		Set i1 = setIntersection (nplus, cluster);
		double sumOfEdgeWeights1 = getSumOfEdgeWeights (i1, v, curGraph);
		System.out.println(" -- intersection: " + i1);
		if ((sumOfEdgeWeights1 + (1 - curDelta)) < ((1 - curDelta) * clusterSize)) {
		System.out.println("false: " + (sumOfEdgeWeights1 + delta));
		return false;
		}
		Set diff = setSubtract (curGraph.getVertexSet(), cluster);
		Set i2 = setIntersection (nplus, diff);
		double sumOfEdgeWeights2 = getSumOfEdgeWeights (i2, v, curGraph);
		if ((sumOfEdgeWeights2 + curDelta) > (curDelta * clusterSize)) {
		System.out.println("False: " + (sumOfEdgeWeights2 + curDelta) + " and " + (curDelta * clusterSize));
		return false;
		}
		else {
		goodsCnt++;
		System.out.println(v + " is d-good wrt " + cluster + " in " + curGraph);
		return true;
		}
    }
	*/
	private double getSumOfEdgeWeights (Set s1, Vertex v, Graph curGraph)
	{
		List allEdges = curGraph.getEdges(v);
		Iterator allIter = allEdges.iterator();
		double curSum = 0.0;

		while (allIter.hasNext()) {
	    WeightedEdgeImpl e = (WeightedEdgeImpl)allIter.next();
	    if (((e.getVertexA() == v) && s1.contains(e.getVertexB())) ||
					((e.getVertexB() == v) && s1.contains(e.getVertexA())))
				curSum += e.getWeight();
		}
		return curSum;
	}


	private boolean deltaGood (Graph curGraph, Vertex v, Set cluster, double curDelta)
	{
		int clusterSize = cluster.size();
		Set nplus = getNPlus (curGraph, v);
		nplus.add(v);
		Set i1 = setIntersection (nplus, cluster);
		System.out.println(" -- intersection: " + i1);
		if (i1.size() < ((1 - curDelta) * clusterSize)) {
	    System.out.println ("BAD 1");
	    return false;
		}
		Set diff = setSubtract (curGraph.getVertexSet(), cluster);
		System.out.println("DIFF: " + diff);
		Set i2 = setIntersection (nplus, diff);
		System.out.println("i2 = " + i2);
		if (i2.size() > (curDelta * clusterSize)) {
	    System.out.println("BAD 2");
	    return false;
		}
		else {
	    goodsCnt++;
	    return true;
		}
	}
    

	private double howGood (Graph curGraph, Vertex v, Set cluster)
	{
		// val = sum of all edge weights within cluster +
		//       inverse sum of all edge weights of GraphVertices - Cluster
		// Idea is to remove the worst vertices *first* in the removeDeltaBads phase
		//   this should provide a better clustering . . . .
		//    -- but chance for big clusters to dominate
		double val = 0.0;
		List edges = curGraph.getEdges(v);
		Iterator i = edges.iterator();
		while (i.hasNext()) {
	    WeightedEdge e = (WeightedEdgeImpl)i.next();
	    if (((e.getVertexA() == v)&& (cluster.contains(e.getVertexB()))) ||
					((e.getVertexB() == v)&& (cluster.contains(e.getVertexA()))))
				val += e.getWeight();
	    else
				val -= e.getWeight();
		}
		return val;
	}
    
	private Set setAdd (Set s1, Set s2)
	{
		LinkedHashSet s = new LinkedHashSet ();
		Iterator i1 = s1.iterator();
		Iterator i2 = s2.iterator();
	
		while (i1.hasNext())
		{
			s.add(i1.next());
		}
		while (i2.hasNext())
		{
			s.add(i2.next());
		}
		return s;
	}
    
	private Set setIntersection (Set s1, Set s2)
	{
		LinkedHashSet s = new LinkedHashSet ();
		Iterator i1 = s1.iterator();
	
		while (i1.hasNext())
		{
			Object el = i1.next();
			if (s2.contains(el))
		    s.add(el);
		}
		return s;
	}
    
	private Set setSubtract (Set s1, Set s2)
	{
		LinkedHashSet s = new LinkedHashSet ();
		Iterator i1 = s1.iterator();
	
		while (i1.hasNext())
		{
			Object el  = i1.next();
			if (!s2.contains(el))
		    s.add(el);
		}
		return s;
	}
    
	public static MappedGraph generateRandomMappedGraph (int size, double posRand)
	{
		int i;
		MappedGraph g = new MappedGraph();
		Vector vertices = new Vector(); // gross
		double val = 0.0;
		Random statRandGen = new Random();
		// add vertices first
		for (i = 0; i < size; i++)
		{
			Integer v = new Integer (i); 
			try {
		    g.addVertexMap (v);
			} catch (Exception e) {e.printStackTrace();}
			vertices.add(v);
		}
	
		for (i = 0; i < size; i++) {
	    for (int j = 0; j < i; j++)
			{
		    if (statRandGen.nextFloat() < posRand)
					val = 1.0;
		    else
					val = -1.0;
		    try {
					g.addEdgeMap ((Object)vertices.elementAt(j),
												(Object)vertices.elementAt(i), val);
		    } catch (Exception f) {f.printStackTrace();}
			}
		}
		return g;
	}

	public static WeightedGraph generateRandomWeightedGraph (int size, double posRand)
	{
		int i;
		WeightedGraph g = new WeightedGraphImpl();
		Vector vertices = new Vector(); // gross
		double val = 0.0;
		Random statRandGen = new Random();
		// add vertices first
		for (i = 0; i < size; i++)
		{
			Vertex v = new VertexImpl (new Integer (i)); 
			try {
		    g.add (v);
			} catch (Exception e) {e.printStackTrace();}
			vertices.add(v);
		}
	
		for (i = 0; i < size; i++) {
	    for (int j = 0; j < i; j++)
			{
		    if (statRandGen.nextFloat() < posRand)
					val = 1.0;
		    else
					val = -1.0;
		    try {
					g.addEdge ((Vertex)vertices.elementAt(j),
										 (Vertex)vertices.elementAt(i), val);
		    } catch (Exception f) {f.printStackTrace();}
			}
		}
		return g;
	}
    
    
	public static void main (String[] args)
	{

		int SIZE = 5;

		MappedGraph tg =
	    new MappedGraph();
	
		Set s1 = new LinkedHashSet();
		Set s2 = new LinkedHashSet();
		Set s3 = new LinkedHashSet();
		s1.add(new String ("foo"));
		s2.add(new String ("bar"));
		s3.add(new String ("baz"));

		try {
	    tg.addEdgeMap (s1,s2, -0.25);
	    tg.addEdgeMap (s2,s3, 0.5);
	    tg.addEdgeMap (s1,s3, 0.5);
	    //tg.addEdge (v1,v4, -0.5);
	    //tg.addEdge (v2,v4, -0.5);
	    //tg.addEdge (v3,v4, -0.1);
		} catch (Exception e) {e.printStackTrace();}
		System.out.println(tg.toString());
		MinimizeDisagreementsClustering cl1 = new
																					MinimizeDisagreementsClustering(tg,(double)1/44);
		Set clusters1 = cl1.getClustering(null);
		System.out.println("clusters: " + clusters1);
	
		MappedGraph g =
	    MinimizeDisagreementsClustering.generateRandomMappedGraph (10, 0.1);
	
		System.out.println (g.toString());

		Set clusterSets[] = new Set[SIZE];
	
		for (int i=0; i < SIZE; i++) {
	    MinimizeDisagreementsClustering cl = new
																					 MinimizeDisagreementsClustering(g,(double)1/44);
	    System.out.println(cl);
	    Set clusters = cl.getClustering(null, -5.0); 
	    System.out.println("There are " + clusters.size() + " clusters in this graph.\n");
	    System.out.println("Clusters: " + clusters);
	    clusterSets[i] = clusters;
		}

		for (int i=0; i < SIZE; i++) {
	    for (int j=i; j < SIZE; j++) {
				ClusterEvaluate eval = new ClusterEvaluate (clusterSets[i], clusterSets[j]);
				eval.evaluate();
				System.out.println("Score " + i + "-" + j + " " + eval.getF1());
	    }
		}

		//System.out.println("There are " + clusters.size() + " clusters in this graph.");
	}
}
