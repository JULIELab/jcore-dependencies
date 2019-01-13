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

public class BenCitationTUINoSeg
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
	(TUI.class, "full-partition", "FILENAME", false, false,
	 "Use full partitioninig", null);

	static CommandOption.Boolean useWeightedAvg = new CommandOption.Boolean
	(TUI.class, "use-weighted-avg", "FILENAME", false, false,
	 "Use weighted average", null);

	static CommandOption.String loadMEFile = new CommandOption.String
	(TUI.class, "load-me-file", "FILENAME", true, null,
	 "The name of the MaxEnt model file.", null);

	static CommandOption.String outputFile = new CommandOption.String
	(TUI.class, "output-file", "FILENAME", true, null,
	 "The name of the file where output clusters will be printed to.", null);

	static CommandOption.String crfInputFile = new CommandOption.String
	(TUI.class, "crf-input-file", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile1 = new CommandOption.String
	(TUI.class, "crf-input-file-1", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile2 = new CommandOption.String
	(TUI.class, "crf-input-file-2", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile3 = new CommandOption.String
	(TUI.class, "crf-input-file-3", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.String crfInputFile4 = new CommandOption.String
	(TUI.class, "crf-input-file-4", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.Boolean useCRF = new CommandOption.Boolean
	(TUI.class, "use-crf", "BOOL", false, false,
	 "Use CRF or not.", null);

	static CommandOption.Boolean useMultipleCRFs = new CommandOption.Boolean
	(TUI.class, "use-multiple-crfs", "BOOL", false, false,
	 "Use a separate crf for each data segment or not.", null);

	static CommandOption.Boolean useTreeModel = new CommandOption.Boolean
	(TUI.class, "use-tree-model", "BOOL", false, false,
	 "Use and train tree model.", null);

	static CommandOption.Boolean useCorrelational = new CommandOption.Boolean
	(TUI.class, "use-correlational", "BOOL", false, false,
	 "Use Correlational Clustering or not, if not uses Greedy.", null);

	static CommandOption.Boolean useFeatureInduction = new CommandOption.Boolean
	(TUI.class, "use-feature-induction", "BOOL", false, false,
	 "Use Feature Induction or Not.", null);

	static CommandOption.Boolean useNBest = new CommandOption.Boolean
	(TUI.class, "use-n-best", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Boolean useTrueNumClusters = new CommandOption.Boolean
	(TUI.class, "use-true-num-clusters", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Boolean useOptimal = new CommandOption.Boolean
	(TUI.class, "use-optimal", "BOOL", false, false,
	 "Use NBest or not.", null);

	static CommandOption.Integer optimalNBest = new CommandOption.Integer
	(TUI.class, "optimal-n-best", "INTEGER", true, -1,
	 "Size of n, for searching for optimal n-best configuration.", null);

	static CommandOption.Integer rBeamSize = new CommandOption.Integer
	(TUI.class, "r-beam-size", "INTEGER", true, 10,
	 "Size of n, for searching for optimal n-best configuration.", null);

	static CommandOption.String trainingDir1 = new CommandOption.String
	(TUI.class, "training-dir-1", "FILENAME", true, null,
	 "Directory containing training files.", null);

	static CommandOption.String trainingDir2 = new CommandOption.String
	(TUI.class, "training-dir-2", "FILENAME", true, null,
	 "Directory containing training files.", null);

	static CommandOption.String trainingDir3 = new CommandOption.String
	(TUI.class, "training-dir-3", "FILENAME", true, null,
	 "Directory containing training files.", null);
	
	static CommandOption.String testingDir = new CommandOption.String
	(TUI.class, "testing-dir", "FILENAME", true, null,
	 "Directory containing testing files.", null);

	static CommandOption.Integer searchIters = new CommandOption.Integer
	(TUI.class, "search-iters", "INTEGER", true, 3,
	 "Number of search iterations.", null);

	static CommandOption.Integer searchReductions = new CommandOption.Integer
	(TUI.class, "search-reductions", "INTEGER", true, 5,
	 "Number of search reductions.", null);

	static CommandOption.Integer numNBest = new CommandOption.Integer
	(TUI.class, "num-n-best", "INTEGER", true, 3,
	 "Number of n-best candidates to store.", null);

	static CommandOption.Integer nthViterbi = new CommandOption.Integer
	(TUI.class, "nth-viterbi", "INTEGER", true, 0,
	 "Number of n-best candidates to use .", null);

	static CommandOption.Boolean trainUsingLabeled = new CommandOption.Boolean
  	(TUI.class, "train-using-labeled", "BOOL", true, false,
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

	protected static ArrayList computeNodes(ArrayList trainFileArray, IEInterface ieInterface) {
		return computeNodes(trainFileArray, ieInterface, false);
	}

	protected static ArrayList computeNodes(ArrayList trainFileArray, IEInterface ieInterface,
																					boolean useCRFLocal)
	{
		System.out.println("Computing nodes...");
		long timeStart = System.currentTimeMillis();
		Reader reader;
		ArrayList nodes = new ArrayList();

		HashMap hMap = new HashMap();  // keys are cluster IDs, values are publications

		int index = 0;
		for(int i=0; i<trainFileArray.size(); i++){
	    File file = (File)trainFileArray.get(i);
	    String fileID = file.toString();

	    System.out.println(i + ": " + fileID );

	    try {
				reader = new FileReader (file);
	    } catch (Exception e) {
				throw new IllegalArgumentException ("Can't read file "+file);
	    }

	    LineGroupIterator lineI = new LineGroupIterator (reader, Pattern.compile(SEPERATOR[1]), true);

	    while(lineI.hasNext()){
				String str = lineI.getLineGroup();
				Integer id = new Integer(index++);
				String label = fileID;
				// <meta reference_no="10" cluster_no="2"></meta>
				String start_tag = "<meta"; // intentionally left off the end tag, because of attributes:
				String end_tag   = "</meta>";

				String s = SGMLStringOperation.locateField(start_tag, end_tag, str);
				String[] ss = s.split("\"");
				if (ss != null && ss.length == 5) {
					label = ss[3];
					label.intern();
					id = new Integer(ss[1]);
				}
				str = str.substring(str.indexOf(end_tag)+end_tag.length(), str.length());
				str = str.intern();
				//str = str.toLowerCase();
				//nodes.add(new Node(new Citation(str, label, id.intValue())));
				if (useCRFLocal) {
					nodes.add(new Citation(str, label, id.intValue(), ieInterface,
																 numNBest.value(), nthViterbi.value()));
				} else {
					nodes.add(new Citation(str, label, id.intValue()));
				}
				//System.out.println("X" + str);
				//System.out.println("X" + label);
				//System.out.println("X" + id);

				lineI.nextLineGroup();
	    }

		}
		long timeEnd = System.currentTimeMillis();
		double timeElapse = (timeEnd - timeStart)/(1000.000);
		System.out.println("Time elapses " + timeElapse + " seconds for computing nodes.");

		return nodes;

	}

	protected static ArrayList computeNodesWPubs(ArrayList trainFileArray, ArrayList publications,
																							 IEInterface ieInterface)
	{
		return computeNodesWPubs(trainFileArray, publications, ieInterface, false);
	}

	protected static ArrayList computeNodesWPubs(ArrayList trainFileArray, ArrayList publications,
																							 IEInterface ieInterface, boolean useCRFLocal)
	{
		System.out.println("Computing nodes...");
		long timeStart = System.currentTimeMillis();
		Reader reader;
		ArrayList nodes = new ArrayList();

		HashMap hMap = new HashMap();  // keys are cluster IDs, values are publications

		int index = 0;
		for(int i=0; i<trainFileArray.size(); i++){
	    File file = (File)trainFileArray.get(i);
	    String fileID = file.toString();

	    System.out.println(i + ": " + fileID );

	    try {
				reader = new FileReader (file);
	    } catch (Exception e) {
				throw new IllegalArgumentException ("Can't read file "+file);
	    }

	    LineGroupIterator lineI = new LineGroupIterator (reader, Pattern.compile(SEPERATOR[1]), true);

	    while(lineI.hasNext()){
				String str = lineI.getLineGroup();
				Integer id = new Integer(index++);
				String label = fileID;
				// <meta reference_no="10" cluster_no="2"></meta>
				String start_tag = "<meta"; // intentionally left off the end tag, because of attributes:
				String end_tag   = "</meta>";

				String s = SGMLStringOperation.locateField(start_tag, end_tag, str);
				String[] ss = s.split("\"");
				if (ss != null && ss.length == 5) {
					label = ss[3];
					label.intern();
					id = new Integer(ss[1]);
				}
				str = str.substring(str.indexOf(end_tag)+end_tag.length(), str.length());
				str = str.intern();
				//str = str.toLowerCase();

				Citation cit = null;
				if (useCRFLocal) {
					cit = new Citation(str, label, id.intValue(), ieInterface,
														 numNBest.value(), nthViterbi.value());
				} else {
					cit = new Citation(str, label, id.intValue());
				}
				nodes.add(cit);
				Publication p = (Publication)hMap.get(label); // look up publication that this
				// belongs to
				if (p != null) { 
					p.addNewCitation (cit);  // add citation to publication
				} else {
					p = new Publication (cit); // create new publication with this citation
					hMap.put(label, p); // add publication to hash map
					publications.add(p);
				}
				
				lineI.nextLineGroup();
	    }

		}
		long timeEnd = System.currentTimeMillis();
		double timeElapse = (timeEnd - timeStart)/(1000.000);
		System.out.println("Time elapses " + timeElapse + " seconds for computing nodes.");

		return nodes;

	}


	public static StringDistance computeDistanceMetric (ArrayList nodes) {
		ArrayList allStrings = new ArrayList();
		StringDistance tfidf = new TFIDF ();
		
		for (int i=0; i < nodes.size(); i++) {
	    //Citation c = (Citation)((Node)nodes.get(i)).getObject();
	    Citation c = (Citation)nodes.get(i);
	    allStrings.addAll(c.getAllStringsWrapped());
		}
		tfidf.accumulateStatistics(allStrings.iterator());
		return tfidf;
		//return new SoftTFIDF(tfidf);
	}

	private static void makeDistMetric(List list) {

		List nodes = new ArrayList();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			nodes.add((Citation)iter.next());
		}

		NGramTokenizer nGramTokenizer =
	    new NGramTokenizer(3,3,false, new SimpleTokenizer(true, true));

		ArrayList allStrings = new ArrayList();
		tfidf = new TFIDF ();
		//softtfidf = new SoftTFIDF(new JaroWinkler(), 0.8);
		triGramDistanceMetric = new TFIDF(nGramTokenizer);
		for (int i=0; i < nodes.size(); i++) {
			Citation c = (Citation)nodes.get(i);
			allStrings.addAll(c.getAllStringsWrapped());
		}
		tfidf.accumulateStatistics(allStrings.iterator());
		triGramDistanceMetric.accumulateStatistics(allStrings.iterator());
		//softtfidf.accumulateStatistics(allStrings.iterator());
	}


	public static void main (String[] args) throws FileNotFoundException
	{

		commandOptions.process (args);
		String[] startTags = new String[]{"<author>"};
		String[] endTags = new String[]{"</author>"};


		boolean oldCluster = false;
		boolean newCluster = true;//true;


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
				nodes1 = computeNodesWPubs(trainFileArray1, pubs1, ieInterface1, !trainUsingLabeled.value());
			else
				nodes1 = computeNodes(trainFileArray1, ieInterface1, !trainUsingLabeled.value());
		}																														
		else {
			if (useTreeModel.value())
				nodes1 = computeNodesWPubs(trainFileArray1, pubs1, ieInterface, !trainUsingLabeled.value());
			else
				nodes1 = computeNodes(trainFileArray1, ieInterface, !trainUsingLabeled.value());
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
					nodes2 = computeNodesWPubs(trainFileArray2, pubs2, ieInterface2, !trainUsingLabeled.value());
				else
					nodes2 = computeNodes(trainFileArray2, ieInterface2, !trainUsingLabeled.value());
			}
			else {
				if (useTreeModel.value())
					nodes2 = computeNodesWPubs(trainFileArray2, pubs2, ieInterface, !trainUsingLabeled.value());
				else
					nodes2 = computeNodes(trainFileArray2, ieInterface, !trainUsingLabeled.value());
			}
		}

		if (trainFI_3 != null) {
	    ArrayList trainFileArray3 = trainFI_3.getFileArray();
	    pubs3 = new ArrayList();
	    System.out.println("Number of files 3: " + trainFileArray3.size());
	    //nodes3 = computeNodesWPubs(trainFileArray3, pubs3, ieInterface3);
	    if (useMultipleCRFs.value() == true) {
				if (useTreeModel.value())
					nodes3 = computeNodesWPubs(trainFileArray3, pubs3, ieInterface3, !trainUsingLabeled.value());
				else
					nodes3 = computeNodes(trainFileArray3, ieInterface3, !trainUsingLabeled.value());
			}
			else {
				if (useTreeModel.value())
					nodes3 = computeNodesWPubs(trainFileArray3, pubs3, ieInterface, !trainUsingLabeled.value());
				else
					nodes3 = computeNodes(trainFileArray3, ieInterface, !trainUsingLabeled.value());
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
	    test_nodes = computeNodes(testFileArray,ieInterface4);
		}
		else {
			if (useTreeModel.value())
				test_nodes = computeNodesWPubs(testFileArray, testPubList, ieInterface, useCRF.value());
			else
				test_nodes = computeNodes(testFileArray, ieInterface, useCRF.value());
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
		makeDistMetric(allnodes);
		
		System.out.println("finished computing nodes, about to compute distanceMetric params ");
		// compute the string distance using SecondString utilities
		// this will serve as a useful feature
		// Possible extension (later): build different string metrics for
		// different fields - this will then be an array of them
		AbstractStatisticalTokenDistance distanceMetric =
	    (AbstractStatisticalTokenDistance)computeDistanceMetric (allnodes);
		
		Pipe instancePipe = new SerialPipes (new Pipe[] {
		    //new ExactFieldMatchPipe(Citation.corefFields),

		    //new PageMatchPipe(),
		    //new YearsWithinFivePipe(),
	    //new FieldStringDistancePipe(new NeedlemanWunsch(),
	    //Citation.corefFields, "EDIST"),
	    //new FieldStringDistancePipe(softtfidf, Citation.corefFields, "softTFIDF"),
	    //new FieldStringDistancePipe(triGramDistanceMetric, Citation.corefFields, "trigramTFIDF"),
			
	    //new PlainFieldPipe (distanceMetric, distanceMetricEditDist),
	    new GlobalPipe(distanceMetric),
	    //new TitlePipe(distanceMetric),
	    //new AuthorPipe(distanceMetric),
	    //new JournalPipe(distanceMetric),
	    //new PagesPipe(distanceMetric),
			//new HeuristicPipe(Citation.corefFields),
		    //new InterFieldPipe(),
			//new HeuristicPipe(Citation.corefFields),
	    //new DatePipe(distanceMetric),

	    //new FuchunPipe(distanceMetricEditDist),
			
	    new NodePair2FeatureVector (),
	    new Target2Label (),
		});

		InstanceList ilist = new InstanceList();
		if (loadMEFile.value() == null) {
			InstanceList ilist1 = makePairs(instancePipe, nodes1);
			ilist.add(ilist1);
			if (nodes2 != null) {
				InstanceList ilist2 = makePairs(instancePipe, nodes2);
				ilist.add(ilist2);
			}
			if (nodes3 != null) {
				InstanceList ilist3 = makePairs(instancePipe, nodes3);
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
		//InstanceList ilistToCluster = makePairs(instancePipe, nodes, pairsFromCanopy);
		InstanceList itestlist = makePairs(instancePipe, test_nodes);
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

		Collection key = makeCollections(allnodes); // make key collections
		//System.out.println("KEY: " + key);
		//System.out.println("NODES: " + nodes);
		Collection testKey = makeCollections(test_nodes);
		Collection s = null;
		if (newCluster) {
			//cl.setKeyPartitioning(key);
	    //s = cl.clusterMentions(ilist, allnodes, optimalNBest.value(), useCorrelational.value());
		}
		System.out.println("Resulting clustering: " + s);

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
		}			

		/*
			System.out.println("Final parameters used: ");
			double [] ps = cl.getClassifier().getParameters();
			for (int k=0; k < Array.getLength(ps); k++) {
			System.out.print(" " + ps[k]);
			}*/

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
				try {
					out.write("<NEWREFERENCE>\n");
					out.write("<meta reference_no=\"" + refNum +
										"\" cluster_no=\"" + clNum + "\"></meta>");
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

	// this version assumes nodes are actually citations
	protected static Collection makeCollections (ArrayList nodes) {

		HashMap map = new HashMap(); // keep an index of node label values to collections
		Collection collection = new LinkedHashSet();
		for (int i=0; i < nodes.size(); i++) {
	    Citation n = (Citation)nodes.get(i);
	    Object o1 = n.getLabel();
	    Collection c = (Collection)map.get(o1);
	    if (c != null) {
				c.add(n);
				//System.out.println("adding new node " + n + " to existing collection with " + o1);
	    } else {
				Collection newC = new LinkedHashSet();
				//System.out.println("Creating new collection -> id: " + o1);
				newC.add(n);
				map.put(o1, newC);
	    }
		}
		Iterator i1 = map.values().iterator();
		while (i1.hasNext()) {
	    collection.add((Collection)i1.next());
		}
		return collection;
	}

	protected static InstanceList makePairs(Pipe instancePipe, ArrayList nodes) {
		System.out.println("PairIterator...");
		long timeStart = System.currentTimeMillis();
		InstanceList ilist = new InstanceList (instancePipe);
		ilist.add (new NodePairIterator (nodes) );
		System.out.println("====");
		long timeEnd = System.currentTimeMillis();
		double timeElapse = (timeEnd - timeStart)/(1000.000);
		System.out.println("Time elapses " + timeElapse + " seconds for computing pair iterator.");
		return ilist;
	}
	protected static InstanceList makePairs(Pipe instancePipe, ArrayList nodes, List pairs) {
		System.out.println("PairIterator...");
		long timeStart = System.currentTimeMillis();
		InstanceList ilist = new InstanceList (instancePipe);
		ilist.add (new NodePairIterator (nodes, pairs) );
		System.out.println("====");
		long timeEnd = System.currentTimeMillis();
		double timeElapse = (timeEnd - timeStart)/(1000.000);
		System.out.println("Time elapses " + timeElapse + " seconds for computing pair iterator.");
		return ilist;
	}
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
