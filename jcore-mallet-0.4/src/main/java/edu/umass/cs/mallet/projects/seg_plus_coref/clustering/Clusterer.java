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

import edu.umass.cs.mallet.projects.seg_plus_coref.graphs.MinimizeDisagreementsClustering;
import salvo.jesus.graph.WeightedGraph;

public class Clusterer 
{

    MappedGraph mGraph;
    WeightedGraph graph;

    public Clusterer (MappedGraph graph)
    {
	mGraph = graph;
	this.graph = graph.getGraph();
    }

    public Clusterer ()
    {
    }

    public void setGraph (MappedGraph graph)
    {
	this.mGraph = graph;
	this.graph = graph.getGraph();
    }

    public MappedGraph getMappedGraph ()
    {
	return mGraph;
    }

    public WeightedGraph getGraph ()
    {
	return graph;
    }
    /*
    public Clustering getClustering (MappedGraph graph)
    {
	setGraph(graph);
	return getClustering();
	}*/

    public Clustering getClusteringGreedily () 
    {
	MinimizeDisagreementsClustering minClustering = null;
	// clustering algorithm here
	if (mGraph != null) {
	    minClustering = new MinimizeDisagreementsClustering(mGraph,(double)1/44);
	}
	else {
	    return null;
	}
	Clustering cl = minClustering.getClusteringGreedily();
	System.out.println("Number of clusters: " + cl.size());
	return (Clustering)new GraphClustering (graph, cl);
    }

    public Clustering getClustering (Clustering prevClustering)
    {
	MinimizeDisagreementsClustering minClustering = null;
	// clustering algorithm here
	if (mGraph != null) {
	    minClustering = new MinimizeDisagreementsClustering(mGraph,(double)1/44);
	}
	else {
	    return null;
	}
	Clustering cl = null;
	if (prevClustering != null)
	    cl = minClustering.getClustering(prevClustering.getSelectVertices());
	else 
	    cl = minClustering.getClustering(null);
	return (Clustering)new GraphClustering (graph, cl);
    }

    public Clustering getClustering ()  // might need some parameters here ...
    {
	return getClustering(null);
    }
}
