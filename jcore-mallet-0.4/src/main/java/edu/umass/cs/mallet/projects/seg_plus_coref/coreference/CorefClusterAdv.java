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
import salvo.jesus.graph.VertexImpl;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;


/*
An object of this class will allow an InstanceList as well as a List of
mentions to be passed to a method that will return a list of lists
representing the partitioning of the mentions into clusters/equiv. classes.
There should be exactly M choose 2 instances in the InstanceList where M is
the size of the List of mentions (assuming a complete graph).
*/

public class CorefClusterAdv {
	double MAX_ITERS = 30;  // max number of typical cluster
	// iterations

	private static boolean export_graph = true;
	private static int falsePositives = 0;
	private static int falseNegatives = 0;

	Collection keyPartitioning = null; // key partitioning - optional for
	// evaluation against test clusters
	// within search procedure

	double MAX_REDUCTIONS = 10;  // max number of reductions
	// allowed

	Pipe pipe;

	boolean trueNumStop = false;  // if true, only stop once true number of
	// clusters achieved

	boolean useOptimal = false; // if true, cheat 

	boolean useNBestInference = false; // true if we should use the Greedy
	// N-best method that Michael developed

	boolean fullPartition = false;
	boolean confidenceWeightedScores = false;

	int rBeamSize = 10;
	
	final double NegativeInfinite = -1000000000;
	MaxEnt meClassifier = null;
	Matrix2 sgdParameters = null;
	int numSGDFeatures = 0;
	double threshold = 0.0;
	TreeModel treeModel = null;
	WeightedGraph wgraph = null;  // pointer to graph accesible from outside

	public CorefClusterAdv () {}

	public CorefClusterAdv (TreeModel tm) {
		this.treeModel = tm;
	}

	public CorefClusterAdv (Pipe p) {
		this.pipe = p;
	}

	public CorefClusterAdv (Pipe p, TreeModel tm) {
		this.pipe = p;
		this.treeModel = tm;
	}
	
	public CorefClusterAdv (double threshold) {
		this.threshold = threshold;
	}

	public CorefClusterAdv (double threshold, MaxEnt classifier, Pipe p) {
		this.threshold = threshold;
		this.meClassifier = classifier;
		this.pipe = p;
	}

	// version that has a tree model
	public CorefClusterAdv (double threshold, MaxEnt classifier, TreeModel tm, Pipe p) {
		this.threshold = threshold;
		this.meClassifier = classifier;
		this.treeModel = tm;
		this.pipe = p;
	}

	// in this case we don't have a MaxEnt classifier, but a set of parameters
	// learned via Stochastic Gradient Descent (or are being learned)
	public CorefClusterAdv (double threshold, Matrix2 sgdParameters, int numSGDFeatures) {
		this.threshold = threshold;
		this.sgdParameters = sgdParameters;
		this.numSGDFeatures = numSGDFeatures;
	}

	public void setConfWeightedScores (boolean b) {
		this.confidenceWeightedScores = b;
	}

	public void setRBeamSize (int s) {
		this.rBeamSize = s;
	}

	public void setOptimality (boolean b) {
		this.useOptimal = b;
	}

	public void setNBestInference (boolean b) {
		this.useNBestInference = b;
	}

	public void setTrueNumStop (boolean b) {
		this.trueNumStop = b;
	}

	public void setSearchParams (int iters, int reductions) {
		MAX_ITERS = iters;
		MAX_REDUCTIONS = reductions;
	}

	public void setThreshold (double t) {
		this.threshold = t;
	}

	public void setKeyPartitioning (Collection keyP) {
		this.keyPartitioning = keyP;
	}

	public void setFullPartition (boolean f) {
		this.fullPartition = f;
	}
		
	/*
		Initialize a list of lists where each inner list is a list with a single element.
	*/

	public void loadME (String file) {
		MaxEnt me = null;
		try {
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream( file ));
	    me = (MaxEnt)ois.readObject();
	    ois.close();
		} catch (Exception e) {e.printStackTrace();}
		this.meClassifier = me;
	}

	public void train (InstanceList ilist) {
		this.meClassifier = trainClassifier (ilist);
	}

	public MaxEnt trainClassifier (InstanceList ilist) {
		// just to plain MaxEnt training for now
		System.out.println("Training NOW: ");
		MaxEnt me = (MaxEnt)(new MaxEntTrainer().train (ilist, null, null, null, null));
		Alphabet alpha = ilist.getDataAlphabet();
		alpha.stopGrowth(); // hack to prevent alphabet from growing
		Trial t = new Trial(me, ilist);
		System.out.println("CorefClusterAdv -> Training F1 on \"yes\" is: " + t.labelF1("yes"));
		//me.write(new File("/tmp/MaxEnt_Output"));
		return me;
	}

	public void testClassifier (InstanceList tlist) {
		testClassifier (tlist, meClassifier);
	}

	public void testClassifier (InstanceList tlist, MaxEnt classifier) {
		Trial t = new Trial(classifier, tlist);
		System.out.println("test accuracy: " + t.labelF1("yes"));
		for (Iterator it = tlist.iterator(); it.hasNext();) {
	    Instance inst = (Instance)it.next();
	    Classification classification = (Classification)classifier.classify(inst);
	    Labeling l = classification.getLabeling();
	    //System.out.println("Best label: " + l.getBestLabel().toString() + " "
	    //								 + inst.getTarget().toString());
			
	    if (!l.getBestLabel().toString().equals(inst.getTarget().toString())) {
				Citation c1 = (Citation)((NodePair)inst.getSource()).getObject1();
				Citation c2 = (Citation)((NodePair)inst.getSource()).getObject2();
				if (inst.getLabeling().getBestLabel().toString().equals("yes")) {
					System.out.println("FN: " + c1.print() + " " + c1.getString()
														 + "\n   " + c2.print() + " " + c2.getString());
					System.out.println("Citation venue: " + c1.getField("venue") + " --> "
														 + c2.getField("venue"));
				}
				else if (inst.getLabeling().getBestLabel().toString().equals("no")) {
					System.out.println("FP: " + c1.print() + " " + c1.getString()
														 + "\n   " + c2.print() + " " + c2.getString());
					System.out.println("Citation venue: " + c1.getField("venue") + " --> "
														 + c2.getField("venue"));
				}
				System.out.println(printParamDetails((FeatureVector)inst.getData(),
																						 classification, classifier));
	    }
		}
	}

	public String printParamDetails (FeatureVector vec, Classification classification,
																	 MaxEnt classifier)
	{
		Labeling l = classification.getLabeling();

		Alphabet dictionary = vec.getAlphabet();
		int [] indices = vec.getIndices();
		double [] values = vec.getValues();
		double [] params = classifier.getParameters();

		int paramsLength = params.length;
		int indicesLength = indices.length;
		int numParams = paramsLength/2;
		//assert (paramsLength == 2*indicesLength);
		//Thread.currentThread().dumpStack();
		StringBuffer sb = new StringBuffer ();
		int valuesLength = vec.numLocations();
		for (int i = 0; i < valuesLength; i++) {
	    if (dictionary == null)
				sb.append ("["+i+"]");
	    else
				sb.append (dictionary.lookupObject(indices == null ? i : indices[i]).toString());
	    sb.append ("(" + indices[i] +")");
	    //sb.append ("(" + i +")");
	    sb.append ("=");
	    sb.append (values[i]);
	    if (l.labelAtLocation(0).toString().equals("no")) 
				sb.append ("  (" + (params[indices[i]+numParams]-params[indices[i]]) + ")");
	    else
				sb.append ("  (" + (params[indices[i]]-params[indices[i]+numParams]) + ")");
	    sb.append ("\n");
		}
		return sb.toString();
	}

	public void printParams (MaxEnt me) {
		double[] parameters = me.getParameters();
		int numFeatures = parameters.length/2;
		Matrix2 matrix2 = new Matrix2(parameters,2,numFeatures);
		for (int i=0; i<2; i++) {
	    System.out.print(i + ": ");
	    for (int j=0; j<numFeatures; j++) {
				System.out.print(j + "=" + matrix2.value(new int[] {i,j}) + " ");
	    }
	    System.out.println();
		}
	}
		

	public MaxEnt getClassifier() {return meClassifier;}

	public Collection clusterMentions (InstanceList ilist, List mentions) {
		return clusterMentions (ilist, mentions, -1, true);
	}

	/* performance time method */
	public Collection clusterMentions (InstanceList ilist, List mentions, int optimalNBest,
																		 boolean stochastic) {
		if (meClassifier != null || sgdParameters != null) {
	    if (optimalNBest > 0) {
				System.out.println("Computing \"optimal\" edge weights using N-best lists to " + optimalNBest);
				WeightedGraph g = constructOptimalEdgesUsingNBest (mentions, optimalNBest);
				wgraph = g;
				System.out.println("!!!! Constructed Graph !!!!!");
				//return null;
				if (stochastic)
					return partitionGraph (g);
				else
					return absoluteCluster (ilist, mentions);
				//return typicalClusterPartition(g);
	    } else {
				if (fullPartition) {
					wgraph = createGraph(ilist, mentions);
					return partitionGraph(wgraph);
				}
				else if (stochastic) {
					return typicalClusterAdv (ilist, mentions);

					//return absoluteCluster (ilist, mentions);
					//wgraph = createGraph(ilist, mentions);
					//return partitionGraph (wgraph);
				}
				else {
					wgraph = createGraph(ilist, mentions);
					//return absoluteCluster (ilist, mentions);
					return typicalClusterPartition (wgraph);
				}
	    }

		}
		else { return null; }
	}

	public WeightedGraph createGraph (InstanceList ilist, List mentions) {
		return createGraph (ilist, mentions, new WeightedGraphImpl());
	}
	
	public WeightedGraph createGraph (InstanceList ilist, List mentions, WeightedGraph graph) {
		return createGraph (ilist, mentions, graph, this.meClassifier);	
	}

	public WeightedGraph createGraph (InstanceList ilist, List mentions,
																		WeightedGraph graph, MaxEnt classifier) {
		HashMap alreadyAddedVertices = new HashMap(); // keep track of
		for (int i=0; i < ilist.size(); i++) {
	    constructEdgesUsingTrainedClusterer(graph, ilist.getInstance(i),
																					alreadyAddedVertices, null, classifier);
		}
		System.out.println("Finished building graph");
		addVerticesToGraph(graph, mentions, alreadyAddedVertices);
		return graph;
	}

	// this just writes out the edges of a graph with one edge per line
	public void exportGraph (String file) {
		Set edges = wgraph.getEdgeSet();
		try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(file));
	    for (Iterator it = edges.iterator(); it.hasNext(); ) {
				WeightedEdge e = (WeightedEdge)it.next();
				VertexImpl v1 = (VertexImpl)e.getVertexA();
				VertexImpl v2 = (VertexImpl)e.getVertexB();
				Citation c1 = (Citation)((List)v1.getObject()).get(0);
				Citation c2 = (Citation)((List)v2.getObject()).get(0);
				out.write(c1.print() + " " + c2.print() + " " + e.getWeight() + "\n");
	    }
	    out.close();
		} catch (IOException e){e.printStackTrace();};
	}

	//shallow copy of graph - just need to keep the edges
	public WeightedGraph copyGraph (WeightedGraph graph) {
		WeightedGraph copy = new WeightedGraphImpl();

		Set edgeSet = graph.getEdgeSet();
		Iterator i1 = edgeSet.iterator();
		HashMap map = new HashMap();

		while (i1.hasNext()) {
	    WeightedEdge e1 = (WeightedEdge)i1.next();
	    VertexImpl v1 = (VertexImpl)e1.getVertexA();
	    VertexImpl v2 = (VertexImpl)e1.getVertexB();
	    VertexImpl n1 = (VertexImpl)map.get(v1);
	    VertexImpl n2 = (VertexImpl)map.get(v2);
	    if (n1 == null) {
				Object o1 = v1.getObject();
				ArrayList l1 = new ArrayList();
				if (o1 instanceof List) 
					for (int i=0; i < ((List)o1).size(); i++)
						l1.add(((List)o1).get(i));
				else
					l1.add(o1);
				n1 = new VertexImpl(l1);
				map.put(v1,n1);
	    }
	    if (n2 == null) {
				Object o2 = v2.getObject();
				ArrayList l2 = new ArrayList();
				if (o2 instanceof List) 
					for (int i=0; i < ((List)o2).size(); i++)
						l2.add(((List)o2).get(i));
				else
					l2.add(o2);
				n2 = new VertexImpl(o2);
				map.put(v2,n2);
	    }
	    WeightedEdge ne = new WeightedEdgeImpl(n1, n2, e1.getWeight());
	    try {
				copy.addEdge(ne);
	    } catch (Exception e) {e.printStackTrace();}
			
		}
		return copy;
	}
	
	public void addVerticesToGraph(WeightedGraph graph,
																 List mentions, HashMap alreadyAddedVertices) {
		for (int i=0; i < mentions.size(); i++) {
	    Object o = mentions.get(i);
	    if (alreadyAddedVertices.get(o) == null) { // add only if it hasn't been
				// added
				List l = new ArrayList();
				l.add(o);
				VertexImpl v = new VertexImpl(l);
				try {
					graph.add(v); // add the vertex
				} catch (Exception e) {e.printStackTrace();}
	    }
		}
	}

	public WeightedEdge chooseEdge3 (List edges, double minVal, double total, java.util.Random rand) {
		if (edges.size() > 0) {
	    return (WeightedEdge)edges.get(0);
		} else return null;
	}

	// simpler more heuristic-based approach
	public WeightedEdge chooseEdge2 (List edges, double minVal, double total, java.util.Random rand) {

		//return (WeightedEdge)edges.first();

		if (edges.size() < 1)
	    return null;
		
		int x = rand.nextInt(10);
		if (x > edges.size())
	    x = edges.size();
		WeightedEdge e = null;
		Iterator i1 = edges.iterator();
		int i=0;
		while (i1.hasNext() && i < x) {
	    e = (WeightedEdge)i1.next();
	    i++;
		}
		if (e != null)
	    return e;
		else 
	    return (WeightedEdge)edges.get(0);
	}

	/*
		Algorithm: Sort edges by magnitude.  Scale so they're all
		positive. Choose a random number between 0 and the sum of all the
		magnitudes.
		Select an edge in this fashion.
		Merge the two vertices and 
	*/
	private WeightedEdge chooseEdge (List edges, double minVal, double total, java.util.Random rand) {

		
		double x = rand.nextDouble() * total;  // 0 < x < total
		double cur = 0.0;
		Iterator i1 = edges.iterator();
		while (i1.hasNext()) {
	    WeightedEdge e = (WeightedEdge)i1.next();
	    cur += (e.getWeight()-minVal); // SUBTRACT minVal
	    if (cur > x) {
				return e;
	    }
		}
		// this shouldn't really happend unless there is some kind if numerical
		// issues - default to the first edge
		return (WeightedEdge)edges.get(0);
	}

	private PseudoEdge choosePseudoEdge (List edges, java.util.Random rand) {
		if (edges.size() == 0)
	    return null;

		double factor = Math.ceil(Math.log(edges.size()))*20;
		int x = rand.nextInt(10);
		if (x > edges.size())
	    x = edges.size();
		PseudoEdge e = null;
		Iterator i1 = edges.iterator();
		int i=0;
		while (i1.hasNext() && i < x) {
	    e = (PseudoEdge)i1.next();
	    i++;
		}
		if (e != null)
	    return e;
		else 
	    return (PseudoEdge)edges.get(0);
	}

	public double evaluatePartitioningExternal (InstanceList ilist, List mentions, Collection collection) {

		return evaluatePartitioningExternal (ilist, mentions, collection, -1);
		
	}

	public double evaluatePartitioningExternal (InstanceList ilist, List mentions, Collection collection,
																							int nBestList) {
		if (nBestList > 0 ) {
	    return evaluatePartitioning (collection, wgraph);

		}
		else
	    return evaluatePartitioning (collection, wgraph);
	}

	private double evaluatePartitioningAgree (Collection clustering, WeightedGraph graph) {

		Set edges = (Set)graph.getEdgeSet();
		Iterator i1 = edges.iterator();
		double cost = 0.0;

		while (i1.hasNext()) {
	    WeightedEdge e = (WeightedEdge)i1.next();
	    VertexImpl v1 = (VertexImpl)e.getVertexA();
	    VertexImpl v2 = (VertexImpl)e.getVertexB();
	    
	    if (inSameCluster (clustering, ((List)v1.getObject()).get(0), ((List)v2.getObject()).get(0))) {
				cost += e.getWeight();
	    }
		}
		return cost;
	}

	private double evaluatePartitioningDisAgree (Collection clustering, WeightedGraph graph) {

		Set edges = (Set)graph.getEdgeSet();
		Iterator i1 = edges.iterator();
		double cost = 0.0;

		while (i1.hasNext()) {
	    WeightedEdge e = (WeightedEdge)i1.next();
	    VertexImpl v1 = (VertexImpl)e.getVertexA();
	    VertexImpl v2 = (VertexImpl)e.getVertexB();
	    if (!inSameCluster (clustering, ((List)v1.getObject()).get(0), 
													((List)v2.getObject()).get(0)))
				cost -= e.getWeight();
		}
		return cost;
	}	


	public double evaluatePartitioning (Collection clustering, WeightedGraph graph) {

		Set edges = (Set)graph.getEdgeSet();
		Iterator i1 = edges.iterator();
		double cost = 0.0;
		Citation c1,c2;
		Object o1,o2;

		if (clustering == null) {
	    System.out.println(" YIKES: clustering is null");
	    return 0.0;
		}
		

		while (i1.hasNext()) {
	    WeightedEdge e = (WeightedEdge)i1.next();
	    VertexImpl v1 = (VertexImpl)e.getVertexA();
	    VertexImpl v2 = (VertexImpl)e.getVertexB();
	    o1 = v1.getObject();
	    o2 = v2.getObject();

	    if ((o1 instanceof List) && ((List)o1).size() == 1)
				c1 = (Citation)((List)o1).get(0);
	    else break;
	    if ((o2 instanceof List) && ((List)o2).size() == 1)
				c2 = (Citation)((List)o2).get(0);
	    else break;
	    if (inSameCluster (clustering, c1, c2)) {
				/*				System.out.println("SAME: " + c1.getIndex() + " and " +
									c2.getIndex() + ": " + e.getWeight());*/
				cost += e.getWeight();
	    }
	    else {
				/*				System.out.println("DIFFERENT: " + c1.getIndex() + " and " +
									c2.getIndex() + ": " +
									(-e.getWeight())); */
				cost -= e.getWeight();
	    }
		}
		return cost;
	}

	public boolean inSameCluster (Collection clustering, Object o1, Object o2) {

		Iterator i1 = clustering.iterator();
		while (i1.hasNext()) {
	    Collection c = (Collection)i1.next();
	    if (c.contains(o1))
				return (c.contains(o2)) ? true : false;
	    if (c.contains(o2))
				return (c.contains(o1)) ? true : false;				
		}
		return false;
	}

	public class PseudoEdge {

		double weight;
		PseudoVertex v1;
		PseudoVertex v2;

		public PseudoEdge (PseudoVertex v1, PseudoVertex v2, double weight) {
	    this.v1 = v1;
	    this.v2 = v2;
	    this.weight = weight;
		}

		public double getWeight () {
	    return weight;
		}

		public PseudoVertex getV1 () {
	    return v1;
		}
		public PseudoVertex getV2 () {
	    return v2;
		}

	}

	public List createPseudoEdges (InstanceList instances, Map map) {
		List al = (List)new ArrayList();
		for (Iterator i1 = instances.iterator(); i1.hasNext();) {
	    Instance inst = (Instance)i1.next();
	    Object o1 = ((NodePair)inst.getSource()).getObject1();
	    Object o2 = ((NodePair)inst.getSource()).getObject2();
	    PseudoVertex po1 = (PseudoVertex)map.get(o1);
	    PseudoVertex po2 = (PseudoVertex)map.get(o2);
	    //			System.out.println("Creating edge out of " + po1 + " and " +
	    //			po2);
	    if (useNBestInference)
				al.add (new PseudoEdge(po1, po2, computeScore_NBest(meClassifier, inst)));
	    else
				al.add (new PseudoEdge(po1, po2, computeScore(meClassifier, inst)));
		}
		return al;
	}

	// this is similar to pseudo edge
	// the graph is implicit and this has structures to optimize
	// the agglomerative clustering AND maintain the objective
	// function score as we go
	public class PseudoVertex  {
		Set cluster; // let this be a set for faster duplicate detection
		Object obj;
		HashMap map;
		double treeVal; // the current tree value the cluster to which this vertex belongs
		
		public PseudoVertex (InstanceList instances, Object mention) {
	    cluster = new LinkedHashSet(); // list of other vertices in
	    this.obj = mention;
	    this.map = new HashMap();
	    initializeMap (instances, mention);
	    cluster.add(this);
		}

		public double lookupEdgeWeight (PseudoVertex v2) {
	    Double d = (Double)map.get(v2.getObject());
	    if (d == null) {
				return 0.0;
	    }
	    return (double)d.doubleValue();
		}

		public Set getCluster () {
	    return cluster;
		}

		public Map getMap () {
	    return map;
		}

		public Object getObject() {
	    return obj;
		}
		
		private void initializeMap (InstanceList l1, Object o1) {
	    for (Iterator i1 = l1.iterator(); i1.hasNext();) {
				Instance inst = (Instance)i1.next();
				NodePair p1 = (NodePair)inst.getSource();
				if (p1.getObject1() == o1)
					map.put(p1.getObject2(), new Double(computeScore(meClassifier, inst)));
				else if (p1.getObject2() == o1)
					map.put(p1.getObject1(), new Double(computeScore(meClassifier, inst)));
	    }
		}
	}

	public Collection createPseudoVertices (InstanceList instances, List mentions, HashMap map) {
		Collection vs = new ArrayList();
		for (Iterator i1 = mentions.iterator(); i1.hasNext();) {
	    Object o1 = i1.next();
	    PseudoVertex pv = new PseudoVertex (instances, o1);
	    vs.add (pv);
	    map.put (o1, pv);
		}
		return vs;
	}

	private double computeInitialObjFnVal (Collection edges) {
		double val = 0.0;
		for (Iterator i1 = edges.iterator(); i1.hasNext(); ) {
	    val -= ((PseudoEdge)i1.next()).getWeight();
		}
		return val;
	}

	public double updateScore (double curScore, double [] treeScore, PseudoVertex v1, PseudoVertex v2,
														 Set s1, Set s2, boolean over_ride) {

		double origScore = curScore;
		double nScore = 0.0;
		double newScore = 0.0;
		
		for (Iterator i1 = s1.iterator(); i1.hasNext(); ) {
	    PseudoVertex v11 = (PseudoVertex)i1.next();
	    for (Iterator i2 = v2.getCluster().iterator(); i2.hasNext(); ) {
				PseudoVertex v22 = (PseudoVertex)i2.next();
				nScore += (2.0 * v11.lookupEdgeWeight(v22));
	    }
		}
		newScore = nScore + curScore;

		/*
			This section will update the tree model score efficiently.
		*/
		double updatedVal = 0.0;
		
		if (treeModel != null) {
	    Collection clusterpair = (Collection)new ArrayList();
	    Collection c1 = (Collection)new ArrayList();
	    Collection c2 = (Collection)new ArrayList();
	    Collection cBoth = (Collection)new ArrayList();
	    for (Iterator ii = s1.iterator(); ii.hasNext(); ) {
				PseudoVertex ppv = (PseudoVertex)ii.next();
				c1.add((Citation)ppv.getObject());
				cBoth.add((Citation)ppv.getObject());
	    }
	    for (Iterator ii = s2.iterator(); ii.hasNext(); ) {
				PseudoVertex ppv = (PseudoVertex)ii.next();
				c2.add((Citation)ppv.getObject());	
				cBoth.add((Citation)ppv.getObject());		
	    }
	    clusterpair.add(c1);
	    clusterpair.add(c2);
	    //System.out.println("--------------");
	    //System.out.println("Pair: ");
	    double pairVal = treeModel.computeTreeObjFn(clusterpair, false);
	    Collection clusterWrap = (Collection)new ArrayList();
	    clusterWrap.add(cBoth);
	    //System.out.println("New group: ");
	    double newVal = treeModel.computeTreeObjFn(clusterWrap, false);
	    //System.out.println("pairVal: " + pairVal + "  newVal" + newVal);
	    //System.out.println("--------------");
	    updatedVal = (treeScore[0] + (newVal - pairVal));
		}

		
		//now commit to the results if the newScore is higher
		if ((newScore >= origScore) || over_ride) {
	    // update tree score, as we're committing to this update
	    treeScore[0] = updatedVal;
			
	    for (Iterator i1 = s1.iterator(); i1.hasNext(); ) {
				PseudoVertex v11 = (PseudoVertex)i1.next();
				Set s11 = v11.getCluster();
				s11.addAll(s2);
				s11.addAll(s1);
	    }
	    for (Iterator i2 = s2.iterator(); i2.hasNext(); ) {
				PseudoVertex v22 = (PseudoVertex)i2.next();
				Set s22 = v22.getCluster();
				s22.addAll(s1);
				s22.addAll(s2);
	    }
		}
		return newScore;
	}

	public double computeInitialTreeObjScore (Collection pvertices) {

		Collection c1 = (Collection)new ArrayList();
		for (Iterator ii = pvertices.iterator(); ii.hasNext(); ) {
	    PseudoVertex ppv = (PseudoVertex)ii.next();
	    Collection c2 = (Collection)new ArrayList();
	    c2.add((Citation)ppv.getObject());
	    c1.add(c2);
		}
		return treeModel.computeTreeObjFn(c1);
	}
	

	public Collection absoluteCluster (InstanceList ilist, List mentions) {

		List mCopy = new ArrayList();
		boolean newCluster;
		java.util.Random r = new java.util.Random();
		int numTries = 0;

		// get pseudo edges and sort em
		HashMap vsToPvs = new HashMap();
		// vsToPvs set destructively
		Collection pvertices = createPseudoVertices (ilist, mentions, vsToPvs);

		for (Iterator i1 = pvertices.iterator(); i1.hasNext(); ) {
	    PseudoVertex v = (PseudoVertex)i1.next();
	    mCopy.add(v);
		}

		List pedges = createPseudoEdges (ilist, vsToPvs);
		Collections.sort(pedges,new PseudoEdgeComparator());
		double initialObjVal = computeInitialObjFnVal (pedges);
		System.out.println("initial obj fn: " + initialObjVal);
		double objFnVal = initialObjVal;
		double prevVal = -10000000000.0;
		int i = 0;
		while (true) {
	    prevVal = objFnVal;
	    PseudoEdge pedge = (PseudoEdge)pedges.get(i); // choosePseudoEdge (pedges, r);
	    PseudoVertex v1 = (PseudoVertex)pedge.getV1();
	    PseudoVertex v2 = (PseudoVertex)pedge.getV2();
	    Set s1 = new LinkedHashSet();
	    Set s2 = new LinkedHashSet();
	    // make copies of the sets
	    for (Iterator i1 = v1.getCluster().iterator(); i1.hasNext(); ) {
				s1.add(i1.next());
	    }
	    for (Iterator i1 = v2.getCluster().iterator(); i1.hasNext(); ) {
				s2.add(i1.next());
	    }
	    // this is the case for when the edge is now irrelevent through
	    // transitive closure - just remove it and start at beginning
	    if (s1.contains(v2) || s2.contains(v1)) {
				//pedges.remove(i);
				continue;
	    }
	    double[] n = new double[]{0.0};
	    objFnVal = updateScore (objFnVal, n , v1, v2, s1, s2, false);
	    i++;
	    if (objFnVal <= prevVal) {
				numTries++;
				objFnVal = prevVal; // reset it, since we don't commit to this
	    }
	    else {
				numTries = 0;
	    }
	    if (numTries > MAX_REDUCTIONS)
				break;
	    //System.out.println("ObjFnVal: " + objFnVal);
		}

		// build a proper graph from the edges since
		// the evaluation code relies on this structure
		this.wgraph = buildGraphFromPseudoEdges (pedges, mentions);

		Collection citClustering = new ArrayList();
		for (Iterator i1 = pvertices.iterator(); i1.hasNext(); ) {
	    PseudoVertex v1 = (PseudoVertex)i1.next();
	    Collection cluster = v1.getCluster();
	    newCluster = true;
	    if (citClustering.size() == 0)
				newCluster = true;
	    for (Iterator i2 = citClustering.iterator(); i2.hasNext(); ) {
				Collection c2 = (Collection)i2.next();
				if (c2.containsAll(cluster)) {
					newCluster = false;
					break;
				}
	    }
	    if (newCluster) {
				citClustering.add(cluster);
	    }
		}

		Collection realClustering = new ArrayList();
		for (Iterator i1 = citClustering.iterator(); i1.hasNext(); ) {
	    Collection s1 = (Collection)i1.next();
	    Collection n1 = new ArrayList();
	    for (Iterator i2 = s1.iterator(); i2.hasNext(); ) {
				n1.add((Citation)((PseudoVertex)i2.next()).getObject());
	    }
	    realClustering.add (n1);
		}
		return (Collection)realClustering;

	}

	protected Collection getPseudoClustering (Collection pvertices) {
		Collection citClustering = new ArrayList();
		boolean newCluster;
		for (Iterator i1 = pvertices.iterator(); i1.hasNext(); ) {
	    PseudoVertex v1 = (PseudoVertex)i1.next();
	    Collection cluster = v1.getCluster();
	    newCluster = true;
	    if (citClustering.size() == 0)
				newCluster = true;
	    for (Iterator i2 = citClustering.iterator(); i2.hasNext(); ) {
				Collection c2 = (Collection)i2.next();
				if (c2.containsAll(cluster)) {
					newCluster = false;
					break;
				}
	    }
	    if (newCluster) {
				citClustering.add(cluster);
	    }
		}
		return citClustering;
	}

	public Collection typicalClusterAdv (InstanceList ilist, List mentions) {

		List pedgesC = null;
		Collection pvertices = null;
		for (int j=0; j < MAX_ITERS; j++) {
	    List mCopy = new ArrayList();
	    java.util.Random r = new java.util.Random();
	    int numTries = 0;
	    // get pseudo edges and sort em
	    HashMap vsToPvs = new HashMap();
	    // vsToPvs set destructively
	    pvertices = createPseudoVertices (ilist, mentions, vsToPvs);

	    for (Iterator i1 = pvertices.iterator(); i1.hasNext(); ) {
				PseudoVertex v = (PseudoVertex)i1.next();
				mCopy.add(v);
	    }
	    List pedges = createPseudoEdges (ilist, vsToPvs);
	    pedgesC = new ArrayList();
	    for (Iterator it = pedges.iterator(); it.hasNext();) {
				pedgesC.add((PseudoEdge)it.next());
	    }
	    // build the actual graph for scoring puposees

	    this.wgraph = buildGraphFromPseudoEdges (pedgesC, mentions);
	    Collections.sort(pedges,new PseudoEdgeComparator());
	    for (int k=0; k < 50; k++) {
				PseudoEdge e1 = (PseudoEdge)pedges.get(k);
	    }
	    double initialObjVal = computeInitialObjFnVal (pedges);

	    double initialTreeObjVal;
	    if (treeModel != null)
				initialTreeObjVal = computeInitialTreeObjScore (pvertices);
	    else
				initialTreeObjVal = 0.0;
			
	    double[] treeObjVal;
	    treeObjVal = new double[]{initialTreeObjVal};

	    double objFnVal = initialObjVal;
	    double prevVal = -10000000000.0;
	    int i = 0;
	    int numClusters = pvertices.size();
	    while (true) {
				prevVal = objFnVal;
				int choice = r.nextInt(rBeamSize); // selection size
				if (choice > pedges.size())
					break;
				PseudoEdge pedge = (PseudoEdge)pedges.get(choice); // choosePseudoEdge (pedges, r);
				PseudoVertex v1 = (PseudoVertex)pedge.getV1();
				PseudoVertex v2 = (PseudoVertex)pedge.getV2();
				Set s1 = new LinkedHashSet();
				Set s2 = new LinkedHashSet();
				// make copies of the sets of vertices represented by each pseudovertex
				for (Iterator i1 = v1.getCluster().iterator(); i1.hasNext(); ) {
					s1.add(i1.next());
				}
				for (Iterator i1 = v2.getCluster().iterator(); i1.hasNext(); ) {
					s2.add(i1.next());
				}
				// this is the case for when the edge is now irrelevent through
				// transitive closure - just remove it and start at beginning
				if (s1.contains(v2) || s2.contains(v1)) {
					pedges.remove(choice);
					continue;
				}
				numClusters--;
				if (trueNumStop) {
					objFnVal = updateScore (objFnVal, treeObjVal, v1, v2, s1, s2, true);
					//Collection cl1 = (Collection)getClusteringFromPseudo(pvertices);
					//System.out.println("+++++++++++++++++++++++++++");
					//double treeV = treeModel.computeTreeObjFn(cl1, false);
					//System.out.println("+++++++++++++++++++++++++++");
					//System.out.println("treeV: " + treeV + " updated: " + treeObjVal[0]);
				}
				else
					objFnVal = updateScore (objFnVal, treeObjVal, v1, v2, s1, s2, false);
				if (!trueNumStop && objFnVal <= prevVal) {
					numTries++;
					objFnVal = prevVal; // reset it, since we don't commit to this
				}
				else {
					//System.out.println(objFnVal + "," + treeObjVal[0]);
					pedges.remove(choice);
					numTries = 0;
				}
				if (trueNumStop && numClusters <= keyPartitioning.size()) {
					Collection cl1 = (Collection)getClusteringFromPseudo(pvertices);
					PairEvaluate pairEval = new PairEvaluate (keyPartitioning, cl1);
					pairEval.evaluate();
					double curAgree = evaluatePartitioningAgree (cl1, this.wgraph);
					double curDisAgree = evaluatePartitioningDisAgree (cl1, this.wgraph);
					/*
						double treeV = treeModel.computeTreeObjFn(cl1, false);
						if (Math.abs(treeV - treeObjVal[0]) > 0.01)
						System.out.println("Tree values don't match: " + treeV + ":" + treeObjVal[0]);*/
					int singles = numSingletons(cl1);
					System.out.println(objFnVal + "," + treeObjVal[0] + "," + curAgree
														 + "," + curDisAgree + "," + singles + "," + pairEval.getF1());
					break;
				}
				else if (numTries > MAX_REDUCTIONS) {
					Collection cl1 = (Collection)getClusteringFromPseudo(pvertices);
					PairEvaluate pairEval = new PairEvaluate (keyPartitioning, cl1);	
					pairEval.evaluate();
					System.out.println(objFnVal + "," + treeObjVal[0] + "," + pairEval.getF1());
					break;
				}
				//System.out.println("ObjFnVal: " + objFnVal);
	    }
		}

		// build a proper graph from the edges since
		// the evaluation code relies on this structure

	
		return (Collection)getClusteringFromPseudo(pvertices);

	}

	protected int numSingletons (Collection clustering) {

		int total = 0;
		for (Iterator it = clustering.iterator(); it.hasNext(); ) {
	    if (((Collection)it.next()).size() == 1)
				total++;
		}
		return total;
	}

	protected Collection getClusteringFromPseudo (Collection pvertices) {
		Collection citClustering = getPseudoClustering (pvertices);
		Collection realClustering = new ArrayList();
		for (Iterator i1 = citClustering.iterator(); i1.hasNext(); ) {
	    Collection s1 = (Collection)i1.next();
	    Collection n1 = new ArrayList();
	    for (Iterator i2 = s1.iterator(); i2.hasNext(); ) {
				n1.add((Citation)((PseudoVertex)i2.next()).getObject());
	    }
	    realClustering.add (n1);
		}
		return (Collection)realClustering;
	}

	protected WeightedGraph buildGraphFromPseudoEdges (List pedges, List mentions) {

		HashMap alreadyAdded = new HashMap();
		WeightedGraph w = (WeightedGraph)new WeightedGraphImpl();
		for (Iterator it = pedges.iterator(); it.hasNext(); ) {
	    constructEdgesFromPseudoEdges (w, (PseudoEdge)it.next(), alreadyAdded);
		}
		addVerticesToGraph (w, mentions, alreadyAdded);
		return w;
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
			System.err.println ("bestEdgeVal: " + bestEdgeVal + " threshold: " + threshold);
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

	public Collection partitionGraph (WeightedGraph origGraph) {

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
	    PairEvaluate pairEval = new PairEvaluate (keyPartitioning, localBestPartitioning);
	    pairEval.evaluate();
			
	    if (treeModel != null)
				treeCost = treeModel.computeTreeObjFn (curPartitioning);

	    if (curCost > bestCost) {
				bestCost = curCost;
				bestPartitioning = localBestPartitioning;

	    }

	    System.out.println(curCost + "," + evaluateAgainstKey (localBestPartitioning)
												 + "," + pairEval.getF1() + "," + treeCost + "," + keyPartitioning.size());

		}
		return bestPartitioning;

	}

	public double evaluateAgainstKey (Collection col) {
		if (keyPartitioning != null) {

	    ClusterEvaluate cl = new ClusterEvaluate(keyPartitioning, col);
	    cl.evaluate();
	    //cl.printResults();
	    //cl.printVerbose();
	    return cl.getRecall();
		}
		return 0.0;

	}
 
	public class EdgeComparator implements Comparator {

		public EdgeComparator () {}

		public int compare (Object e1, Object e2) {
	    // note that this is backwards because we want it to sort in
	    // descending order
	    if (e1 == e2)
				return 0;
	    double val = ((WeightedEdge)e2).getWeight()-((WeightedEdge)e1).getWeight();
	    if (val > 0)
				return 1;
	    else
				return -1;
			
		}
		
		public boolean equals (Object e1, Object e2) {
	    return e1 == e2;
		}

	}

	public class PseudoEdgeComparator implements Comparator {

		public PseudoEdgeComparator () {}

		public int compare (Object e1, Object e2) {
	    // note that this is backwards because we want it to sort in
	    // descending order
	    if (e1 == e2)
				return 0;
	    double val = ((PseudoEdge)e2).getWeight()-((PseudoEdge)e1).getWeight();
	    if (val > 0)
				return 1;
	    else
				return -1;
		}
		public boolean equals (Object e1, Object e2) {
	    return e1 == e2;
		}
	}

	/**
	 * Construct a Collection of Collections where the objects in the
	 * collections are the original objects (i.e. the object of the vertices)
	 */
	public Collection getCollectionOfOriginalObjects (Collection vertices) {
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

	private void printVObj (Object o1) {
		if (o1 instanceof List) {
	    List l10 = (List)o1;
	    for (int k=0; k < l10.size(); k++) {
				System.out.print(" " + ((Citation)l10.get(k)).getIndex());
	    }
		} else 
	    System.out.print(" " + ((Citation)o1).getIndex());
	}

	public void mergeVertices (WeightedGraph g, VertexImpl v1, VertexImpl v2) {
		// Change: mhay 1/10/04
		//addNegativeWeights(g, v1, v2);

		Object o1 = v1.getObject();
		Object o2 = v2.getObject();
		List l1 = new ArrayList();

		//System.out.println("Merging o1 = " + toString(o1) + " and o2 = " +
		//toString(o2));
		if (o1 instanceof List)
			l1.addAll ((List)o1);
		else l1.add (o1);
		if (o2 instanceof List)
			l1.addAll ((List)o2);
		else l1.add (o2);

		
/*		if ((o1 instanceof List) && (o2 instanceof List)) {
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
	}

	public void printGraph (WeightedGraph g) {
		Set vs = g.getVertexSet();
		Iterator i = vs.iterator();
		System.out.println("Vertices: " + vs);
		while (i.hasNext()) {
	    VertexImpl v = (VertexImpl)i.next();
	    printVObj(v.getObject());
	    System.out.println(" ");
		}
		Set es = g.getEdgeSet();
		Iterator i2 = es.iterator();
		System.out.println("Edges: ");
		while (i2.hasNext()) {
	    WeightedEdge e = (WeightedEdge)i2.next();
	    VertexImpl v1 = (VertexImpl)e.getVertexA();
	    VertexImpl v2 = (VertexImpl)e.getVertexB();
	    printVObj(v1.getObject());
	    System.out.print(" <----> (" + e.getWeight() + ") ");
	    printVObj(v2.getObject());
	    System.out.println("");
		}

	}

	private double computeScore_NBest (MaxEnt classifier, Instance origInstPair, int ind1, int ind2) {
		NodePair origNodePair = (NodePair)origInstPair.getSource();
		Citation citation1 = (Citation)origNodePair.getObject1();
		Citation citation2 = (Citation)origNodePair.getObject2();
		Citation nBest1 = citation1.getNthBest(ind1);
		Citation nBest2 = citation2.getNthBest(ind2);
		NodePair newPair = new NodePair (nBest1, nBest2);
		Pipe pipe = origInstPair.getPipe();
		return computeScore(classifier, new Instance(newPair, "yes", null, newPair, pipe));
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
	private double computeScore_NBest(MaxEnt classifier, Instance origInstPair) {
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
	    Instance instPair = new Instance (origNodePair, label, name, source, pipe);
	    double score = computeScore(classifier, instPair);
	    int i1 = citation1.getIndex();
	    int i2 = citation2.getIndex();
	    if (score < 0.0 && label == "yes") {
				System.out.println(i1 + " " + i2 + " " + score + " " + label);
				falseNegatives++;
	    } else if (score > 0.0 && label == "no") {
				System.out.println(i1 + " " + i2 + " " + score + " " + label);
				falsePositives++;
	    }
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
				if (confidenceWeightedScores) {
					double weight = (nbest_citation1.getConfidenceScore() *
													 nbest_citation2.getConfidenceScore());
					scores.add(new Double(score*weight));
				} else {
					scores.add(new Double(score));
				}
				j++;
	    }
	    i++;
		}
		// this version actually returns the highest possible edge when they
		// _are_ coreferential and the lowest posslbe edge otherwise
		double optEdgeWeight;
		if (useOptimal) {
	    if (origInstPair.getTarget().toString().equals("yes")) {
				optEdgeWeight = ((Double)Collections.max(scores)).doubleValue();
				//System.out.println("HIGHEST edge weight " + optEdgeWeight + " selected from "
				//								 + scores);
	    }
	    else {
				optEdgeWeight = ((Double)Collections.min(scores)).doubleValue();
				//System.out.println("LOWEST edge weight " + optEdgeWeight + " selected from "
				//									 + scores);
	    }
		} else {
	    // just average all the pair-wise scores for now
	    // eventually do something sophisticated here 
	    optEdgeWeight = collectionAvg (scores);
		}
		
		return optEdgeWeight;
	}

	protected double collectionAvg (Collection collection) {
		double sum = 0.0;
		for (Iterator it = collection.iterator(); it.hasNext(); ) {
	    sum += ((Double)it.next()).doubleValue();
		}
		return sum/(double)collection.size();
	}

	protected boolean hasNextIndexList(int[] indexList, int N)
	{
		for(int i=0; i<indexList.length; i++){
	    if(indexList[i] < N-1) return true;
		}

		return false;
	}

	protected int[] nextIndexList(int[] indexList, int N)
	{
		for(int i=indexList.length-1; i>=0; i--){
	    if(indexList[i] <= N-2){
				indexList[i] ++;

				for(int j=i+1; j<=indexList.length-1;j++){
					indexList[j] = 0;
				}

				break;
	    }
		}
		return indexList;
	}

	protected int[] nextIndexListStochastic (int[] indexList, int N) {
		java.util.Random r = new java.util.Random();
		int v = 0;
		for (int i=0; i < indexList.length; i++) {
	    v = r.nextInt(N);
	    indexList[i] = v;
		}
		return indexList;
	}

	public double weightOfConfig (int [] indexList, List instList) {
		double score = 0.0;
		for (int i=0; i<indexList.length; i++) {
	    for (int j=i; j > 0; j--) {
				NodePair p = new NodePair((Citation)instList.get(i),(Citation)instList.get(j));
				Instance inst = new Instance(p, "yes", null, p, this.pipe);
				score += computeScore_NBest(meClassifier, inst, indexList[i], indexList[j]);
	    }
		}
		return score;
	}

	public void updateGraphNBest (WeightedGraph graph, int [] indexList, List instList, HashMap alreadyAdded) {

		for (int i=0; i<indexList.length; i++) {
	    for (int j=i+1; j < indexList.length; j++) {
				Object c1 = instList.get(i);
				Object c2 = instList.get(j);
				NodePair p = new NodePair((Citation)c1,(Citation)c2);
				Instance inst = new Instance(p, "yes", null, p, this.pipe);
				constructEdgesUsingTrainedClusterer (graph, inst, alreadyAdded,
																						 new Double (computeScore_NBest (meClassifier, inst, indexList[i], indexList[j])) );
	    }
		}

	}

	public WeightedGraph constructOptimalEdgesUsingNBest (List mentions, int N) {

		WeightedGraph graph = new WeightedGraphImpl();
		HashMap alreadyAddedVertices = new HashMap(); // keep track of
		HashMap bestCitationMap = new HashMap();
		
		for (Iterator iter = keyPartitioning.iterator(); iter.hasNext();) {
	    Collection cluster = (Collection)iter.next(); // get key cluster
	    List instList = Collections.list(Collections.enumeration(cluster));
	    int [] indexList = new int[instList.size()];
	    for (int j=0; j < indexList.length; j++) indexList[j] = 0; // initialize
	    int[] optimalIndexList = (int[])indexList.clone();
	    double highestWeight = weightOfConfig(indexList, instList);
	    //System.out.println("Cluster size: " + cluster.size());
	    int numCombinations = (int)Math.pow((double)N,(double)cluster.size());
	    if (numCombinations > 16000) {
				for (int k=0; k < 4000; k++) {
					indexList = nextIndexList(indexList, N);
					double weight = weightOfConfig(indexList, instList);
					if( weight > highestWeight ){
						highestWeight = weight;
						optimalIndexList = (int[])indexList.clone();
					}
				}
	    } else {
				while (hasNextIndexList(indexList, N)) {
					indexList = nextIndexList(indexList, N);
					double weight = weightOfConfig(indexList, instList);
					//printIList(indexList);
					//System.out.println(" -> " + weight);
					if( weight > highestWeight ){
						highestWeight = weight;
						optimalIndexList = (int[])indexList.clone();
					}
				}
	    }
	    for (int j=0; j < optimalIndexList.length; j++) {
				bestCitationMap.put(instList.get(j),new Integer(optimalIndexList[j]));
	    }
	    updateGraphNBest (graph, optimalIndexList, instList, alreadyAddedVertices);
		}
		addVerticesToGraph(graph, mentions, alreadyAddedVertices);
		//printGraph(graph);
		completeGraphNBest (graph, keyPartitioning, bestCitationMap);
		return graph;
	}

	private void printIList (int [] list) {
		for (int i=0; i < list.length; i++) {
	    System.out.print(" " + list[i]);
		}
	}

	public void completeGraphNBest (WeightedGraph graph, Collection keyPartitioning, Map citMap) {

		HashMap m1 = new HashMap(); // map from objects to their vertices
		Set vs = graph.getVertexSet();
		for (Iterator iter = vs.iterator(); iter.hasNext();) {
	    VertexImpl v = (VertexImpl)iter.next();
	    if ((v.getObject() instanceof List) && ((List)v.getObject()).size() == 1) {
				Object o = ((List)v.getObject()).get(0);
				m1.put(o,v);
	    }
		}
		List kList = Collections.list(Collections.enumeration(keyPartitioning));
		for (int i=0; i < kList.size(); i++) {
	    Collection c1 = (Collection)kList.get(i);
	    for (int j=i+1; j < kList.size(); j++) {
				Collection c2 = (Collection)kList.get(j);
				for (Iterator i1 = c1.iterator(); i1.hasNext();) {
					Citation cit1 = (Citation)i1.next();
					VertexImpl v1 = (VertexImpl)m1.get((Object)cit1);
					for (Iterator i2 = c2.iterator(); i2.hasNext();) {
						Citation cit2 = (Citation)i2.next();
						VertexImpl v2 = (VertexImpl)m1.get((Object)cit2);
						NodePair np = new NodePair (cit1, cit2);
						Instance inst = new Instance (np, "no", null, np, this.pipe);
						double eval = computeScore_NBest(meClassifier, inst, ((Integer)citMap.get(cit1)).intValue(),
																						 ((Integer)citMap.get(cit2)).intValue());
						try {
							graph.addEdge (v1, v2, eval);
						} catch (Exception e) {e.printStackTrace();}
					}
				}
	    }
		}
	}

	protected void constructEdgesFromPseudoEdges (WeightedGraph graph,
																								PseudoEdge pedge,
																								HashMap alreadyAdded ) {
		PseudoVertex pv1 = pedge.getV1();
		PseudoVertex pv2 = pedge.getV2();
		Object node1 = pv1.getObject();
		Object node2 = pv2.getObject();
		VertexImpl v1 = (VertexImpl)alreadyAdded.get(pv1);
		VertexImpl v2 = (VertexImpl)alreadyAdded.get(pv2);
		
		if (v1 == null) {
	    ArrayList a1 = new ArrayList();
	    a1.add(node1);
	    v1 = new VertexImpl(a1);
	    alreadyAdded.put(node1,v1);
		}
		if (v2 == null) {
	    ArrayList a2 = new ArrayList();
	    a2.add(node2);			
	    v2 = new VertexImpl(a2);
	    alreadyAdded.put(node2,v2);
		}
		try {
	    graph.addEdge (v1, v2, pedge.getWeight());
		} catch (Exception e) {e.printStackTrace();}
		
	}
	
	public void constructEdgesUsingTrainedClusterer (WeightedGraph graph,
																									 Instance instPair,
																									 HashMap alreadyAdded) {
		constructEdgesUsingTrainedClusterer (graph, instPair, alreadyAdded, null);
	}

	
	public void constructEdgesUsingTrainedClusterer (WeightedGraph graph,
																									 Instance instPair,
																									 HashMap alreadyAdded,
																									 Double edgeWeight) {
		constructEdgesUsingTrainedClusterer (graph, instPair,
																				 alreadyAdded, edgeWeight, this.meClassifier);
	}
	
	public void constructEdgesUsingTrainedClusterer (WeightedGraph graph,
																									 Instance instPair,
																									 HashMap alreadyAdded,
																									 Double edgeWeight,
																									 MaxEnt classifier)
	{
		NodePair mentionPair = (NodePair)instPair.getSource();
		Object node1 = mentionPair.getObject1();
		Object node2 = mentionPair.getObject2();
		VertexImpl v1 = (VertexImpl)alreadyAdded.get(node1);
		VertexImpl v2 = (VertexImpl)alreadyAdded.get(node2);

		if (v1 == null) {
	    ArrayList a1 = new ArrayList();
	    a1.add(node1);
	    v1 = new VertexImpl(a1);
	    alreadyAdded.put(node1,v1);
		}
		if (v2 == null) {
	    ArrayList a2 = new ArrayList();
	    a2.add(node2);			
	    v2 = new VertexImpl(a2);
	    alreadyAdded.put(node2,v2);
		}
		
		double  edgeVal = 0.0;
		double  edgeVal2;
		if (edgeWeight == null) {
	    if (classifier != null) {
				if (useNBestInference) {
					Classification classification = (Classification)classifier.classify(instPair);
					Labeling labeling = classification.getLabeling();
					edgeVal = computeScore_NBest(classifier, instPair);
					if (labeling.labelAtLocation(0).toString().equals("no")) {
						edgeVal2 =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
					} else {
						edgeVal2 =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
					}
					if ((edgeVal > 0 && edgeVal2 < 0) ||
							(edgeVal < 0 && edgeVal2 > 0))
						System.out.println(" " + edgeVal + " (" + edgeVal2 + ")");
				} else {
					// Include the feature weights according to each label
					Classification classification = (Classification)classifier.classify(instPair);
					Labeling labeling = classification.getLabeling();
					//classifier.getUnnormalizedClassificationScores(instPair, scores);
					if (labeling.labelAtLocation(0).toString().equals("no")) {
						edgeVal =  labeling.valueAtLocation(1)-labeling.valueAtLocation(0);
					} else {
						edgeVal =  labeling.valueAtLocation(0)-labeling.valueAtLocation(1);
					}
				}
	    } else if (sgdParameters != null) {
				FeatureVector fv = (FeatureVector)instPair.getData();
				double scores [] = new double[2];
				getUnNormalizedScores (sgdParameters, fv, scores);
				edgeVal = scores[1] - scores[0];
	    }
		} else {
	    edgeVal = edgeWeight.doubleValue();
		}
		try {
	    if (node1 != null && node2 != null) {
				graph.addEdge (v1, v2, edgeVal);
	    }
		} catch (Exception e) {e.printStackTrace();}
	}

	public void getUnNormalizedScores (Matrix2 lambdas, FeatureVector fv, double[] scores)
	{
		for (int li = 0; li < 2; li++) {
	    scores[li] = lambdas.value (li, numSGDFeatures)
									 + lambdas.rowDotProduct (li, fv, numSGDFeatures,null);
		}
	}

	private double computeScore(MaxEnt classifier, Instance instPair) {

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
