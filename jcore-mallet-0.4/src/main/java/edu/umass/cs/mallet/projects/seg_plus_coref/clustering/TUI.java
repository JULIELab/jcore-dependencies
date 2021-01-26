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

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.Target2Label;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.types.FeatureVector;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.Matrix2;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class TUI
{

	static int yesIndex = 0;
	static int noIndex  = 1;
    public static final boolean QUANTIZE_EDGE_VALUES = false;

    public static void main (String[] args)
    {
	/*
	if (args.length != 2) {
	    System.err.println ("Usage: "+TUI.class.getName()+
				" <directory of MUC training> and <directory of MUC test");
	    System.exit(-1);
	    } */

	String trainingDataPath;
	String testDataPath;
	String sourceType = null;
	if (args.length != 3) {
	    // System.exit(-1);
	    //trainingDataPath = new String ("/odin.mitre.org/tmp/treebank/xml-bigger/train");
	    //testDataPath = new String ("/odin.mitre.org/tmp/treebank/xml-bigger/train");
	    //trainingDataPath = new String("c:/JavaDevel/data/toy");
	    //testDataPath = new String("c:/JavaDevel/data/toy");
	    trainingDataPath = new String("/usr/wod/tmp2/wellner/data/all-docs/training");
	    testDataPath = new String("/usr/wod/tmp2/wellner/data/all-docs/test-annotated");
		//trainingDataPath = "/usr/wod/tmp2/wellner/ace-data/bnews-xml/trn1";
		//testDataPath = "/usr/wod/tmp2/wellner/ace-data/bnews-xml/tst1";
		//trainingDataPath = "/tmp/wellner/xml-ready/med-train-2";
		//testDataPath = "/tmp/wellner/xml-ready/med-test-2";

	} else {
	    sourceType = args[0];
	    trainingDataPath = args[1];
	    testDataPath = args[2];
	}

	XMLFileFilter filter = new XMLFileFilter(".*xml");
	FileIterator fileIterator = new FileIterator (new File(trainingDataPath), 
						      (FileFilter)filter);
	FileIterator testFileIterator = new FileIterator (new File(testDataPath), 
							  (FileFilter)filter);

	ArrayList pairFilters = new ArrayList();
	if (sourceType.equals("MUC"))
	    pairFilters.add(new ProperNounFilterMUC());
	else
	    pairFilters.add(new ProperNounFilter());

	//FileIterator testFileIterator = new FileIterator (new File(args[1]));
	// This iterator takes an iterator over files, and iterates over all (relevant)
	// pairs of DOM nodes in each file

	MentionPairIterator pairIterator = new MentionPairIterator (fileIterator, sourceType, false, true, true, pairFilters);
	MentionPairIterator testPairIterator = new MentionPairIterator (testFileIterator, sourceType, false, true, true, pairFilters);

	// This pipeline takes individual pairs as input and produces a feature vector
	Pipe instancePipe = new SerialPipes (new Pipe[] {
	    new Target2Label(),
	    new AffixOfMentionPair (),
	    new AcronymOf (),
	    new AceTypeFeature (),
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
	InstanceList ilist = new InstanceList (instancePipe);
	ilist.add (pairIterator);


	//MaxEnt classifier = (MaxEnt)new MaxEntTrainer().train (ilist);
	//System.out.println ("Training Accuracy on \"yes\" = "+
	//	    new Trial (classifier, ilist).labelF1("yes"));
	//System.out.println ("Training Accuracy on \"no\" = "+
	//		    new Trial (classifier, ilist).labelF1("no"));

	System.out.println("About to partition training instances into associated doc sets");
	Set docInstances = MentionPairIterator.partitionIntoDocumentInstances(ilist);
	System.out.println("Number of docInstance sets: " + docInstances.size());


	InstanceList testList = new InstanceList (instancePipe);
	testList.add (testPairIterator);
	Set testDocs = MentionPairIterator.partitionIntoDocumentInstances(testList);
	/*	
	MaxEnt classifier = (MaxEnt)new MaxEntTrainer().train(ilist);
	
	LabelAlphabet labelVoc = classifier.getLabelAlphabet();
	for (int i=0; i < labelVoc.size(); i++) {
		if (labelVoc.lookupLabel(i).toString().equals("yes")) {
			yesIndex = i;
			System.out.println("Yes INDEX is: " + yesIndex);
		}
		if (labelVoc.lookupLabel(i).toString().equals("no")) {
			noIndex = i;
			System.out.println("No INDEX is: " + noIndex);
		}
	}
	System.out.println("Initializing ClusterLearner with parameters from BFGS");
	*/
	yesIndex = 0;
	noIndex = 1;
	
	//System.out.println("About to create cluster learner");
	// Classifier is now used to initialize vertex selection order - NOT THE LAMBDAS
	ClusterLearnerAvg learner = new ClusterLearnerAvg (200, docInstances,
							   instancePipe,
							   yesIndex, noIndex);

	Iterator iter1 = docInstances.iterator();
	Iterator iter2 = testDocs.iterator();

	//System.out.println("INITIAL performance on Training: ");
	//runTrainedModel (iter1, learner, instancePipe);

	//System.out.println("INITIAL performance on Testing: ");
	//runTrainedModel (iter2, learner, instancePipe);
	
	learner.startTrainingAvg(testDocs); // testDocs will be evaluated at each
	// epoch 
	learner.getFinalLambdas();
	System.out.println("Finished training...");

	Iterator i1 = docInstances.iterator();
	Iterator i2 = testDocs.iterator();

	System.out.println("TRAINING DATA");
	System.out.println("-------------------------------");
	runTrainedModel (i1, learner, instancePipe);
	
	System.out.println("TEST DATA");
	System.out.println("-------------------------------");
	runTrainedModel (i2, learner, instancePipe);
	
    }

	public static void runTrainedModel (Iterator iter1, ClusterLearner learner, Pipe instancePipe)
	{
	    double overallTestResults = 0.0;
	    double overallTestPairwise = 0.0;
	    Clusterer clusterer = new Clusterer();
	    int docIndex = 0;
	    while (iter1.hasNext()) {
	    docIndex++;
	    LinkedHashSet keyClusters = new LinkedHashSet();
	    MappedGraph graph = new MappedGraph(); // need a MappedGraph because we need to be able to copy
	    // Create the graph with all the correct edge weights, using the current (averaged?) lambdas
	    List testMentionPairs = (List)iter1.next();
	    KeyClustering keyClustering = TUIGraph.collectAllKeyClusters(testMentionPairs);
	    //keyClustering.print();
	    System.out.println("Number of pairs: " + testMentionPairs.size());
	    Iterator trPairIterator = testMentionPairs.iterator();
	    while (trPairIterator.hasNext()) {
				Instance mentionPair = (Instance)trPairIterator.next();
				//constructEdgesUsingTargets (graph, mentionPair);
				constructEdgesUsingTrainedClusterer (graph, mentionPair,
																						 learner.getFinalLambdas(),
																						 instancePipe);
				//coalesceNewPair (keyClusters, mentionPair);
	    }	    
	    clusterer.setGraph(graph);
	    Clustering clustering = clusterer.getClustering();
	    ClusterEvaluate eval1 = new ClusterEvaluate (keyClustering, clustering);
	    PairEvaluate pairEval2 = new PairEvaluate(keyClustering, clustering);
	    eval1.evaluate();
	    eval1.printErrors(true);
	    pairEval2.evaluate();
	    System.out.println("Cluster F1 using Model: " + eval1.getF1());
	    System.out.println("PairWise F1 using Model: " + pairEval2.getF1());
	    System.out.println("  -- recall " + pairEval2.getRecall());
	    System.out.println("  -- precision " + pairEval2.getPrecision());		
	    overallTestResults += eval1.getF1();
	    overallTestPairwise += pairEval2.getF1();

		}
		System.out.println("Overall Cluster F1: " + (overallTestResults / (double)docIndex));
		System.out.println("Overall PairWise F1: " + (overallTestPairwise / (double)docIndex));
	}

	public static void constructEdgesUsingTrainedClusterer (MappedGraph graph,
																														Instance instPair,
																														Matrix2 lambdas,
																													Pipe instancePipe)
	{
		
		MentionPair mentionPair = (MentionPair)instPair.getSource(); // this needs to get stored in source
		Mention antecedent = mentionPair.getAntecedent();
		Mention referent =   mentionPair.getReferent();
		double  edgeVal = 0.0;

		int defaultFeatureIndex = instancePipe.getDataAlphabet().size();
		double scores[] = new double[2];
		FeatureVector fv = (FeatureVector) instPair.getData ();
		assert (fv.getAlphabet ()
						== instancePipe.getDataAlphabet ());
		// Include the feature weights according to each label
		scores[yesIndex] = lambdas.value (yesIndex, defaultFeatureIndex)
											 + lambdas.rowDotProduct (yesIndex, fv,
																								defaultFeatureIndex,null);
		scores[noIndex] = lambdas.value (noIndex, defaultFeatureIndex)
											 + lambdas.rowDotProduct (noIndex, fv, defaultFeatureIndex,null);


		if (lambdas == null)
	    System.out.println("LAMBDAS NULL");
		edgeVal =  scores[yesIndex] - scores[noIndex];

		if (QUANTIZE_EDGE_VALUES) {
	    if (edgeVal >= 0)
				edgeVal = 1.0;
	    else
				edgeVal = -1.0;
		}
		try {
	    if (!mentionPair.nullPair())
				graph.addEdgeMap (antecedent, referent, edgeVal);
		} catch (Exception e) {e.printStackTrace();}

	}
}
