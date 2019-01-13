/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */




package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.fst.*;
import edu.umass.cs.mallet.base.util.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.pipe.tsf.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.lang.reflect.Array;

import com.wcohen.secondstring.*;
import com.wcohen.secondstring.tokens.NGramTokenizer;
import com.wcohen.secondstring.tokens.SimpleTokenizer;

import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.ExactFieldMatchPipe;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.PageMatchPipe;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.YearsWithinFivePipe;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.FieldStringDistancePipe;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEEvaluator;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.CRFIO;
//import edu.umass.cs.mallet.users.hay.canopy.Util;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructor;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructorSimple;
//import edu.umass.cs.mallet.users.hay.canopy.IndexFiles;
//import edu.umass.cs.mallet.users.hay.canopy.CanopyMaker;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructorAuthDateTitle;
//import salvo.jesus.graph.WeightedGraph;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.SimpleAnalyzer;

/** Clusters papers and venues jointly using MultipleCorefCluterer */
public class ClusterPapersAndVenues
{
	private static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "<NEWREFERENCE>"};

	private static CRF crf = null;
	private static Pipe pipe;
	private static IEInterface ieInterface;
	private static IEInterface ieInterface1;
	private static IEInterface ieInterface2;
	private static IEInterface ieInterface3;
	private static IEInterface ieInterface4;
	private static StringDistance softtfidfPaper;
	private static StringDistance tfidfPaper;
	private static StringDistance tfidfVenue;
	private static Jaccard distanceMetricEditDistPaper;
	private static StringDistance triGramDistanceMetricPaper;
	private static StringDistance triGramDistanceMetricVenue;

	static CommandOption.Boolean fullPartition = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "full-partition", "FILENAME", false, false,
	 "Use full partitioninig", null);

	static CommandOption.Boolean useWeightedAvg = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-weighted-avg", "FILENAME", false, false,
	 "Use weighted average", null);

	static CommandOption.String loadMEFile = new CommandOption.String
	(ClusterPapersAndVenues.class, "load-me-file", "FILENAME", true, null,
	 "The name of the MaxEnt model file.", null);

	static CommandOption.String outputFile = new CommandOption.String
	(ClusterPapersAndVenues.class, "output-file", "FILENAME", true, null,
	 "The name of the file where output clusters will be printed to.", null);

	static CommandOption.String crfInputFile = new CommandOption.String
	(ClusterPapersAndVenues.class, "crf-input-file", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile1 = new CommandOption.String
	(ClusterPapersAndVenues.class, "crf-input-file-1", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile2 = new CommandOption.String
	(ClusterPapersAndVenues.class, "crf-input-file-2", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile3 = new CommandOption.String
	(ClusterPapersAndVenues.class, "crf-input-file-3", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile4 = new CommandOption.String
	(ClusterPapersAndVenues.class, "crf-input-file-4", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.Boolean useCRF = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-crf", "BOOL", false, false,
	 "Use CRF or not.", null);

	static CommandOption.Boolean useMultipleCRFs = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-multiple-crfs", "BOOL", false, false,
	 "Use a separate crf for each data segment or not.", null);

	static CommandOption.Boolean useTreeModel = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-tree-model", "BOOL", false, false,
	 "Use and train tree model.", null);

	static CommandOption.Boolean useCorrelational = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-correlational", "BOOL", false, false,
	 "Use Correlational Clustering or not, if not uses Greedy.", null);

	static CommandOption.Boolean useFeatureInduction = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-feature-induction", "BOOL", false, false,
	 "Use Feature Induction or Not.", null);

	static CommandOption.Boolean useNBest = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-n-best", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Boolean useTrueNumClusters = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-true-num-clusters", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Boolean useOptimal = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "use-optimal", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Integer optimalNBest = new CommandOption.Integer
	(ClusterPapersAndVenues.class, "optimal-n-best", "INTEGER", true, -1,
	 "Size of n, for searching for optimal n-best configuration.", null);

	static CommandOption.Integer rBeamSize = new CommandOption.Integer
	(ClusterPapersAndVenues.class, "r-beam-size", "INTEGER", true, 10,
	 "Size of n, for searching for optimal n-best configuration.", null);

	static CommandOption.String trainingDir1 = new CommandOption.String
	(ClusterPapersAndVenues.class, "training-dir-1", "FILENAME", true, null,
	 "Directory containing training files.", null);

	static CommandOption.String trainingDir2 = new CommandOption.String
	(ClusterPapersAndVenues.class, "training-dir-2", "FILENAME", true, null,
	 "Directory containing training files.", null);

	static CommandOption.String trainingDir3 = new CommandOption.String
	(ClusterPapersAndVenues.class, "training-dir-3", "FILENAME", true, null,
	 "Directory containing training files.", null);
	
	static CommandOption.String testingDir = new CommandOption.String
	(ClusterPapersAndVenues.class, "testing-dir", "FILENAME", true, null,
	 "Directory containing testing files.", null);

	static CommandOption.Integer searchIters = new CommandOption.Integer
	(ClusterPapersAndVenues.class, "search-iters", "INTEGER", true, 3,
	 "Number of search iterations.", null);

	static CommandOption.Integer searchReductions = new CommandOption.Integer
	(ClusterPapersAndVenues.class, "search-reductions", "INTEGER", true, 5,
	 "Number of search reductions.", null);

	static CommandOption.Integer numNBest = new CommandOption.Integer
	(ClusterPapersAndVenues.class, "num-n-best", "INTEGER", true, 3,
	 "Number of n-best candidates to store.", null);

	static CommandOption.Integer nthViterbi = new CommandOption.Integer
	(ClusterPapersAndVenues.class, "nth-viterbi", "INTEGER", true, 0,
	 "Number of n-best candidates to use .", null);

	static CommandOption.Boolean trainUsingLabeled = new CommandOption.Boolean
	(ClusterPapersAndVenues.class, "train-using-labeled", "BOOL", true, false,
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

	private static Logger logger = MalletLogger.getLogger (ClusterPapersAndVenues.class.getName());
	

	public static void main (String[] args) throws FileNotFoundException
	{
		commandOptions.process (args);
		commandOptions.logOptions (logger);
		boolean newCluster = true;
		loadCRFs();

		// create paper nodes
		logger.info ("Creating Paper Nodes");
		ArrayList[] paperTrainingNodes = createNodesFromTraining (CitationUtils.PAPER);
		ArrayList paperTestingNodes = createNodesFromTesting (CitationUtils.PAPER);
		ArrayList allPaperTrainingNodes = new ArrayList ();
		for (int i=0; i < paperTrainingNodes.length; i++)
			allPaperTrainingNodes.addAll (paperTrainingNodes[i]);		
		System.out.println("finished computing nodes for PAPER, about to compute distanceMetric params ");
		triGramDistanceMetricPaper = getDistanceMetric (allPaperTrainingNodes);
		AbstractStatisticalTokenDistance distanceMetricPaper =
	    (AbstractStatisticalTokenDistance)CitationUtils.computeDistanceMetric (allPaperTrainingNodes);
		Pipe paperPipe = getPaperPipe(distanceMetricPaper, triGramDistanceMetricPaper);
		InstanceList paperTraining = getTrainingList (paperTrainingNodes, paperPipe);		
		InstanceList paperTesting = CitationUtils.makePairs(paperPipe, paperTestingNodes);
		//Collection paperKey = CitationUtils.makeCollections(allPaperTrainingNodes); // make key collections
		Collection paperTestKey = CitationUtils.makeCollections(paperTestingNodes);

		// create venue nodes
		logger.info ("Creating Venue Nodes");
		ArrayList[] venueTrainingNodes = createNodesFromTraining (CitationUtils.VENUE);
		ArrayList venueTestingNodes = createNodesFromTesting (CitationUtils.VENUE);
		ArrayList allVenueTrainingNodes = new ArrayList ();
		for (int i=0; i < venueTrainingNodes.length; i++)
			allVenueTrainingNodes.addAll (venueTrainingNodes[i]);		
		System.out.println("finished computing nodes for VENUE, about to compute distanceMetric params ");
		triGramDistanceMetricVenue = getDistanceMetric (allVenueTrainingNodes);
		AbstractStatisticalTokenDistance distanceMetricVenue =
	    (AbstractStatisticalTokenDistance)CitationUtils.computeDistanceMetric (allVenueTrainingNodes);		
		Pipe venuePipe = getVenuePipe(distanceMetricVenue, triGramDistanceMetricVenue);
		InstanceList venueTraining = getTrainingList (venueTrainingNodes, venuePipe);		
		InstanceList venueTesting = CitationUtils.makePairs(venuePipe, venueTestingNodes);
		//Collection venueKey = CitationUtils.makeCollections(allVenueTrainingNodes); // make key collections
		Collection venueTestKey = CitationUtils.makeCollections(venueTestingNodes);
				
		
		FeatureInducer fi = null;
		/*
		// try doing some feature induction now
		if (useFeatureInduction.value()) {
	    RankedFeatureVector.Factory gainFactory = null;
	    gainFactory = new InfoGain.Factory();
	    fi = new FeatureInducer (gainFactory,
															 paperTraining, 10);
	    fi.induceFeaturesFor(paperTraining, false, false);
		}
		*/
		TreeModel tmodel = null;
		if (useTreeModel.value()) {
			throw new UnsupportedOperationException ("Tree model not supported yet.");
	    /*if (pubs2 != null && pubs3 != null) {
				tmodel = new TreeModel(paperInstancePipe, paperTrainingNodes[0], paperTrainingNodes[1], paperTrainingNodes[2], pubs1, pubs3, pubs3);
	    }
	    else {
				tmodel = new TreeModel(instancePipe, nodes1, pubs1);
				}*/
	    //tmodel.setMultiTree (true);
		}
		/*
		if (useFeatureInduction.value()) {
	    System.out.println("\n\nINDUCING FEATURES FOR TEST INSTANCES");
	    fi.induceFeaturesFor(paperTesting, false, false);
		}
		*/

		MultipleCorefClusterer cl = null;
		MultipleCorefClusterer paperCl = null;
		MultipleCorefClusterer venueCl = null;
		
		if (newCluster) {
			cl = new MultipleCorefClusterer(new Pipe[] {paperPipe, venuePipe});
			paperCl = new MultipleCorefClusterer(new Pipe[] {paperPipe});
			venueCl = new MultipleCorefClusterer(new Pipe[] {venuePipe});
			initializeClusterer (cl);
			initializeClusterer (paperCl);
			initializeClusterer (venueCl);
	    if (loadMEFile.value() != null) {
				throw new UnsupportedOperationException ("Loading MaxEnt not implemented yet");
				//cl.loadME(loadMEFile.value());
			}
	    else {
				cl.train(new InstanceList[] {paperTraining, venueTraining});
				paperCl.train(new InstanceList[] {paperTraining});
				venueCl.train(new InstanceList[] {venueTraining});
			}
	    cl.testClassifiers(new InstanceList[] {paperTesting, venueTesting});
	    paperCl.testClassifiers(new InstanceList[] {paperTesting});
	    venueCl.testClassifiers(new InstanceList[] {venueTesting});
		}

		Collection[] testKeys = new Collection[] {paperTestKey, venueTestKey};
		Collection[] paperTestKeys = new Collection[] {paperTestKey};
		Collection[] venueTestKeys = new Collection[] {venueTestKey};
		// xxx keyPartitioning not implemented correctly in MultipleCorefClusterer
		cl.setKeyPartitioning (paperTestKey);
		if (newCluster) {
	    Collection[] testS = cl.clusterMentions(new InstanceList[] {paperTesting, venueTesting},
																							new List[] {paperTestingNodes, venueTestingNodes},
																							-1, useCorrelational.value());
			logger.info ("Evaluating " + testS.length + " type(s) of clusterings");
			for (int ti=0; ti < testS.length; ti++) {
				CitationUtils.evaluateClustering (testKeys[ti], testS[ti],  String.valueOf(ti) +
																					" JOINT COREFERENCE RESULTS");
				if (outputFile.value() != null)
					printClustersToFile (testS[ti], outputFile.value() + "_" + String.valueOf(ti));
			}
	    Collection[] paperTestS = paperCl.clusterMentions(new InstanceList[] {paperTesting},
																							new List[] {paperTestingNodes},
																							-1, useCorrelational.value());			
	    Collection[] venueTestS = venueCl.clusterMentions(new InstanceList[] {venueTesting},
																							new List[] {venueTestingNodes},
																							-1, useCorrelational.value());
			CitationUtils.evaluateClustering (paperTestKeys[0], paperTestS[0], "SOLO PAPER COREFERENCE RESULTS");
			CitationUtils.evaluateClustering (venueTestKeys[0], venueTestS[0], "SOLO VENUE COREFERENCE RESULTS");						
		}			
	}

	private static void initializeClusterer (MultipleCorefClusterer cl) {
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
	}

	private static void loadCRFs () {
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
	}

	/** Create citation nodes of type "type" from training files */
	private static ArrayList[] createNodesFromTraining (String type) {
		FileIterator trainFI_1 = null;
		FileIterator trainFI_2 = null;
		FileIterator trainFI_3 = null;
		if (useCRF.value() == true) {
	    trainFI_1 = new FileIterator (trainingDir1.value(),
																		new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir2.value() != null)
				trainFI_2 = new FileIterator (trainingDir2.value(),
																			new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir3.value() != null)
				trainFI_3 = new FileIterator (trainingDir3.value(),
																			new RegexFileFilter(Pattern.compile(".*")));			
		}
		else {
	    trainFI_1 = new FileIterator (trainingDir1.value(),
																		new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir2.value() != null)
				trainFI_2 = new FileIterator (trainingDir2.value(),
																			new RegexFileFilter(Pattern.compile(".*")));
	    if (trainingDir3.value() != null)
				trainFI_3 = new FileIterator (trainingDir3.value(),
																			new RegexFileFilter(Pattern.compile(".*")));			
		}		
		ArrayList trainFileArray1 = trainFI_1.getFileArray();
		ArrayList pubs1 = new ArrayList();
		System.out.println("Number of files 1: " + trainFileArray1.size());
		ArrayList nodes1;
		if (useMultipleCRFs.value() == true) {
	    if (useTreeModel.value())
				throw new UnsupportedOperationException ("tree model unsupported");
				//nodes1 = CitationUtils.computeNodesWPubs(trainFileArray1, pubs1, ieInterface1, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
	    else
				nodes1 = CitationUtils.computeNodes(trainFileArray1, ieInterface1, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
		}																														
		else {
	    if (useTreeModel.value())
				throw new UnsupportedOperationException ("tree model unsupported");
			//nodes1 = CitationUtils.computeNodesWPubs(trainFileArray1, pubs1, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
	    else
				nodes1 = CitationUtils.computeNodes(trainFileArray1, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
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
					throw new UnsupportedOperationException ("tree model unsupported");
				//nodes2 = CitationUtils.computeNodesWPubs(trainFileArray2, pubs2, ieInterface2, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
				else
					nodes2 = CitationUtils.computeNodes(trainFileArray2, ieInterface2, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
	    }
	    else {
				if (useTreeModel.value())
					throw new UnsupportedOperationException ("tree model unsupported");
				//nodes2 = CitationUtils.computeNodesWPubs(trainFileArray2, pubs2, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
				else
					nodes2 = CitationUtils.computeNodes(trainFileArray2, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
	    }
		}

		if (trainFI_3 != null) {
	    ArrayList trainFileArray3 = trainFI_3.getFileArray();
	    pubs3 = new ArrayList();
	    System.out.println("Number of files 3: " + trainFileArray3.size());
	    //nodes3 = computeNodesWPubs(trainFileArray3, pubs3, ieInterface3);
	    if (useMultipleCRFs.value() == true) {
				if (useTreeModel.value())
					throw new UnsupportedOperationException ("tree model unsupported");
				//nodes3 = CitationUtils.computeNodesWPubs(trainFileArray3, pubs3, ieInterface3, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
				else
					nodes3 = CitationUtils.computeNodes(trainFileArray3, ieInterface3, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
	    }
	    else {
				if (useTreeModel.value())
					throw new UnsupportedOperationException ("tree model unsupported");
				//nodes3 = CitationUtils.computeNodesWPubs(trainFileArray3, pubs3, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
				else
					nodes3 = CitationUtils.computeNodes(trainFileArray3, ieInterface, !trainUsingLabeled.value(), numNBest.value(), nthViterbi.value(), type);
	    }
	    System.out.println(" There are " + nodes3.size() + " training nodes");
		}
		ArrayList[] ret = null;
		int numLists = 1;
		if (nodes2 != null)
			numLists++;
		if (nodes3 != null)
			numLists++;
		if (numLists == 3)
			ret = new ArrayList[] {nodes1, nodes2, nodes3};
		else if (numLists == 2)
			ret = new ArrayList[] {nodes1, nodes2};
		else
			ret = new ArrayList[] {nodes1};
		return ret;
		
	}

	/** Create citation nodes of type "type" from testing files */
	private static ArrayList createNodesFromTesting (String type) {
		FileIterator testFI = null;
		if (useCRF.value() == true)
	    testFI = new FileIterator (testingDir.value(), new RegexFileFilter(Pattern.compile(".*")));
		else	
	    testFI = new FileIterator (testingDir.value(), new RegexFileFilter(Pattern.compile(".*")));
		
		ArrayList testFileArray = testFI.getFileArray();
		ArrayList testPubList = new ArrayList();
		
		ArrayList test_nodes;
		if (useMultipleCRFs.value() == true) {
	    test_nodes = CitationUtils.computeNodes(testFileArray,ieInterface4, false, numNBest.value(), nthViterbi.value(), type);
		}
		else {
	    if (useTreeModel.value())
				throw new UnsupportedOperationException ("tree model unsupported");
			//test_nodes = CitationUtils.computeNodesWPubs(testFileArray, testPubList, ieInterface, useCRF.value(), numNBest.value(), nthViterbi.value(), type);
	    else
				test_nodes = CitationUtils.computeNodes(testFileArray, ieInterface, useCRF.value(), numNBest.value(), nthViterbi.value(),type);
		}
		return test_nodes;
	}

	private static InstanceList getTrainingList (ArrayList[] nodes, Pipe p) {
		InstanceList ilist = new InstanceList();
		if (loadMEFile.value() == null) {
	    InstanceList ilist1 = CitationUtils.makePairs(p, nodes[0]);
	    ilist.add(ilist1);
	    if (nodes.length > 1) {
				InstanceList ilist2 = CitationUtils.makePairs(p, nodes[1]);
				ilist.add(ilist2);
	    }
	    if (nodes.length > 2) {
				InstanceList ilist3 = CitationUtils.makePairs(p, nodes[2]);
				ilist.add(ilist3);
	    }
		}
		return ilist;
	}
	
	private static Pipe getPaperPipe (AbstractStatisticalTokenDistance distanceMetric, StringDistance triGramDistanceMetric) {
		Pipe p = new SerialPipes (new Pipe[] {
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
		return p;
	}

	private static Pipe getVenuePipe (AbstractStatisticalTokenDistance distanceMetric, StringDistance triGramDistanceMetric) {
		Pipe p = new SerialPipes (new Pipe[] {
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
		return p;
	}
	
	private static TFIDF getDistanceMetric (ArrayList allnodes) {
		//make distance metrics
		TFIDF tfidf = new TFIDF();
		NGramTokenizer nGramTokenizer =
	    new NGramTokenizer(3,3,false, new SimpleTokenizer(true, true));				
		TFIDF ret = new TFIDF (nGramTokenizer);
		CitationUtils.makeDistMetric(allnodes, tfidf, ret);
		return ret;
	}
	
	private static void readCluster (File f) {
		
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
