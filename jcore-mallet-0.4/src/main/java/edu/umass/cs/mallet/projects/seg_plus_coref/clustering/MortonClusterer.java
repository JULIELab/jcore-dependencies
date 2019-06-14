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

import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.Mention;
import salvo.jesus.graph.Vertex;
import salvo.jesus.graph.WeightedEdge;
import salvo.jesus.graph.WeightedGraph;

import java.util.Iterator;
import java.util.List;

public class MortonClusterer
{

    MappedGraph mGraph;
    WeightedGraph graph;

    public MortonClusterer ()
    {
    }

    public MortonClusterer (MappedGraph graph)
    {
	this.mGraph = graph;
	this.graph = graph.getGraph();
    }

    public WeightedGraph getGraph ()
    {
	return graph;
    }


    // this should iterate through 
    // these objects are the objects the vertices refer to
    public Clustering getClustering ()
    {
	List vertices = mGraph.getOrderedVertices();
	MortonClustering clustering = new MortonClustering();
	System.out.println("+++++++++++++++++++++++++++++++++++++++++");
	System.out.println("MORTON CLUSTERING: DEBUG" );
        for (int i=0; i < vertices.size(); i++) {
	    Vertex refV = (Vertex)vertices.get(i);
	    System.out.println("Considering antecedents for: " + refV + " "
			       + mGraph.getObjectFromVertex(refV));
	    List edges = graph.getEdges(refV);
	    double bestM = 0.0;
	    Vertex bestAntecedent = null;
	    for (int j=i-1; j >= 0; j--) {
		Iterator eIter = edges.iterator();
		while (eIter.hasNext()) {
		    WeightedEdge e = (WeightedEdge)eIter.next();
		    Vertex curVertex = null; 

		    if (e.getVertexA() == vertices.get(j))
			curVertex = e.getVertexA();
		    else if (e.getVertexB() == vertices.get(j))
			curVertex = e.getVertexB();
		    System.out.println("  -- " + curVertex + " " + 
				       mGraph.getObjectFromVertex(curVertex));
		    if (curVertex != null) {
			if (e.getWeight() > bestM) {
			    bestM = e.getWeight();
			    bestAntecedent = curVertex;
			}
		    }
		}
	    }
	    if (bestAntecedent != null) {
		Mention ref = (Mention)mGraph.getObjectFromVertex(refV);
		Mention ant = (Mention)mGraph.getObjectFromVertex(bestAntecedent);
		if ((ref != null) && (ant != null)) {
		    System.out.println(ref.getString()
				       + " -- " + 
				       ref.getString() 
				       + " with " + bestM);
		}
		clustering.addToClustering(mGraph.getObjectFromVertex(refV),
					   mGraph.getObjectFromVertex(bestAntecedent));
	    } else
		clustering.addToClustering(mGraph.getObjectFromVertex(refV));
	}
	return clustering;
    }
}
