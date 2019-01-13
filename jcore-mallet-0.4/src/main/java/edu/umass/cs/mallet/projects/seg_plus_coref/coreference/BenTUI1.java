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

import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
//import edu.umass.cs.mallet.users.hay.canopy.Util;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructor;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructorSimple;
//import edu.umass.cs.mallet.users.hay.canopy.IndexFiles;
//import edu.umass.cs.mallet.users.hay.canopy.CanopyMaker;
//import edu.umass.cs.mallet.users.hay.canopy.QueryConstructorAuthDateTitle;
//import salvo.jesus.graph.WeightedGraph;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.SimpleAnalyzer;

public class BenTUI1
{
	private static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "<NEWREFERENCE>"};
    private static boolean useNBest = false;

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

	// publications will be set destructively here
	protected static ArrayList computeNodes(ArrayList trainFileArray, ArrayList publications)
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

				//String s = SGMLStringOperation.locateField(start_tag, end_tag, str, false);
				String s = "foo";
				String[] ss = s.split("\"");
				if (ss != null && ss.length == 5) {
					label = ss[3];
					label.intern();
					id = new Integer(ss[1]);
				}
				str = str.substring(str.indexOf(end_tag)+end_tag.length(), str.length());
				str = str.intern();
				str = str.toLowerCase();
				Citation cit = null;
				if (useNBest) {
					cit = new Citation(str, label, id.intValue(), null);
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

	public static void main (String[] args) throws FileNotFoundException
	{

//              String[] startTags = new String[]{};
		//             String[] endTags = new String[]{};

		String[] startTags = new String[]{"<author>"};
		String[] endTags = new String[]{"</author>"};

//		String[] startTags = new String[]{"<title>"};
//		String[] endTags = new String[]{"</title>"};

//		String[] startTags = new String[]{"<journal>"};
//		String[] endTags = new String[]{"</journal>"};

//		String[] startTags = new String[]{"<author>", "<title>"};
//		String[] endTags = new String[]{"</author>", "</title>"};

//		String[] startTags = new String[]{"<author>", "<journal>"};
//		String[] endTags = new String[]{"</author>",  "</journal>"};

//		String[] startTags = new String[]{"<title>", "<journal>"};
//		String[] endTags = new String[]{"</title>", "</journal>"};

//		String[] startTags = new String[]{"<author>", "<title>", "<journal>"};
//		String[] endTags = new String[]{"</author>", "</title>", "</journal>"};


		//deal with multiple directories
		//the first argument specifies the number of training directories

		// threshold
		double threshold = Double.parseDouble(args[0]);

		
		int trainingDirCount = Integer.parseInt(args[1]);
		String[] trainingDirs = new String[trainingDirCount];
		for (int i = 0; i < trainingDirCount; i++) {
			trainingDirs[i] = args[i+2];
		}
		FileIterator trainFI = new FileIterator (trainingDirs, new RegexFileFilter(Pattern.compile(".*tagged")));
		ArrayList trainFileArray = trainFI.getFileArray();
		//ArrayList nodes = computeNodes(trainFileArray);
		ArrayList pubList = new ArrayList();
		ArrayList nodes = computeNodes(trainFileArray, pubList);

		int testDirCount = args.length - 2 - trainingDirCount;
		String[] testDirs = new String[testDirCount];
		for (int i = 0; i < testDirCount; i++) {
			testDirs[i] = args[i+2+trainingDirCount];
		}
		FileIterator testFI = new FileIterator (testDirs, new RegexFileFilter(Pattern.compile(".*tagged")));
		ArrayList testFileArray = testFI.getFileArray();
		ArrayList testPubList = new ArrayList();

		//ArrayList test_nodes = computeNodes(testFileArray);
		ArrayList test_nodes = computeNodes(testFileArray, testPubList);

		ArrayList allnodes = new ArrayList();  // all nodes, both training and test
		allnodes.addAll(nodes);
		allnodes.addAll(test_nodes);
		
		System.out.println("finished computing nodes, about to compute distanceMetric params ");
		// compute the string distance using SecondString utilities
		// this will serve as a useful feature
		// Possible extension (later): build different string metrics for
		// different fields - this will then be an array of them
		
		AbstractStatisticalTokenDistance distanceMetric =
		(AbstractStatisticalTokenDistance)computeDistanceMetric (allnodes);
		StringDistance distanceMetricEditDist = new Jaccard();

		Pipe instancePipe = new SerialPipes (new Pipe[] {
//			new SGMLStringDistances(),
//			new SGMLStringDistances(startTags, endTags),

			new GlobalPipe(distanceMetric),
			new TitlePipe(distanceMetricEditDist),
			new AuthorPipe(distanceMetricEditDist),
			new JournalPipe(distanceMetricEditDist),
			new PagesPipe(distanceMetricEditDist),
			new InterFieldPipe(),			
			new DatePipe(distanceMetric),

//			new FuchunPipe(distanceMetricEditDist),
			new NodePair2FeatureVector (),
			new Target2Label (),
		});
		
		InstanceList ilist = makePairs(instancePipe, nodes);


		//List pairsFromCanopy = Util.readPairsFromFile("/tmp/pairs");
		//InstanceList ilistToCluster = makePairs(instancePipe, nodes, pairsFromCanopy);

		InstanceList itestlist = makePairs(instancePipe, test_nodes);
		TreeModel tmodel = new TreeModel(instancePipe, nodes, pubList);

		//training
		//CitationClustering cl = new CitationClustering();
		CorefCluster cl_old = new CorefCluster();
		CorefClusterAdv cl = new CorefClusterAdv(tmodel);

		cl_old.train(ilist);



			Collection key = makeCollections(nodes); // make key collections
			cl.train(ilist);
			cl.setKeyPartitioning(key);
			Collection s = cl.clusterMentions(ilist, nodes);

			System.out.println("training....");


			Collection c1 = cl_old.clusterMentions(ilist, nodes);
		 
			System.out.println("Objective fn of KEY: " +
												 cl.evaluatePartitioningExternal(ilist, nodes, key));

			System.out.println("Objective fn of OLD CLUSTERING: " +
												 cl.evaluatePartitioningExternal(ilist, nodes, c1));
			ClusterEvaluate eval1 = new ClusterEvaluate(key, c1);
			eval1.evaluate();
			System.out.println("Old Training Cluster F1: " + eval1.getF1());
			System.out.println("Old Training Cluster Recall: " + eval1.getRecall());
			System.out.println("Old Training Cluster Precision: " + eval1.getPrecision());

			PairEvaluate p1 = new PairEvaluate (key, c1);
			p1.evaluate();
			System.out.println("Pair F1: " + p1.getF1());

			ClusterEvaluate eval = new ClusterEvaluate(key, s);
			eval.evaluate();
			PairEvaluate pairEval = new PairEvaluate(key, s);
			pairEval.evaluate();

			System.out.println("Objective fn of NEW CLUSTERING Training: " +
												 cl.evaluatePartitioningExternal(ilist, nodes, s));

			System.out.println("Training Cluster F1: " + eval.getF1());
			System.out.println("Training Cluster Recall: " + eval.getRecall());
			System.out.println("Training Cluster Precision: " + eval.getPrecision());
			System.out.println("Number of clusters " + s.size());
			//System.out.println("Training pair-wise F1: " + pairEval.getF1());

			//evaluate on testing set
			Collection testKey = makeCollections(test_nodes);
			cl.setKeyPartitioning (testKey);
			Collection testS = cl.clusterMentions(itestlist, test_nodes);

			ClusterEvaluate evalTest = new ClusterEvaluate(testKey, testS);
			evalTest.evaluate();
			evalTest.printVerbose();

			System.out.println("Tree Obj Fn Val: " + tmodel.computeTreeObjFn (testS));

			System.out.println("Final parameters used: ");
			double [] ps = cl.getClassifier().getParameters();
			for (int k=0; k < Array.getLength(ps); k++) {
				System.out.print(" " + ps[k]);
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
