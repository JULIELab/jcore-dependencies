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

public class GraphClustering extends Clustering
{
    WeightedGraph graph;
    HashMap       verticesToClusters; // maps OBJECTS encapsulated in vertex into clusters (sets)

    public GraphClustering (WeightedGraph graph, Clustering clusters)
    {
	super();
	addClusters(clusters);
	this.graph = graph;
	buildHash();
    }

    private void addClusters (Clustering clusters)
    {
	Iterator i = clusters.iterator();
	while (i.hasNext()) {
	    add(i.next());
	}
    }

    private void buildHash()
    {
	verticesToClusters = new HashMap();
	Iterator vIterator = graph.getVerticesIterator();
	while (vIterator.hasNext()) {
	    VertexImpl v = (VertexImpl)vIterator.next();
	    Iterator clIterator = this.iterator();
	    while (clIterator.hasNext()) {
		Set cluster = (Set)clIterator.next();
		if (cluster.contains(v)) {
		    verticesToClusters.put(v.getObject(),cluster);
		}
	    }
	}
    }

    public void printClustering ()
    {
	System.out.println(this);
    }

    public double evaluateClustering ()
    {
	Set edges = graph.getEdgeSet();
	Iterator iter = edges.iterator();
	double value = 0.0;
	
	while (iter.hasNext()) {
	    WeightedEdge e = (WeightedEdge)iter.next();
	    if (inSameCluster (((VertexImpl)e.getVertexA()).getObject(), ((VertexImpl)e.getVertexB()).getObject()))
		value += e.getWeight();
	    else
		value += -e.getWeight();
	}
	return value;
    }

    public int inSameCluster (Mention v1, Mention v2)
    {
	Object c1 = verticesToClusters.get(v1);
	Object c2 = verticesToClusters.get(v2);
	if (c1 == c2)
	    return 1;
	else
	    return 0;  // return integer for array access
    }
}
