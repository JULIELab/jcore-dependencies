/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */




package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.AbstractStatisticalTokenDistance;
import com.wcohen.secondstring.Jaccard;
import com.wcohen.secondstring.StringDistance;
import com.wcohen.secondstring.TFIDF;
import com.wcohen.secondstring.tokens.NGramTokenizer;
import com.wcohen.secondstring.tokens.SimpleTokenizer;
import edu.umass.cs.mallet.base.fst.CRF;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.Target2Label;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.types.FeatureInducer;
import edu.umass.cs.mallet.base.types.InfoGain;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.RankedFeatureVector;
import edu.umass.cs.mallet.base.util.CommandOption;
import edu.umass.cs.mallet.base.util.MalletLogger;
import edu.umass.cs.mallet.base.util.RegexFileFilter;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.ClusterEvaluate;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.ConstrainedClusterer;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.PairEvaluate;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
//import edu.umass.cs.mallet.users.hay.canopy.Util;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructor;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructorSimple;
//import edu.umass.cs.mallet.users.hay.canopy.IndexFiles;
//import edu.umass.cs.mallet.users.hay.canopy.CanopyMaker;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructorAuthDateTitle;
//import salvo.jesus.graph.WeightedGraph;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.SimpleAnalyzer;

public class ClusterPapersAndVenuesUsingFeatures
{
	private static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "<NEWREFERENCE>"};

	private static CRF crf = null;
	private static Pipe pipe;
	private static IEInterface ieInterface;
	private static IEInterface ieInterface1;
	private static IEInterface ieInterface2;
	private static IEInterface ieInterface3;
	private static IEInterface ieInterface4;
	private static StringDistance softtfidf;
	private static StringDistance tfidf;
	private static Jaccard distanceMetricEditDist;
	private static StringDistance triGramDistanceMetric;

	static CommandOption.Boolean fullPartition = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "full-partition", "FILENAME", false, false,
	 "Use full partitioninig", null);

	static CommandOption.Boolean useWeightedAvg = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-weighted-avg", "FILENAME", false, false,
	 "Use weighted average", null);

	static CommandOption.String loadMEFile = new CommandOption.String
	(ClusterPapersGivenVenues.class, "load-me-file", "FILENAME", true, null,
	 "The name of the MaxEnt model file.", null);

	static CommandOption.String outputFile = new CommandOption.String
	(ClusterPapersGivenVenues.class, "output-file", "FILENAME", true, null,
	 "The name of the file where output clusters will be printed to.", null);

	static CommandOption.String crfInputFile = new CommandOption.String
	(ClusterPapersGivenVenues.class, "crf-input-file", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile1 = new CommandOption.String
	(ClusterPapersGivenVenues.class, "crf-input-file-1", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile2 = new CommandOption.String
	(ClusterPapersGivenVenues.class, "crf-input-file-2", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile3 = new CommandOption.String
	(ClusterPapersGivenVenues.class, "crf-input-file-3", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile4 = new CommandOption.String
	(ClusterPapersGivenVenues.class, "crf-input-file-4", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.Boolean useCRF = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-crf", "BOOL", false, false,
	 "Use CRF or not.", null);

	static CommandOption.Boolean useMultipleCRFs = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-multiple-crfs", "BOOL", false, false,
	 "Use a separate crf for each data segment or not.", null);

	static CommandOption.Boolean useTreeModel = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-tree-model", "BOOL", false, false,
	 "Use and train tree model.", null);

	static CommandOption.Boolean useCorrelational = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-correlational", "BOOL", false, false,
	 "Use Correlational Clustering or not, if not uses Greedy.", null);

	static CommandOption.Boolean useFeatureInduction = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-feature-induction", "BOOL", false, false,
	 "Use Feature Induction or Not.", null);

	static CommandOption.Boolean useNBest = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-n-best", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Boolean useTrueNumClusters = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-true-num-clusters", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Boolean useOptimal = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "use-optimal", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Integer optimalNBest = new CommandOption.Integer
	(ClusterPapersGivenVenues.class, "optimal-n-best", "INTEGER", true, -1,
	 "Size of n, for searching for optimal n-best configuration.", null);

	static CommandOption.Integer rBeamSize = new CommandOption.Integer
	(ClusterPapersGivenVenues.class, "r-beam-size", "INTEGER", true, 10,
	 "Size of n, for searching for optimal n-best configuration.", null);

	static CommandOption.String trainingDir1 = new CommandOption.String
	(ClusterPapersGivenVenues.class, "training-dir-1", "FILENAME", true, null,
	 "Directory containing training files.", null);

	static CommandOption.String trainingDir2 = new CommandOption.String
	(ClusterPapersGivenVenues.class, "training-dir-2", "FILENAME", true, null,
	 "Directory containing training files.", null);

	static CommandOption.String trainingDir3 = new CommandOption.String
	(ClusterPapersGivenVenues.class, "training-dir-3", "FILENAME", true, null,
	 "Directory containing training files.", null);
	
	static CommandOption.String testingDir = new CommandOption.String
	(ClusterPapersGivenVenues.class, "testing-dir", "FILENAME", true, null,
	 "Directory containing testing files.", null);

	static CommandOption.Integer searchIters = new CommandOption.Integer
	(ClusterPapersGivenVenues.class, "search-iters", "INTEGER", true, 3,
	 "Number of search iterations.", null);

	static CommandOption.Integer searchReductions = new CommandOption.Integer
	(ClusterPapersGivenVenues.class, "search-reductions", "INTEGER", true, 5,
	 "Number of search reductions.", null);

	static CommandOption.Integer numNBest = new CommandOption.Integer
	(ClusterPapersGivenVenues.class, "num-n-best", "INTEGER", true, 3,
	 "Number of n-best candidates to store.", null);

	static CommandOption.Integer nthViterbi = new CommandOption.Integer
	(ClusterPapersGivenVenues.class, "nth-viterbi", "INTEGER", true, 0,
	 "Number of n-best candidates to use .", null);

	static CommandOption.Boolean trainUsingLabeled = new CommandOption.Boolean
	(ClusterPapersGivenVenues.class, "train-using-labeled", "BOOL", true, false,
	 "Train just using the labeled data, but test on CRF output", null);


	static final CommandOption.List commandOptions = 
	new CommandOption.List (
		"Training, testing and running information extraction on paper header or reference.",
		new CommandOption[] {
			useWeightedAvg,
			trainUsingLabeled,
			rBeamSize,
			loadMEFile,
			useTreeModel,
			fullPartition,
			outputFile,
			useOptimal,
			crfInputFile,
			crfInputFile1,
			crfInputFile2,
			crfInputFile3,
			crfInputFile4,
			useCRF,
			useMultipleCRFs,
			useFeatureInduction,
			useCorrelational,			
			useNBest,
			optimalNBest,
			useTrueNumClusters,
			trainingDir1,
			trainingDir2,
			trainingDir3,			
			testingDir,
			searchIters,
			searchReductions,
			numNBest,
			nthViterbi
		});

	private static Logger logger = MalletLogger.getLogger (ClusterPapersGivenVenues.class.getName());
	
	/*
	// this method simply places each node (citation) in a publication object
	// this needs to be reworked when we consider how Publications and Citations
	// actually interact - i.e. are Publications nodes in the graph - or just ciations
	protected static ArrayList computePublications (ArrayList nodes) {
	ArrayList pubs = new ArrayList();
	for (int i=0; i<nodes.size(); i++) {
	pubs.add(new Publication ((Node)nodes.get(i)));
	}
	return pubs;
	}*/

	public static void main (String[] args) throws FileNotFoundException
	{

		commandOptions.process (args);
		commandOptions.logOptions (logger);

		boolean oldCluster = false;
		boolean newCluster = true;

		if (useCRF.value() == true) {
	    if (useMultipleCRFs.value() == true) {
				System.out.println("Initializing CRF");
				File crfFile1 = new File(crfInputFile1.value());
				ieInterface1 = new IEInterface(crfFile1);
				ieInterface1.loadCRF(crfFile1);
				File crfFile2 = new File(crfInputFile2.value());
				ieInterface2 = new IEInterface(crfFile2);
				ieInterface2.loadCRF(crfFile2);
				File crfFile3 = new File(crfInputFile3.value());
				ieInterface3 = new IEInterface(crfFile3);
				ieInterface3.loadCRF(crfFile3);
				File crfFile4 = new File(crfInputFile4.value());
				ieInterface4 = new IEInterface(crfFile4);
				ieInterface4.loadCRF(crfFile4);
	    } else {
				File crfFile = new File(crfInputFile.value());
				ieInterface = new IEInterface(crfFile);
				ieInterface.loadCRF(crfFile);
	    }
		}
		if (useNBest.value() == true) {
	    System.out.println("Using n-best CRF");
		}

		FileIterator trainFI_1 = null;
		FileIterator trainFI_2 = null;
		FileIterator trainFI_3 = null;
		if (useCRF.value() == true) {
	    trainFI_1 = new FileIterator (trainingDir1.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir2.value() != null)
				trainFI_2 = new FileIterator (trainingDir2.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir3.value() != null)
				trainFI_3 = new FileIterator (trainingDir3.value(), new RegexFileFilter(Pattern.compile(".*")));			
		}
		else {
	    trainFI_1 = new FileIterator (trainingDir1.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir2.value() != null)
				trainFI_2 = new FileIterator (trainingDir2.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir3.value() != null)
				trainFI_3 = new FileIterator (trainingDir3.value(), new RegexFileFilter(Pattern.compile(".*")));			
		}
		
		ArrayList trainFileArray1 = trainFI_1.getFileArray();
		ArrayList pubs1 = new ArrayList();
		System.out.println("Number of files 1: " + trainFileArray1.size());
		//ArrayList nodes1 = computeNodesWPubs(trainFileArray1, pubs1,
		//ieInterface1);
		ArrayList nodes1;
		if (useMultipleCRFs.value() == true) {
	    if (useTreeModel.value())
				nodes1 = CitationUtils.computeNodesWPubs(trainFileArray1, pubs1, ieInterface1, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    else
				nodes1 = CitationUtils.computeNodes(trainFileArray1, ieInterface1, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
		}																														
		else {
	    if (useTreeModel.value())
				nodes1 = CitationUtils.computeNodesWPubs(trainFileArray1, pubs1, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    else
				nodes1 = CitationUtils.computeNodes(trainFileArray1, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
		}
		ArrayList nodes2 = null;
		ArrayList nodes3 = null;
		ArrayList pubs2 = null;
		ArrayList pubs3 = null;

		if (trainFI_2 != null) {
	    ArrayList trainFileArray2 = trainFI_2.getFileArray();
	    pubs2 = new ArrayList ();
	    System.out.println("Number of files 2: " + trainFileArray2.size());
	    if (useMultipleCRFs.value() == true) {
				if (useTreeModel.value()) 
					nodes2 = CitationUtils.computeNodesWPubs(trainFileArray2, pubs2, ieInterface2, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes2 = CitationUtils.computeNodes(trainFileArray2, ieInterface2, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    }
	    else {
				if (useTreeModel.value())
					nodes2 = CitationUtils.computeNodesWPubs(trainFileArray2, pubs2, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes2 = CitationUtils.computeNodes(trainFileArray2, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    }
		}

		if (trainFI_3 != null) {
	    ArrayList trainFileArray3 = trainFI_3.getFileArray();
	    pubs3 = new ArrayList();
	    System.out.println("Number of files 3: " + trainFileArray3.size());
	    //nodes3 = computeNodesWPubs(trainFileArray3, pubs3, ieInterface3);
	    if (useMultipleCRFs.value() == true) {
				if (useTreeModel.value())
					nodes3 = CitationUtils.computeNodesWPubs(trainFileArray3, pubs3, ieInterface3, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes3 = CitationUtils.computeNodes(trainFileArray3, ieInterface3, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    }
	    else {
				if (useTreeModel.value())
					nodes3 = CitationUtils.computeNodesWPubs(trainFileArray3, pubs3, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes3 = CitationUtils.computeNodes(trainFileArray3, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    }
	    System.out.println(" There are " + nodes3.size() + " training nodes");
		}

		FileIterator testFI = null;
		if (useCRF.value() == true)
	    testFI = new FileIterator (testingDir.value(), new RegexFileFilter(Pattern.compile(".*")));
		else	
	    testFI = new FileIterator (testingDir.value(), new RegexFileFilter(Pattern.compile(".*")));
		
		ArrayList testFileArray = testFI.getFileArray();
		ArrayList testPubList = new ArrayList();

		ArrayList test_nodes;
		if (useMultipleCRFs.value() == true) {
	    test_nodes = CitationUtils.computeNodes(testFileArray,ieInterface4, false, numNBest.value(), nthViterbi.value());
		}
		else {
	    if (useTreeModel.value())
				test_nodes = CitationUtils.computeNodesWPubs(testFileArray, testPubList, ieInterface, useCRF.value(), numNBest.value(), nthViterbi.value());
	    else
				test_nodes = CitationUtils.computeNodes(testFileArray, ieInterface, useCRF.value(), numNBest.value(), nthViterbi.value());
		}

		//double testingCRFscore = scoreCitations (test_nodes);
		//ArrayList test_nodes = computeNodesWPubs(testFileArray, testPubList, ieInterface4);

		ArrayList allnodes = new ArrayList();  // all nodes, both training and
		// test
		
		allnodes.addAll(nodes1);
		if (nodes2 != null)
	    allnodes.addAll(nodes2);
		if (nodes3 != null)
	    allnodes.addAll(nodes3);
		//allnodes.addAll(test_nodes);

		//double trainingCRFscore = scoreCitations(allnodes);
		//System.out.println("CRF Score for Training Citations: " + trainingCRFscore);
		//System.out.println("CRF Score for Testing Citations: " + testingCRFscore);

		//make distance metrics
		tfidf = new TFIDF();
		NGramTokenizer nGramTokenizer =
	    new NGramTokenizer(3,3,false, new SimpleTokenizer(true, true));				
		triGramDistanceMetric = new TFIDF (nGramTokenizer);
		CitationUtils.makeDistMetric(allnodes, tfidf, triGramDistanceMetric);
		
		System.out.println("finished computing nodes, about to compute distanceMetric params ");
		// compute the string distance using SecondString utilities
		// this will serve as a useful feature
		// Possible extension (later): build different string metrics for
		// different fields - this will then be an array of them
		AbstractStatisticalTokenDistance distanceMetric =
	    (AbstractStatisticalTokenDistance)CitationUtils.computeDistanceMetric (allnodes);
		
		Pipe instancePipe = new SerialPipes (new Pipe[] {
	    new ExactFieldMatchPipe(Citation.corefFields),

	    new PageMatchPipe(),
	    new YearsWithinFivePipe(),
	    //new FieldStringDistancePipe(new NeedlemanWunsch(),
	    //Citation.corefFields, "EDIST"),
	    //new FieldStringDistancePipe(softtfidf, Citation.corefFields, "softTFIDF"),
	    new FieldStringDistancePipe(triGramDistanceMetric, Citation.corefFields, "trigramTFIDF"),
			
	    //new PlainFieldPipe (distanceMetric, distanceMetricEditDist),
	    new GlobalPipe(distanceMetric),
	    //new TitlePipe(distanceMetric),
	    new AuthorPipe(distanceMetric),
	    //new VenueClusterPipe(),
	    //new JournalPipe(distanceMetric),
	    //new PagesPipe(distanceMetric),
	    new HeuristicPipe(Citation.corefFields),
	    new InterFieldPipe(),
	    //new HeuristicPipe(Citation.corefFields),
	    //new DatePipe(distanceMetric),

	    //new FuchunPipe(distanceMetricEditDist),
			
	    new NodePair2FeatureVector (),
	    new Target2Label (),
		});

		InstanceList ilist = new InstanceList();
		if (loadMEFile.value() == null) {
	    InstanceList ilist1 = CitationUtils.makePairs(instancePipe, nodes1);
	    ilist.add(ilist1);
	    if (nodes2 != null) {
				InstanceList ilist2 = CitationUtils.makePairs(instancePipe, nodes2);
				ilist.add(ilist2);
	    }
	    if (nodes3 != null) {
				InstanceList ilist3 = CitationUtils.makePairs(instancePipe, nodes3);
				ilist.add(ilist3);
	    }
		}

		FeatureInducer fi = null;
		// try doing some feature induction now
		if (useFeatureInduction.value()) {
	    RankedFeatureVector.Factory gainFactory = null;
	    gainFactory = new InfoGain.Factory();
	    fi = new FeatureInducer (gainFactory,
															 ilist, 10);
	    fi.induceFeaturesFor(ilist, false, false);
		}
		TreeModel tmodel = null;
		if (useTreeModel.value()) {
	    if (pubs2 != null && pubs3 != null) {
				tmodel = new TreeModel(instancePipe, nodes1, nodes2, nodes3,
															 pubs1, pubs3, pubs3);
	    }
	    else {
				tmodel = new TreeModel(instancePipe, nodes1, pubs1);
	    }
	    //tmodel.setMultiTree (true);
		}

		//List pairsFromCanopy = Util.readPairsFromFile("/tmp/pairs");
		//InstanceList ilistToCluster = CitationUtils.makePairs(instancePipe, nodes, pairsFromCanopy);
		InstanceList itestlist = CitationUtils.makePairs(instancePipe, test_nodes);
		if (useFeatureInduction.value()) {
	    System.out.println("\n\nINDUCING FEATURES FOR TEST INSTANCES");
	    fi.induceFeaturesFor(itestlist, false, false);
		}

		CorefClusterAdv cl = null;
		//CorefClusterAdv cl_old = null;
		CorefClusterAdv cl_old = null;

		//training
		//CitationClustering cl = new CitationClustering();
		if (oldCluster) {
	    cl_old = new CorefClusterAdv(instancePipe);
	    cl_old.setTrueNumStop (useTrueNumClusters.value());
	    cl_old.train(ilist);
		}
		if (newCluster) {
	    cl = new CorefClusterAdv(instancePipe, tmodel);
	    cl.setTrueNumStop (useTrueNumClusters.value());
	    cl.setConfWeightedScores(useWeightedAvg.value());
	    cl.setOptimality (useOptimal.value());
	    cl.setRBeamSize (rBeamSize.value());
	    cl.setNBestInference (useNBest.value()); // actually use n-best list in
	    //coref
	    cl.setFullPartition(fullPartition.value());
	    int si = searchIters.value();
	    int	sd = searchReductions.value();
	    cl.setSearchParams (si, sd);
	    if (loadMEFile.value() != null)
				cl.loadME(loadMEFile.value());
	    else
				cl.train(ilist);
	    cl.testClassifier(itestlist);
		}

		Collection key = CitationUtils.makeCollections(allnodes); // make key collections
		//System.out.println("KEY: " + key);
		//System.out.println("NODES: " + nodes);
		Collection testKey = CitationUtils.makeCollections(test_nodes);
		Collection s = null;
		if (newCluster) {
	    //cl.setKeyPartitioning(key);
	    //s = cl.clusterMentions(ilist, allnodes, optimalNBest.value(), useCorrelational.value());
			System.out.println("Resulting clustering: " + s);
		}

		Collection c1 = null;
		if (oldCluster) {
			
	    cl_old.setKeyPartitioning(testKey);
	    c1 = cl_old.clusterMentions(ilist, allnodes, optimalNBest.value(), false);
	    if (newCluster) {
				System.out.println("Objective fn of KEY: " +
													 cl.evaluatePartitioningExternal(ilist, allnodes, key, optimalNBest.value()));
				System.out.println("Objective fn of GREEDY CLUSTERING: " +
													 cl.evaluatePartitioningExternal(ilist, allnodes, c1, optimalNBest.value()));
	    }
		}
		//			System.out.println("Objective fn of KEY w/optimal edges: " +
		//											 cl.evaluatePartitioningExternal(ilist, nodes, key, true));

		if (oldCluster) {
			
	    //			System.out.println("Objective fn of OLD CLUSTERING w/optimal edges: " +
	    //											 cl.evaluatePartitioningExternal(ilist, nodes, c1, true));
	    ClusterEvaluate eval1 = new ClusterEvaluate(key, c1);
	    eval1.evaluate();
	    System.out.println("Threshold Training Cluster F1: " + eval1.getF1());
	    System.out.println("Threshold Training Cluster Recall: " + eval1.getRecall());
	    System.out.println("Threshold Training Cluster Precision: " + eval1.getPrecision());
	    System.out.println("Number of clusters " + c1.size());
	    PairEvaluate p1 = new PairEvaluate (key, c1);
	    p1.evaluate();
	    System.out.println("Threshold Pair F1: " + p1.getF1());
	    System.out.println("Threshold Pair Recall: " + p1.getRecall());
	    System.out.println("Threshold Pair Precision: " + p1.getPrecision());

		}
			
		if (newCluster) {
	    if (s != null) {
				ClusterEvaluate eval = new ClusterEvaluate(key, s);
				eval.evaluate();
				PairEvaluate pairEval = new PairEvaluate(key, s);
				pairEval.evaluate();

				/*
					System.out.println("Objective fn of CORRELATIONAL CLUSTERING Training: " +
					cl.evaluatePartitioningExternal(ilist, allnodes, s, optimalNBest.value()));
				*/
				//eval.printVerbose();
				System.out.println("ObjFn Training Cluster F1: " + eval.getF1());
				System.out.println("ObjFn Training Cluster Recall: " + eval.getRecall());
				System.out.println("ObjFnTraining Cluster Precision: " + eval.getPrecision());
				System.out.println("Number of clusters " + s.size());
				System.out.println("ObjFn Pair F1: " + pairEval.getF1());
				System.out.println("ObjFn Pair Recall: " + pairEval.getRecall());
				System.out.println("ObjFn Pair Precision: " + pairEval.getPrecision());
	    }
		}
		
		cl.setKeyPartitioning (testKey);
		if (oldCluster) {
	    //evaluate on testing set
	    Collection testS_old = cl_old.clusterMentions(itestlist, test_nodes, -1, useCorrelational.value());
	    //Collection testS_old = cl_old.clusterMentions(itestlist, test_nodes, -1, false);
	    //Collection testS_old = cl_old.clusterMentions(itestlist, test_nodes);
			
	    ClusterEvaluate eval_t_old = new ClusterEvaluate(testKey, testS_old);
	    eval_t_old.evaluate();
	    if (newCluster) {
				
				
				System.out.println("Objective fn of OLD CLUSTERING: " +
													 cl.evaluatePartitioningExternal(itestlist, test_nodes, testS_old, optimalNBest.value()));
	    }
			
	    System.out.println("Threshold Testing Cluster F1: " + eval_t_old.getF1());
	    System.out.println("Threshold Testing Cluster Recall: " + eval_t_old.getRecall());
	    System.out.println("Threshold Testing Cluster Precision: " + eval_t_old.getPrecision());
	    System.out.println("Number of clusters " + testS_old.size());
	    PairEvaluate p_t_old = new PairEvaluate (testKey, testS_old);
	    p_t_old.evaluate();
	    System.out.println("Threshold Pair F1: " + p_t_old.getF1());
	    System.out.println("Threshold Pair Recall: " + p_t_old.getRecall());
	    System.out.println("Threshold Pair Precision: " + p_t_old.getPrecision());
		}
		if (newCluster) {
	    Collection testS = cl.clusterMentions(itestlist, test_nodes, -1, useCorrelational.value());
			System.out.println("TESTING Objective fn of KEY: " +
												 cl.evaluatePartitioningExternal(itestlist, test_nodes,
																												 testKey, optimalNBest.value()));
			
			System.out.println("TESTING Objective fn of CORRELATIONAL CLUSTERING Testing: " +
											 cl.evaluatePartitioningExternal(itestlist, test_nodes,
																											 testS, optimalNBest.value()));
			CitationUtils.evaluateClustering (testKey, testS, "ORIGINAL PAPER CLUSTERING");
 	    if (outputFile.value() != null)
				printClustersToFile (testS, outputFile.value());
			// CLUSTER VENUES
			Collection venueClusters = clusterVenues();
			// CONSTRAIN PAPER CLUSTER USING VENUE CLUSTERS
			ConstrainedClusterer cc = new ConstrainedClusterer (testS);
			Collection constrainedPaperClusters = cc.constrainByVenues (venueClusters);
			System.out.println ("RESULTS OF CONSTRAINED PAPER CLUSTERING:\n");
			CitationUtils.evaluateClustering (testKey, constrainedPaperClusters, "CONSTRAINED PAPER CLUSTERING");
		}			

		/*
			System.out.println("Final parameters used: ");
			double [] ps = cl.getClassifier().getParameters();
			for (int k=0; k < Array.getLength(ps); k++) {
			System.out.print(" " + ps[k]);
			}*/

	}

	private static void readCluster (File f) {
		
	}

	/** Cluster the venues from the same data and arguments specified to
	 * cluster papers.  Basically cut-and-paste from VenueCoreference*/
	protected static Collection clusterVenues () {
		System.err.println ("CLUSTERING VENUES\n");
		boolean oldCluster = false;
		boolean newCluster = true;
		if (useCRF.value() == true) {
	    if (useMultipleCRFs.value() == true) {
				System.out.println("Initializing CRF");
				File crfFile1 = new File(crfInputFile1.value());
				ieInterface1 = new IEInterface(crfFile1);
				ieInterface1.loadCRF(crfFile1);
				File crfFile2 = new File(crfInputFile2.value());
				ieInterface2 = new IEInterface(crfFile2);
				ieInterface2.loadCRF(crfFile2);
				File crfFile3 = new File(crfInputFile3.value());
				ieInterface3 = new IEInterface(crfFile3);
				ieInterface3.loadCRF(crfFile3);
				File crfFile4 = new File(crfInputFile4.value());
				ieInterface4 = new IEInterface(crfFile4);
				ieInterface4.loadCRF(crfFile4);
	    } else {
				File crfFile = new File(crfInputFile.value());
				ieInterface = new IEInterface(crfFile);
				ieInterface.loadCRF(crfFile);
	    }
		}
		if (useNBest.value() == true) {
	    System.out.println("Using n-best CRF");
		}

		FileIterator trainFI_1 = null;
		FileIterator trainFI_2 = null;
		FileIterator trainFI_3 = null;
		if (useCRF.value() == true) {
	    trainFI_1 = new FileIterator (trainingDir1.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir2.value() != null)
				trainFI_2 = new FileIterator (trainingDir2.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir3.value() != null)
				trainFI_3 = new FileIterator (trainingDir3.value(), new RegexFileFilter(Pattern.compile(".*")));			
		}
		else {
	    trainFI_1 = new FileIterator (trainingDir1.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir2.value() != null)
				trainFI_2 = new FileIterator (trainingDir2.value(), new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir3.value() != null)
				trainFI_3 = new FileIterator (trainingDir3.value(), new RegexFileFilter(Pattern.compile(".*")));			
		}
		
		ArrayList trainFileArray1 = trainFI_1.getFileArray();
		ArrayList pubs1 = new ArrayList();
		System.out.println("Number of files 1: " + trainFileArray1.size());
		//ArrayList nodes1 = computeNodesWPubs(trainFileArray1, pubs1,
		//ieInterface1);
		ArrayList nodes1;
		if (useMultipleCRFs.value() == true) {
	    if (useTreeModel.value())
				nodes1 = CitationUtils.computeNodesWPubs(trainFileArray1, pubs1, ieInterface1, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    else
				nodes1 = CitationUtils.computeNodes(trainFileArray1, ieInterface1, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
		}																														
		else {
	    if (useTreeModel.value())
				nodes1 = CitationUtils.computeNodesWPubs(trainFileArray1, pubs1, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
	    else
				nodes1 = CitationUtils.computeNodes(trainFileArray1, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
		}
		ArrayList nodes2 = null;
		ArrayList nodes3 = null;
		ArrayList pubs2 = null;
		ArrayList pubs3 = null;

		if (trainFI_2 != null) {
	    ArrayList trainFileArray2 = trainFI_2.getFileArray();
	    pubs2 = new ArrayList ();
	    System.out.println("Number of files 2: " + trainFileArray2.size());
	    if (useMultipleCRFs.value() == true) {
				if (useTreeModel.value()) 
					nodes2 = CitationUtils.computeNodesWPubs(trainFileArray2, pubs2, ieInterface2, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes2 = CitationUtils.computeNodes(trainFileArray2, ieInterface2, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
	    }
	    else {
				if (useTreeModel.value())
					nodes2 = CitationUtils.computeNodesWPubs(trainFileArray2, pubs2, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes2 = CitationUtils.computeNodes(trainFileArray2, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
	    }
		}

		if (trainFI_3 != null) {
	    ArrayList trainFileArray3 = trainFI_3.getFileArray();
	    pubs3 = new ArrayList();
	    System.out.println("Number of files 3: " + trainFileArray3.size());
	    //nodes3 = computeNodesWPubs(trainFileArray3, pubs3, ieInterface3);
	    if (useMultipleCRFs.value() == true) {
				if (useTreeModel.value())
					nodes3 = CitationUtils.computeNodesWPubs(trainFileArray3, pubs3, ieInterface3, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes3 = CitationUtils.computeNodes(trainFileArray3, ieInterface3, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
	    }
	    else {
				if (useTreeModel.value())
					nodes3 = CitationUtils.computeNodesWPubs(trainFileArray3, pubs3, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value());
				else
					nodes3 = CitationUtils.computeNodes(trainFileArray3, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
	    }
	    System.out.println(" There are " + nodes3.size() + " training nodes");
		}

		FileIterator testFI = null;
		if (useCRF.value() == true)
	    testFI = new FileIterator (testingDir.value(), new RegexFileFilter(Pattern.compile(".*")));
		else	
	    testFI = new FileIterator (testingDir.value(), new RegexFileFilter(Pattern.compile(".*")));
		
		ArrayList testFileArray = testFI.getFileArray();
		ArrayList testPubList = new ArrayList();

		ArrayList test_nodes;
		if (useMultipleCRFs.value() == true) {
	    test_nodes = CitationUtils.computeNodes(testFileArray,ieInterface4, false, numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
		}
		else {
	    if (useTreeModel.value())
				test_nodes = CitationUtils.computeNodesWPubs(testFileArray, testPubList, ieInterface, useCRF.value(), numNBest.value(), nthViterbi.value());
	    else
				test_nodes = CitationUtils.computeNodes(testFileArray, ieInterface, useCRF.value(), numNBest.value(), nthViterbi.value(), CitationUtils.VENUE);
		}

		ArrayList allnodes = new ArrayList();  // all nodes, both training and
		// test
		
		allnodes.addAll(nodes1);
		if (nodes2 != null)
	    allnodes.addAll(nodes2);
		if (nodes3 != null)
	    allnodes.addAll(nodes3);

		//make distance metrics
		tfidf = new TFIDF();
		NGramTokenizer nGramTokenizer =
	    new NGramTokenizer(3,3,false, new SimpleTokenizer(true, true));				
		triGramDistanceMetric = new TFIDF (nGramTokenizer);
		CitationUtils.makeDistMetric(allnodes, tfidf, triGramDistanceMetric);
		
		System.out.println("finished computing nodes, about to compute distanceMetric params ");
		// compute the string distance using SecondString utilities
		// this will serve as a useful feature
		// Possible extension (later): build different string metrics for
		// different fields - this will then be an array of them
		AbstractStatisticalTokenDistance distanceMetric =
	    (AbstractStatisticalTokenDistance)CitationUtils.computeDistanceMetric (allnodes);
		
		Pipe instancePipe = new SerialPipes (new Pipe[] {
	    new ExactFieldMatchPipe(Citation.corefFields),

	    new PageMatchPipe(),
	    new YearsWithinFivePipe(),
	    //new FieldStringDistancePipe(new NeedlemanWunsch(),
	    //Citation.corefFields, "EDIST"),
	    //new FieldStringDistancePipe(softtfidf, Citation.corefFields, "softTFIDF"),
	    new FieldStringDistancePipe(triGramDistanceMetric, Citation.corefFields, "trigramTFIDF"),
			
	    //new PlainFieldPipe (distanceMetric, distanceMetricEditDist),
	    new GlobalPipe(distanceMetric),
	    //new TitlePipe(distanceMetric),
			new AuthorPipe(distanceMetric),
	    //new JournalPipe(distanceMetric),
			///new BooktitlePipe(distanceMetric),			
			new VenuePipe(distanceMetric),
			new VenueAcronymPipe(),
	    //new PagesPipe(distanceMetric),
	    new HeuristicPipe(Citation.corefFields),
	    new InterFieldPipe(),
	    //new HeuristicPipe(Citation.corefFields),
	    //new DatePipe(distanceMetric),

	    //new FuchunPipe(distanceMetricEditDist),
	    new NodePair2FeatureVector (),
	    new Target2Label (),
			//	new PrintInputAndTarget (),
		});

		InstanceList ilist = new InstanceList();
		if (loadMEFile.value() == null) {
	    InstanceList ilist1 = CitationUtils.makePairs(instancePipe, nodes1);
	    ilist.add(ilist1);
	    if (nodes2 != null) {
				InstanceList ilist2 = CitationUtils.makePairs(instancePipe, nodes2);
				ilist.add(ilist2);
	    }
	    if (nodes3 != null) {
				InstanceList ilist3 = CitationUtils.makePairs(instancePipe, nodes3);
				ilist.add(ilist3);
	    }
		}

		FeatureInducer fi = null;
		// try doing some feature induction now
		if (useFeatureInduction.value()) {
	    RankedFeatureVector.Factory gainFactory = null;
	    gainFactory = new InfoGain.Factory();
	    fi = new FeatureInducer (gainFactory,
															 ilist, 10);
	    fi.induceFeaturesFor(ilist, false, false);
		}
		TreeModel tmodel = null;
		if (useTreeModel.value()) {
	    if (pubs2 != null && pubs3 != null) {
				tmodel = new TreeModel(instancePipe, nodes1, nodes2, nodes3,
															 pubs1, pubs3, pubs3);
	    }
	    else {
				tmodel = new TreeModel(instancePipe, nodes1, pubs1);
	    }
	    //tmodel.setMultiTree (true);
		}

		//List pairsFromCanopy = Util.readPairsFromFile("/tmp/pairs");
		//InstanceList ilistToCluster = CitationUtils.makePairs(instancePipe, nodes, pairsFromCanopy);
		InstanceList itestlist = CitationUtils.makePairs(instancePipe, test_nodes);
		if (useFeatureInduction.value()) {
	    System.out.println("\n\nINDUCING FEATURES FOR TEST INSTANCES");
	    fi.induceFeaturesFor(itestlist, false, false);
		}

		CorefClusterAdv cl = null;
		//CorefClusterAdv cl_old = null;
		CorefClusterAdv cl_old = null;

		//training
		//CitationClustering cl = new CitationClustering();
		if (oldCluster) {
	    cl_old = new CorefClusterAdv(instancePipe);
	    cl_old.setTrueNumStop (useTrueNumClusters.value());
	    cl_old.train(ilist);
		}
		if (newCluster) {
	    cl = new CorefClusterAdv(instancePipe, tmodel);
	    cl.setTrueNumStop (useTrueNumClusters.value());
	    cl.setConfWeightedScores(useWeightedAvg.value());
	    cl.setOptimality (useOptimal.value());
	    cl.setRBeamSize (rBeamSize.value());
	    cl.setNBestInference (useNBest.value()); // actually use n-best list in
	    //coref
	    cl.setFullPartition(fullPartition.value());
	    int si = searchIters.value();
	    int	sd = searchReductions.value();
	    cl.setSearchParams (si, sd);
	    if (loadMEFile.value() != null)
				cl.loadME(loadMEFile.value());
	    else
				cl.train(ilist);
	    cl.testClassifier(itestlist);
		}

		Collection key = CitationUtils.makeCollections(allnodes); // make key collections
		//System.out.println("KEY: " + key);
		//System.out.println("NODES: " + nodes);
		Collection testKey = CitationUtils.makeCollections(test_nodes);
		Collection s = null;
		Collection c1 = null;
		if (oldCluster) {
			
	    cl_old.setKeyPartitioning(testKey);
	    c1 = cl_old.clusterMentions(ilist, allnodes, optimalNBest.value(), false);
	    if (newCluster) {
				System.out.println("Objective fn of KEY: " +
													 cl.evaluatePartitioningExternal(ilist, allnodes, key, optimalNBest.value()));
				System.out.println("Objective fn of GREEDY CLUSTERING: " +
													 cl.evaluatePartitioningExternal(ilist, allnodes, c1, optimalNBest.value()));
	    }
		}
		//			System.out.println("Objective fn of KEY w/optimal edges: " +
		//											 cl.evaluatePartitioningExternal(ilist, nodes, key, true));

		if (oldCluster) {
			
	    //			System.out.println("Objective fn of OLD CLUSTERING w/optimal edges: " +
	    //											 cl.evaluatePartitioningExternal(ilist, nodes, c1, true));
	    ClusterEvaluate eval1 = new ClusterEvaluate(key, c1);
	    eval1.evaluate();
	    System.out.println("Threshold Training Cluster F1: " + eval1.getF1());
	    System.out.println("Threshold Training Cluster Recall: " + eval1.getRecall());
	    System.out.println("Threshold Training Cluster Precision: " + eval1.getPrecision());
	    System.out.println("Number of clusters " + c1.size());
	    PairEvaluate p1 = new PairEvaluate (key, c1);
	    p1.evaluate();
	    System.out.println("Threshold Pair F1: " + p1.getF1());
	    System.out.println("Threshold Pair Recall: " + p1.getRecall());
	    System.out.println("Threshold Pair Precision: " + p1.getPrecision());

		}
			
		if (newCluster) {
	    if (s != null) {
				ClusterEvaluate eval = new ClusterEvaluate(key, s);
				eval.evaluate();
				PairEvaluate pairEval = new PairEvaluate(key, s);
				pairEval.evaluate();

				/*
					System.out.println("Objective fn of CORRELATIONAL CLUSTERING Training: " +
					cl.evaluatePartitioningExternal(ilist, allnodes, s, optimalNBest.value()));
				*/
				//eval.printVerbose();
				System.out.println("ObjFn Training Cluster F1: " + eval.getF1());
				System.out.println("ObjFn Training Cluster Recall: " + eval.getRecall());
				System.out.println("ObjFnTraining Cluster Precision: " + eval.getPrecision());
				System.out.println("Number of clusters " + s.size());
				System.out.println("ObjFn Pair F1: " + pairEval.getF1());
				System.out.println("ObjFn Pair Recall: " + pairEval.getRecall());
				System.out.println("ObjFn Pair Precision: " + pairEval.getPrecision());
	    }
		}
		
		cl.setKeyPartitioning (testKey);
		if (oldCluster) {
	    //evaluate on testing set
	    Collection testS_old = cl_old.clusterMentions(itestlist, test_nodes, -1, useCorrelational.value());
	    //Collection testS_old = cl_old.clusterMentions(itestlist, test_nodes, -1, false);
	    //Collection testS_old = cl_old.clusterMentions(itestlist, test_nodes);

	    ClusterEvaluate eval_t_old = new ClusterEvaluate(testKey, testS_old);
	    eval_t_old.evaluate();
	    if (newCluster) {

				
				System.out.println("Objective fn of OLD CLUSTERING: " +
													 cl.evaluatePartitioningExternal(itestlist, test_nodes, testS_old, optimalNBest.value()));
	    }

	    System.out.println("Threshold Testing Cluster F1: " + eval_t_old.getF1());
	    System.out.println("Threshold Testing Cluster Recall: " + eval_t_old.getRecall());
	    System.out.println("Threshold Testing Cluster Precision: " + eval_t_old.getPrecision());
	    System.out.println("Number of clusters " + testS_old.size());
	    PairEvaluate p_t_old = new PairEvaluate (testKey, testS_old);
	    p_t_old.evaluate();
	    System.out.println("Threshold Pair F1: " + p_t_old.getF1());
	    System.out.println("Threshold Pair Recall: " + p_t_old.getRecall());
	    System.out.println("Threshold Pair Precision: " + p_t_old.getPrecision());
		}
		if (newCluster) {
	    Collection testS = cl.clusterMentions(itestlist, test_nodes, -1, useCorrelational.value());
	    ClusterEvaluate evalTest = new ClusterEvaluate(testKey, testS);
	    evalTest.evaluate();
	    evalTest.printVerbose();
	    PairEvaluate pairEvalTest = new PairEvaluate(testKey, testS);
	    pairEvalTest.evaluate();

	    System.out.println("TESTING Objective fn of KEY: " +
												 cl.evaluatePartitioningExternal(itestlist, test_nodes, testKey, optimalNBest.value()));

	    System.out.println("TESTING Objective fn of CORRELATIONAL CLUSTERING Testing: " +
												 cl.evaluatePartitioningExternal(itestlist, test_nodes, testS, optimalNBest.value()));
	    //cl.exportGraph("/tmp/testGraphEdges");
	    //eval.printVerbose();
	    System.out.println("TESTING ObjFn Cluster F1: " + evalTest.getF1());
	    System.out.println("TESTING ObjFn Cluster Recall: " + evalTest.getRecall());
	    System.out.println("TESTING ObjFn Cluster Precision: " + evalTest.getPrecision());
	    System.out.println("Number of clusters " + testS.size());
	    System.out.println("TESTING ObjFn Pair F1: " + pairEvalTest.getF1());
	    System.out.println("TESTING ObjFn Pair Recall: " + pairEvalTest.getRecall());
	    System.out.println("TESTING ObjFn Pair Precision: " + pairEvalTest.getPrecision());
	    if (outputFile.value() != null)
				printClustersToFile (testS, outputFile.value());
			return testS;
		}			
		return null;
	}

	protected static void printClustersToFile (Collection citations, String file) {
		try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(file));
	    printClustersAsReceived (citations, out);
	    out.close();
		} catch (Exception e) {e.printStackTrace();}

	}

	protected static void printClustersAsReceived (Collection citations, BufferedWriter out) {

		int refNum = 1;
		int clNum = 1;
		for (Iterator it = citations.iterator(); it.hasNext();) {
	    Collection cl = (Collection)it.next();
	    for (Iterator i2 = cl.iterator(); i2.hasNext(); ) {
				Citation c = (Citation)i2.next();
				String lab = (String)c.getLabel();
				try {
					out.write("<NEWREFERENCE>\n");
					out.write("<meta reference_no=\"" + refNum +
										"\" cluster_no=\"" + clNum + "\" true_id=\"" + lab + "\"></meta>");
					out.write(c.getOrigString());
				} catch (Exception e) {}
				refNum++;
	    }
	    clNum++;
		}
	}

	protected static void printCollectionReferences (Collection collection) {
		Iterator i1 = collection.iterator();
		while (i1.hasNext()) {
	    Iterator i2 = ((Collection)i1.next()).iterator();
	    while (i2.hasNext()) {
				Object o = i2.next();
				if (o instanceof Node) {
					Node n = (Node)o;
					System.out.println("Node: " + n);
					System.out.println("Node label: " + n.getLabel());
					System.out.println("Node index: " + n.getIndex());
				} else {
					System.out.println("Node: " + o);
				}
	    }
		}
	}

	public static double scoreCitations(List citations) {

		double score = 0.0;
		for (Iterator i = citations.iterator(); i.hasNext(); ) {
	    score += (double)((Citation)i.next()).getScore();
		}
		return score/(double)citations.size();
	}

	/*
		This method will create a collection of collections from the citation nodes
	*/
	/*
		protected static Collection makeCollections (ArrayList nodes) {

		HashMap map = new HashMap(); // keep an index of node label values to collections
		Collection collection = new LinkedHashSet();
		for (int i=0; i<nodes.size(); i++) {
		Node n = (Node)nodes.get(i);
		Object o1 = n.getLabel();
		Collection c = (Collection)map.get(o1);
		if (c != null) {
		c.add(n);
		//System.out.println("adding new node " + n + " to existing collection");
		} else {
		Collection newC = new LinkedHashSet();
		System.out.println("Creating new collection");
		newC.add(n);
		map.put(o1, newC);
		}
		}
		Iterator i1 = map.values().iterator();
		while (i1.hasNext()) {
		collection.add((Collection)i1.next());
		}
		return collection;
		}*/




	/*
		protected static List runCanopies(List files) throws Exception {
		double loose = 0.3;
		double tight = 0.7;
		String indexName = "/tmp/index";

		Analyzer analyzer = new SimpleAnalyzer();
		//Analyzer analyzer = new NGramAnalyzer();
		//Analyzer analyzer = new TriGramAnalyzer();
		//QueryConstructor queryConstructor = new QueryConstructorSimple(analyzer);
		QueryConstructor queryConstructor = new QueryConstructorAuthDateTitle(analyzer);

		IndexFiles.indexFiles(files, indexName, analyzer);
		CanopyMaker cm = new CanopyMaker(indexName, queryConstructor);
		cm.setLooseThreshold(loose);
		cm.setTightThreshold(tight);
		cm.makeCanopies();

		Util.allScores(cm);
		return Util.getUniquePairsFromSets(Util.convertIds(cm.getCanopies(), cm.getDocIdToDocno()));
		}
	*/

}
