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

package edu.umass.cs.mallet.projects.seg_plus_coref.clustering;

import salvo.jesus.graph.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.graphs.*;
import edu.umass.cs.mallet.base.types.Instance;
import java.util.*;

// This class wraps a WeightedGraph and maintains an index from actual objects
// to integer objects
public class MappedGraph
{

	List          orderedVertices;
	WeightedGraph graph;
	HashMap       objectsToVertices;
	HashMap       verticesToObjects;
	int           curId;

	public MappedGraph (WeightedGraph g)
	{
		graph = g;
		objectsToVertices = new HashMap();
		verticesToObjects = new HashMap();
		curId = 0;
		orderedVertices = new ArrayList();
	}

	public MappedGraph ()
	{
		graph = new WeightedGraphImpl ();
		objectsToVertices = new HashMap();
		verticesToObjects = new HashMap();
		curId = 0;
		orderedVertices = new ArrayList();
	}

	public List getOrderedVertices ()
	{
		return orderedVertices;
	}

	public void printMap()
	{
		System.out.println(objectsToVertices);
	}

	public WeightedGraph getGraph()
	{
		return graph;
	}

	public Object getObjectFromVertex (Vertex v)
	{
		return verticesToObjects.get (v);
	}

	public Vertex getVertexFromObject (Object o)
	{
		return (Vertex)objectsToVertices.get (o);
	}

	public void addVertexMap (Object o1)
	{
		Vertex v1 = (Vertex)objectsToVertices.get(o1);
		if (v1 == null) {
	    curId++;
	    v1 = new VertexImpl ( new Integer (curId));
	    objectsToVertices.put (o1, v1);
	    verticesToObjects.put (v1, o1);
	    if (!orderedVertices.contains(v1))
				orderedVertices.add(v1);
	    try {
				graph.add(v1);
	    } catch (Exception e) {e.printStackTrace();}
		}
	}

	public void addEdgeMap (Object o1, Object o2, double weight)
	{
		Vertex v1 = (Vertex)objectsToVertices.get(o1);
		Vertex v2 = (Vertex)objectsToVertices.get(o2);

		if (v1 == null) {
	    curId++;
	    v1 = new VertexImpl ( new Integer (curId));
	    orderedVertices.add(v1);
	    objectsToVertices.put (o1, v1);
	    verticesToObjects.put (v1, o1);
		}
		if (v2 == null) {
	    curId++;
	    v2 = new VertexImpl ( new Integer (curId));
	    orderedVertices.add(v2);
	    objectsToVertices.put (o2, v2);
	    verticesToObjects.put (v2, o2); 
		}
		try {
	    graph.addEdge (v1, v2, weight);
		} catch (Exception e) {e.printStackTrace();}
	}
}
