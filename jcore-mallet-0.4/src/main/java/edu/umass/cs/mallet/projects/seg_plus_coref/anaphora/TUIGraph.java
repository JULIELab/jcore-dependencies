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

package edu.umass.cs.mallet.projects.seg_plus_coref.anaphora;

import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.classify.MaxEnt;
import edu.umass.cs.mallet.base.classify.MaxEntTrainer;
import edu.umass.cs.mallet.base.classify.Trial;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.Target2Label;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
import salvo.jesus.graph.WeightedEdge;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.util.*;

public class TUIGraph
{

    public static final String [] pronouns = new String[] {"He", "he", "Him",
							   "him",
							   "His", "his",
							   "She", "she", "Her",
							   "her", "hers", "it",
							   "It",
							   "its",
							   "Its", "itself",
							   "himself",
							   "herself"};

    public static final int pronounsSize = 18;
    public static void main (String[] args)
    {

	if (new Integer(4) == new Integer(4))
	    System.out.println("INTERESTING");
	String trainingDataPath;
	String testDataPath;
	if (args.length != 2) {
	    // System.exit(-1);
	    //trainingDataPath = new String ("/odin.mitre.org/tmp/treebank/xml-bigger/train");
	    //testDataPath = new String ("/odin.mitre.org/tmp/treebank/xml-bigger/train");
	    //trainingDataPath = new String("c:/JavaDevel/data/toy");
	    //testDataPath = new String("c:/JavaDevel/data/toy");
	    trainingDataPath = new String("/usr/dan/users8/wellner/data/all-docs/test-annotated");
	    testDataPath = new String("/usr/dan/users8/wellner/data/all-docs/mini-train");
	} else {
	    trainingDataPath = args[0];
	    testDataPath = args[1];
	}
	
	// This iterator takes a directory and iterates over the files contained
	// in it
	
	XMLFileFilter filter = new XMLFileFilter(".*xml");
	
	FileIterator fileIterator = new FileIterator (new File(trainingDataPath), (FileFilter)filter);
	FileIterator testFileIterator = new FileIterator (new File(testDataPath), (FileFilter)filter);
	
	ArrayList pairFilters = new ArrayList();
	pairFilters.add(new MentionPairFilter());
	
	// This iterator takes an iterator over files, and iterates over all (relevant)
	// pairs of DOM nodes in each file
	//MentionPairIterator pairIterator = new MentionPairIterator (fileIterator, "TB", true, true);
	MentionPairIterator pairIterator = new MentionPairIterator (fileIterator, "MUC", true, true, true, pairFilters);
	//MentionPairIterator testPairIterator = new MentionPairIterator
	//    (testFileIterator, "TB", true, true);
	MentionPairIterator testPairIterator = new MentionPairIterator
	    (testFileIterator, "MUC", true, true, true, pairFilters);
	// This pipeline takes individual pairs as input and produces a feature vector

	Pipe instancePipe = new SerialPipes (new Pipe[] {
	    new Target2Label(),
	    new AffixOfMentionPair (),
	    new MentionPairHeadIdentical(),
	    new MentionPairIdentical(),
	    new MentionPairSentenceDistance(),
	    new PartOfSpeechMentionPair(),
	    new HobbsDistanceMentionPair(),
	    new MentionPairAntecedentPosition(),
	    new NullAntecedentFeatureExtractor(),
	    new ModifierWordFeatures(),
	    new MentionPair2FeatureVector ()
	    });

	/*
	Pipe instancePipe = new SerialPipes (new Pipe[] {
	    new Target2Label(),
	    new AffixOfMentionPair (),
	    //new MentionPairHeadIdentical(),
	    //new MentionPairIdentical(),
	    new NullAntecedentFeatureExtractor(),
	    new MentionPair2FeatureVector()
	    }); */

	InstanceList ilist = new InstanceList (instancePipe);
	ilist.add (pairIterator);
	
	InstanceList testList = new InstanceList (instancePipe);
	testList.add (testPairIterator);
	
	InstanceList[] ilists = ilist.split (new double[] {.7, .3});
	MaxEnt classifier = (MaxEnt)new MaxEntTrainer().train (ilist);
	System.out.println ("Training Accuracy on \"yes\" = "+
			    new Trial (classifier, ilist).labelF1("yes"));
	System.out.println ("Training Accuracy on \"no\" = "+
			    new Trial (classifier, ilist).labelF1("no"));

	System.out.println ("Testing Accuracy on \"yes\" = "+
			    new Trial (classifier, testList).labelF1("yes"));
	System.out.println ("Testing Accuracy on \"no\" = "+
			    new Trial (classifier, testList).labelF1("no"));

	Set trainingDocuments = MentionPairIterator.partitionIntoDocumentInstances(ilist);
	Set testDocuments = MentionPairIterator.partitionIntoDocumentInstances(testList);

	Clusterer clusterer = new Clusterer();
	int numInstances = testDocuments.size();
	int documentIndex = 0;
	Iterator iter1 = testDocuments.iterator();
	int docIndex = 0;
	
	while (iter1.hasNext()) {  // iterates over doc training instances
	    LinkedHashSet keyClusters = new LinkedHashSet();
	    MappedGraph graph = new MappedGraph(); // need a MappedGraph because we need to be able to copy
	    // Create the graph with all the correct edge weights, using the current (averaged?) lambdas
	    List testMentionPairs = (List)iter1.next();
	    KeyClustering keyClustering = collectAllKeyClusters(testMentionPairs);
	    keyClustering.print();
	    Iterator trPairIterator = testMentionPairs.iterator();
	    Clustering mortonClustering = getMortonClustering(testMentionPairs, classifier);
	    System.out.println("Number of pairs: " + testMentionPairs.size());
	    while (trPairIterator.hasNext()) {
		Instance mentionPair = (Instance)trPairIterator.next();
		//constructEdgesUsingTargets (graph, mentionPair);
		constructEdgesUsingModel (graph, classifier, mentionPair);
		//coalesceNewPair (keyClusters, mentionPair);
	    }

	    clusterer.setGraph(graph);
	    Clustering clustering = clusterer.getClustering(); // this could have memory of graphs
	    System.out.println("Model clusters: ");
	    clustering.printDetailed();
	    //System.out.println("Morton clusters: ");
	    // mortonClustering.print();
	    System.out.println("Key clusters: ");
	    keyClustering.printDetailed();
	    ClusterEvaluate eval = new ClusterEvaluate(keyClustering,
						       mortonClustering);
	    eval.evaluate();
	    System.out.println("F1 morton is : " + eval.getF1());

	    ClusterEvaluate eval1 = new ClusterEvaluate(keyClustering,
							clustering);
	    eval1.evaluate();
	    System.out.println("F1 using model is : " + eval1.getF1());

	    ClusterEvaluate eval2 =  new ClusterEvaluate(keyClustering, keyClustering);
	    eval2.evaluate();
	    System.out.println("F1 using keykey is : " + eval2.getF1());

	    System.out.println("Pairwise key:morton");
	    PairEvaluate pairEval1 = new PairEvaluate(keyClustering, mortonClustering);
	    pairEval1.evaluate();
	    System.out.println("Morton pairF1: " + pairEval1.getF1());
	    System.out.println("Pairwise key:model");
	    PairEvaluate pairEval2 = new PairEvaluate(keyClustering, clustering);
	    pairEval2.evaluate();
	    System.out.println("Model pairF1: " + pairEval2.getF1());
	    System.out.println("\n\n Error analysis: MORTON");
	    eval.printErrors(true);
	    System.out.println("\n\n Error analysis: Model");
	    eval1.printErrors(true);
	    System.out.println("Mapping: ");
	    graph.printMap();
	    //System.out.println("Graph:" + graph.getGraph());
	    
	}
    }

    public static Clustering getMortonClustering (List trainingMentionPairs, Classifier classifier)
    {
	MortonClustering mortClustering = new MortonClustering();

	Iterator iter = trainingMentionPairs.iterator();
	Mention curRef = null;
	Mention bestAntecedent = null;
	double  bestValue = -10000.0;
	double  edgeVal = -10000.0;
	while (iter.hasNext()) {
	    Instance inst = (Instance)iter.next();
	    MentionPair pair = (MentionPair)inst.getSource();
	    LabelVector labelVec = classifier.classify(inst).getLabelVector();
	    Mention ref = pair.getReferent();
	    Mention ant = pair.getAntecedent();
	    
	    //if ((referentPronoun (ref))) {
	    //if ((referentPronoun (ref)) || ((referentNNP(ref) && (ant != null) && referentNNP(ant)))) {
	    //if (false) {
	    if (true) {
		for (int i=0; i < labelVec.singleSize(); i++) {
		    if (labelVec.labelAtLocation(i).toString().equals("yes"))
			edgeVal = labelVec.valueAtLocation(i);
		}
	    } else if (pair.getEntityReference() != null) {
		edgeVal = 1.0;
		//mortClustering.addToClustering(ref,ant); // automatically add
		//System.out.println("Edge - " + edgeVal);
		//if (bestAntecedent != null)
		//System.out.println(" -- best " + bestAntecedent.getString());
	    } else {
		edgeVal = -10000.0;
	    }
	    if (ref != curRef) { // new referent
		bestValue = -10000.0;
		if (curRef != null) {
		    if (bestAntecedent != null) {
			mortClustering.addToClustering(curRef, bestAntecedent);
			System.out.println("merging: " + curRef.getString() + ":" + bestAntecedent.getString());
		    }
		    else {
			mortClustering.addToClustering(curRef);
			System.out.println("merging: " + curRef.getString() + ":NULL");
		    }
		}
		curRef = ref;
		if (edgeVal > bestValue) {
		    bestAntecedent = ant;
		    bestValue = edgeVal;
		} else
		    bestAntecedent = null;
	    } else {
		if (edgeVal > bestValue) {
		    /*
		    if ((bestAntecedent != null) && (ant != null)) {
			System.out.println(":: " + curRef.getString() + 
					   "-" + bestAntecedent.getString() + "(" + bestValue + ")" +
					   " to " + ant.getString() + "(" + edgeVal + ")");
					   }*/
		    bestAntecedent = ant;
		    bestValue = edgeVal;
		}
	    }
	}
	if (bestAntecedent != null) {
	    mortClustering.addToClustering(curRef, bestAntecedent);
	    System.out.println("merging: " + curRef.getString() + ":" + bestAntecedent.getString());
	}
	else {
	    mortClustering.addToClustering(curRef);
	    System.out.println("merging: " + curRef.getString() + ":NULL");
	}
	return mortClustering;
    }
		
    // this is gross to have to do this
    public static List getMentionsFromPairs (List pairs)
    {
	ArrayList mentions = new ArrayList();
	Iterator i = pairs.iterator();
	while (i.hasNext()) {
	    Instance inst = (Instance) i.next();
	    MentionPair pair = (MentionPair)inst.getSource();
	    Mention ant = pair.getAntecedent();
	    Mention ref = pair.getReferent();
	    if ((ant != null) && !mentions.contains(ant))
		mentions.add(ant);
	    if ((ref != null) && !mentions.contains(ref))
		mentions.add(ref);
	}
	return mentions;
    }

    public static void normalizeGraphEdges (MappedGraph graph)
    {
	Set edges = graph.getGraph().getEdgeSet();
	Iterator iter = edges.iterator();
	double highestEdge = 0.0;

	while (iter.hasNext()) {
	    WeightedEdge e = (WeightedEdge)iter.next();
	    double curWeightMag = Math.abs(e.getWeight());
	    if (curWeightMag > highestEdge)
		highestEdge = curWeightMag;
	}
	Iterator i2 = edges.iterator();
	while (i2.hasNext()) {
	    WeightedEdge e = (WeightedEdge)i2.next();
	    //System.out.println("Setting edge: " + (e.getWeight() / highestEdge));
	    e.setWeight ((double)(e.getWeight() / highestEdge));
	}
	
    }

    // go through pairs and if the have the same entity reference
    public static KeyClustering collectAllKeyClusters (List trainingMentionPairs)
    {
	Set allMentions = new LinkedHashSet();
	Iterator all = trainingMentionPairs.iterator();
	while (all.hasNext()) {
	    MentionPair p = (MentionPair)((Instance)all.next()).getSource();
	    Mention ant = p.getAntecedent();
	    Mention ref = p.getReferent();
	    if (ant != null)
		allMentions.add(ant);
	    if (ref != null)
		allMentions.add(ref);
	}
	KeyClustering keyClustering = new KeyClustering();
	Iterator i = trainingMentionPairs.iterator();
	while (i.hasNext()) {
	    Instance inst = (Instance)i.next();
	    MentionPair pair = (MentionPair)inst.getSource();
	    String entId = pair.getEntityReference();
	    Mention referent = pair.getReferent();
	    Mention antecedent = pair.getAntecedent();
	    if (entId != null) {
		if (antecedent != null) {
		    keyClustering.addToClustering(entId, antecedent);
		    allMentions.remove(antecedent);
		}
		if (referent != null) {
		    keyClustering.addToClustering(entId, referent);
		    allMentions.remove(referent);
		}
	    }
	}
	// add back all remaining clusters as singletons
	Iterator rem = allMentions.iterator();
	int singletonIds = 0;
	while (rem.hasNext()) {
	    String sid = new String("s").concat(new Integer(singletonIds).toString());
	    keyClustering.addToClustering(sid, (Mention)rem.next());
	    singletonIds++;
	}

	return keyClustering;
    }

    private static void coalesceNewPair (Set keyClusters, Instance inst)
    {
	
	if (inst.getLabeling().toString().equals("yes")) {
	    MentionPair pair = (MentionPair)inst.getSource();
	    //System.out.println("Current clusters: ");
	    //printClusters(keyClusters);
	    if (!pair.nullPair()) {
		Iterator i = keyClusters.iterator();
		boolean addedToExisting = false;
		while (i.hasNext()) {
		    Set cluster = (Set)i.next();
		    if ((cluster.contains(pair.getAntecedent())) ||
			(cluster.contains(pair.getReferent()))) {
			cluster.add (pair.getReferent());
			cluster.add (pair.getAntecedent());
			addedToExisting = true;
		    }
		}
		if (!addedToExisting) {
		    LinkedHashSet newS = new LinkedHashSet();
		    newS.add(pair.getAntecedent());
		    newS.add(pair.getReferent());
		    keyClusters.add(newS);
		}
	    } else {
		LinkedHashSet newS = new LinkedHashSet();
		MalletPhrase newPh = pair.getReferent().getMalletPhrase();
		newS.add (pair.getReferent());
		Iterator i = keyClusters.iterator();
		while (i.hasNext()) {
		    Set cluster = (Set)i.next();
			System.out.println("Creating " + pair.getReferent() + 
					   " when it already exists in " + cluster);
		}
		keyClusters.add(newS);
	    }
	}
    }

    private static void printClusters (Set clusters)
    {
	System.out.println("[[[");
	Iterator i = clusters.iterator();
	while (i.hasNext()) {
	    Object cl = i.next();
	    if (cl instanceof Set)
		printCluster((Set)cl);
	}
	System.out.println("]]]");
    }
    private static void printCluster (Set cluster)
    {
	System.out.print("(");
	Iterator i = cluster.iterator();
	while (i.hasNext()) {
	    Mention men = (Mention)i.next();
	    men.getMalletPhrase().printPreTerms();
	    System.out.print(" - " + men.getUniqueEntityIndex());
	    System.out.println("++" + men);
	}
	System.out.println(") ");
    }

    private static boolean referentPronoun (Mention referent)
    {
	String refString = referent.getString();
	for (int i=0; i < pronounsSize; i++) {
	    if (((String)pronouns[i]).equals(refString)) {
		return true;
	    }
	}
	return false;
    }

    private static boolean referentNNP (Mention referent)
    {
	MalletPreTerm ph = referent.getMalletPhrase().getHeadPreTerm();
	if ((ph.getPartOfSpeech() != null) && (ph.getPartOfSpeech().equals("NNP")))
	    return true;
	else
	    return false;
    }

    // the VotedPeception would be roughly the same but would pass in a
    // Matrix2 instead of a classifier and just do a dotproduct with feature vector
    // and subtract these to get the egde weight
    // use the rowDotProduct method
    private static void constructEdgesUsingModel (MappedGraph graph, MaxEnt cl, Instance pair)
    {
	MentionPair mentionPair = (MentionPair)pair.getSource(); // this needs to get stored in source
	Mention antecedent = mentionPair.getAntecedent();
	Mention referent =   mentionPair.getReferent();
	double  edgeVal = 0.0;
	//LabelVector labelVec = cl.classify(pair).getLabelVector();

	/*
	// this just uses positive label (always positive if this is a MaxEnt classifier)
	for (int i=0; i < labelVec.singleSize(); i++) {
	    if (labelVec.labelAtLocation(i).toString().equals("yes"))
		edgeVal = labelVec.valueAtLocation(i);
	}
	*/
	/*
	double posVal = 0.0;
	double negVal = 0.0;
	for (int i=0; i < labelVec.singleSize(); i++) {
	    if (labelVec.labelAtLocation(i).toString().equals("yes"))
		posVal = labelVec.valueAtLocation(i);
	    if (labelVec.labelAtLocation(i).toString().equals("no"))
		negVal = labelVec.valueAtLocation(i);
		}
	edgeVal = posVal - negVal;  // this will give us positive and negative values
	*/
	double [] rawParams = cl.getParameters();
	Matrix2 parameters = new Matrix2(rawParams,2,Array.getLength(rawParams)/2);
	edgeVal = (double)parameters.rowDotProduct(0,(FeatureVector)pair.getData())
	    - (double)parameters.rowDotProduct(1,(FeatureVector)pair.getData());
	
	if (mentionPair.nullPair()) {
	    if (referentPronoun (referent)) {
		try {
		    graph.addVertexMap (referent);
		} catch (Exception e) {e.printStackTrace();}
	    }
	}
	//else if ((referentPronoun(referent)) || (referentNNP(referent) && referentNNP(antecedent))) {
	//else if ((referentPronoun(referent))) {
	//else if (false) {
	else if (true) {
	    try {
		//System.out.println("Setting edge " + antecedent + "-" + referent +
		//	       " in graph to " + edgeVal);
		graph.addEdgeMap (antecedent, referent, edgeVal);
	    } catch (Exception e) {e.printStackTrace();}
	} else if (mentionPair.getEntityReference() != null) {
	    try {
		graph.addEdgeMap (antecedent, referent, 100.0);
	    } catch (Exception e) {e.printStackTrace();}
	} else
	    try {
		graph.addEdgeMap (antecedent, referent, -100.0);
	    } catch (Exception e) {e.printStackTrace();}
	
    }

    // construct edges using key labels
    private static void constructEdgesUsingTargets (MappedGraph graph, Instance pair)
    {
	MentionPair mentionPair = (MentionPair)pair.getSource(); // this needs to get stored in source
	//FeatureVector vec = (FeatureVector)pair.getData();
	//Iterator vIterator = graph.getGraph().getVerticesIterator();
	Mention antecedent = mentionPair.getAntecedent();
	Mention referent =   mentionPair.getReferent();
	

	// in this case, simply add the referent to graph with no edges
	if (mentionPair.nullPair()) {
	    try {
		graph.addVertexMap (referent);
	    } catch (Exception e) {e.printStackTrace();}
	    return;
	}

	if (mentionPair.getEntityReference() != null) {
	    try {
		graph.addEdgeMap (antecedent, referent, 1000.0);
	    } catch (Exception e) {e.printStackTrace();}
	}
	
	/*
	else {
	    try {
		graph.addEdgeMap (antecedent, referent, -1000.0);
	    } catch (Exception e) {e.printStackTrace();}
	    } */
    }

}
