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

import edu.umass.cs.mallet.base.classify.Classification;
import edu.umass.cs.mallet.base.classify.MaxEnt;
import edu.umass.cs.mallet.base.classify.MaxEntTrainer;
import edu.umass.cs.mallet.base.classify.Trial;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.Labeling;
import salvo.jesus.graph.*;

import java.util.*;


/*
An object of this class will allow an InstanceList as well as a List of
mentions to be passed to a method that will return a list of lists
representing the partitioning of the mentions into clusters/equiv. classes.
There should be exactly M choose 2 instances in the InstanceList where M is
the size of the List of mentions (assuming a complete graph).
*/

public class CorefCluster {
    private final double NegativeInfinite = -1000000000;
    MaxEnt meClassifier = null;
	double threshold = 0.0;
    private static int falsePositives = 0;
    private static int falseNegatives = 0;

	public CorefCluster () {}
	
    public CorefCluster (double threshold) {
			this.threshold = threshold;
		}

    public CorefCluster (double threshold, MaxEnt classifier) {
			this.threshold = threshold;
			this.meClassifier = classifier;
    }

	public void setThreshold (double t) {
		this.threshold = t;
	}

    /*
    Initialize a list of lists where each inner list is a list with a single element.
    */

	public void train (InstanceList ilist) {
		// just to plain MaxEnt training for now
		MaxEnt me = (MaxEnt)(new MaxEntTrainer().train (ilist, null, null, null, null));
		Trial t = new Trial(me, ilist);
		System.out.println("CorefCluster -> Training F1 on \"yes\" is: " + t.labelF1("yes"));
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
            addVerticesToGraph(graph, mentions, alreadyAddedVertices);
			for (int i=0; i<ilist.size(); i++) {
				constructEdgesUsingTrainedClusterer(graph, ilist.getInstance(i),
																						alreadyAddedVertices, meClassifier);
			}
			System.out.println("False positives: " + falsePositives);
			System.out.println("False negatives: " + falseNegatives);
			//addVerticesToGraph(graph, mentions, alreadyAddedVertices);  // mhay 1/30/04
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
                alreadyAddedVertices.put(o,v);   // mhay 1/30/04
				try {
					graph.add(v); // add the vertex
				} catch (Exception e) {e.printStackTrace();}
			}
		}
	}

	/*
		This version is plain old single-link clustering and doesn't use the Graph
		infrastructure at all.
	 */
	public Collection absoluteCluster (InstanceList ilist, List mentions) {

		List clustering = new ArrayList();

		List mCopy = new ArrayList();
		
		for (int i=0; i < mentions.size(); i++) {
			mCopy.add(mentions.get(i));
		}
		
		for (int i=0; i < ilist.size(); i++) {
			Instance inst = (Instance)ilist.getInstance(i);
			Classification classification = (Classification)meClassifier.classify(inst);
			Labeling labeling = classification.getLabeling();
			NodePair pair = (NodePair)inst.getSource();
			Citation c1 = (Citation)pair.getObject1();
			Citation c2 = (Citation)pair.getObject2();
			if (labeling.getBestLabel().toString().equals("yes") && labeling.getBestValue() > 0.8) {
				boolean newCluster = true;
				for (int j=0; j<clustering.size(); j++) {
					List cl = (List)clustering.get(j);
					if (cl.contains(c1)) {
						cl.add(c2);
						newCluster = false;
						break;
					} else if (cl.contains(c2)) {
						cl.add(c1);
						newCluster = false;
						break;
					}
				}
				if (newCluster) {
					List nc = new ArrayList();
					nc.add(c1);
					nc.add(c2);
					clustering.add(nc);
				} 
				mCopy.remove(c1);
				mCopy.remove(c2);
			}
		}
		// add in singletons remaining
		for (int i=0; i < mCopy.size(); i++) {
			List newone = new ArrayList();
			newone.add((Citation)mCopy.get(i));
			clustering.add(newone);
		}
		return (Collection)clustering;

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
			if (bestEdgeVal < threshold)
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

		Object o1 = v1.getObject();
		Object o2 = v2.getObject();
		List l1;

        //System.out.println("Merging o1 = " + toString(o1) + " and o2 = " + toString(o2));

        if ((o1 instanceof List) && (o2 instanceof List)) {
            /* Create a new vertex that merges the list of objects of the old vertices */
            l1 = (List) o1;
            l1.addAll((List) o2); // concatenate lists
        } else if (o1 instanceof List) {
            l1 = (List) o1;
            l1.add(o2);
        } else if (o2 instanceof List) {
            l1 = (List) o2;
            l1.add(o1);
        } else {
            l1 = new ArrayList();
            l1.add(o1);
            l1.add(o2);
        }
//		System.out.println("l1=" + l1);

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
				double weight = (w1 > w2) ? w1 : w2;   // max
				//double weight = (w1 + w2)/2;   // avg
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
            System.err.println("Expected to have a vertex for node1" + node1.toString());
			v1 = new VertexImpl(node1);
			alreadyAdded.put(node1,v1);
		}
		if (v2 == null) {
            System.err.println("Expected to have a vertex for node1" + node2.toString());
			v2 = new VertexImpl(node2);
			alreadyAdded.put(node2,v2);
		}
		
		// double  edgeVal = computeScore(classifier, instPair);
		double  edgeVal = computeScore_NBest(classifier, instPair);

		try {
			if (node1 != null && node2 != null) {
				graph.addEdge (v1, v2, edgeVal);
			}
		} catch (Exception e) {e.printStackTrace();}
	}


    /**
     * This method assumes that the instance is a pair of Citation and that the
     * Citation objects have an N-best list for the n-best segmentations. The
     * edge value returned is simply the MAX score of any pair of citations.
     *
     * @param classifier
     * @param origInstPair
     * @return
     */
    private static double computeScore_NBest(MaxEnt classifier, Instance origInstPair) {
        NodePair origNodePair = (NodePair)origInstPair.getSource();
        Citation citation1 = (Citation)origNodePair.getObject1();
        Citation citation2 = (Citation)origNodePair.getObject2();
        Collection nBest1 = citation1.getNBest();
        Collection nBest2 = citation2.getNBest();

        String label;
        if (origNodePair.getIdRel())
            label = "yes";
        else
            label = "no";
        Object name = origInstPair.getName();
        Object source = origInstPair.getSource();
        Pipe pipe = origInstPair.getPipe();

        if (nBest1 == null || nBest2 == null) {
            //System.err.println("Did not find n-best, using original");
            //Instance instPair = new Instance (origNodePair, label, name, source, pipe);
            double score = computeScore(classifier, origInstPair);
						/*						
            int i1 = citation1.getIndex();
            int i2 = citation2.getIndex();

            if (score < 0.0 && label == "yes") {
                System.out.println(i1 + " " + i2 + " " + score + " " + label);
                falseNegatives++;
            } else if (score > 0.0 && label == "no") {
                System.out.println(i1 + " " + i2 + " " + score + " " + label);
                falsePositives++;
								}*/
            return score;
        }

        List scores = new ArrayList();

        int i = 0, j = 0;
        // make a new instance which is all pairs of two sets of n citations
        for (Iterator iterator = nBest1.iterator(); iterator.hasNext();) {
            j=0;
            Citation nbest_citation1 = (Citation) iterator.next();
            for (Iterator iterator2 = nBest2.iterator(); iterator2.hasNext();) {
                Citation nbest_citation2 = (Citation) iterator2.next();
                NodePair nodePair = new NodePair(nbest_citation1, nbest_citation2,
                        origNodePair.getIdRel());
                Instance instPair = new Instance (nodePair, label, name, null, pipe);
//                System.out.println(i + ", " + j);
//                System.out.println(nbest_citation1.rawstring);
//                System.out.println(nbest_citation2.rawstring);
                double score = computeScore(classifier, instPair);
//                System.out.println(score);
//                System.out.println(i + ", " + j + ": " + score);
                scores.add(new Double(score));
                j++;
            }
            i++;
        }
        return ((Double)Collections.max(scores)).doubleValue();
    }

    private static double computeScore(MaxEnt classifier, Instance instPair) {

        // Include the feature weights according to each label
        Classification classification = (Classification)classifier.classify(instPair);
        Labeling labeling = classification.getLabeling();

        /** NOTE:  THIS ASSUMES THERE ARE JUST TWO LABELS - IE CLASSIFIER IS
         * BINARY */
        double score = 0.0;
        if (labeling.labelAtLocation(0).toString().equals("no")) {
            score =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
        } else {
            score =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
        }
        return score;
    }

}
