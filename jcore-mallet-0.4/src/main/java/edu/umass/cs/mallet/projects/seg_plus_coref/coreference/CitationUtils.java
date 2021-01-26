package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import com.wcohen.secondstring.TFIDF;
import com.wcohen.secondstring.tokens.NGramTokenizer;
import com.wcohen.secondstring.tokens.SimpleTokenizer;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.iterator.LineGroupIterator;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.ClusterEvaluate;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.PairEvaluate;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class CitationUtils {

	private static Logger logger = Logger.getLogger(CitationUtils.class.getName());

	public final static String PAPER = "PAPER";
	public final static String VENUE = "VENUE";
	public final static String AUTHOR = "AUTHOR";	
	public static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "<NEWREFERENCE>"};

	public static ArrayList computeNodes(ArrayList trainFileArray, IEInterface ieInterface,
																			 boolean useCRFLocal,
																			 int numNBest, int nthViterbi ) {
		return computeNodes(trainFileArray, ieInterface, useCRFLocal, numNBest, nthViterbi, PAPER);
	}

	public static void addPaperFromLine (String str, ArrayList nodes,
																				 IEInterface ieInterface,
																				 boolean useCRFLocal,
																				 int numNBest, int nthViterbi) {				
		// xxx why is label made fileID only to be overwritten?
		// does code require that files correspond to clusters?!?
		// <meta reference_no="10" cluster_no="2"></meta>
		HashMap clusterAttributes = SGMLStringOperation.locateAttributes ("meta", str);
 		if (clusterAttributes.size() != 2) 
			throw new IllegalArgumentException ("Reference has no paper label tag: " + str);
		String label = (String)clusterAttributes.get ("cluster_no");
		Integer id = new Integer ((String)clusterAttributes.get ("reference_no"));
		String start_tag = "<meta"; 
		String end_tag   = "</meta>";
		//str = str.substring(str.indexOf(end_tag)+end_tag.length(), str.length());
		//str = str.intern();
		if (useCRFLocal) {
			nodes.add(new PaperCitation(str, label, id.intValue(), ieInterface,
																	numNBest, nthViterbi));
		} else {
			nodes.add(new PaperCitation(str, label, id.intValue()));
		}
	}		

	public static void addVenuesFromLine (String str, ArrayList nodes,
																					IEInterface ieInterface,
																					boolean useCRFLocal,
																					int numNBest, int nthViterbi) {
		HashMap clusterAttributes = SGMLStringOperation.locateAttributes ("booktitle", str);
		if (clusterAttributes.size() != 3)
			clusterAttributes = SGMLStringOperation.locateAttributes ("journal", str);
 		if (clusterAttributes.size() != 3) 
			return;
		String label = (String)clusterAttributes.get ("venue_cluster");
		if (label == null)
			throw new IllegalArgumentException ("bad venue line: " + str);
		Integer id = new Integer ((String)clusterAttributes.get ("venue_no"));
		String start_tag = "<meta"; 
		String end_tag   = "</meta>";
		//str = str.substring(str.indexOf(end_tag)+end_tag.length(), str.length());
		//str = str.intern();
		if (useCRFLocal) {
			nodes.add(new VenueCitation(str, label, id.intValue(), ieInterface,
														 numNBest, nthViterbi));
		} else {
			nodes.add(new VenueCitation(str, label, id.intValue()));
		}		
	}

	public static void addAuthorsFromLine (String str, ArrayList nodes,
																					IEInterface ieInterface,
																					boolean useCRFLocal,
																					int numNBest, int nthViterbi) {
		throw new UnsupportedOperationException ("Not yet implemented");
		/*String end_tag   = "</meta>";
 		String subs = str.substring(str.indexOf(end_tag)+end_tag.length(), str.length());
		//subs = sNorm.intern();
		ArrayList clusterAttributes = SGMLStringOperation.locateAllAttributes ("author", str);
		if (clusterAttributes.size() == 0)
			logger.fine ("WARNING: No author fields for " + str);
		for (int i=0; i < clusterAttributes.size(); i++) { // for each author
			HashMap h = (HashMap) clusterAttributes.get (i);
			String label = (String) h.get ("author_cluster");
			if (label == null)
				throw new IllegalArgumentException ("bad author line: " + str);			
			Integer id = new Integer ((String) h.get ("author_no"));
			if (useCRFLocal) {
				nodes.add(new AuthorCitation(subs, label, id.intValue(), ieInterface,
															 numNBest, nthViterbi));
			} else {
				nodes.add(new AuthorCitation(subs, label, id.intValue()));
			}
			// xxx indicate to which author in this citation this Citation instance refers 
		}
		*/
	}
	
/** Build nodes with labels corresponding to cluster ids. Passing
			different strings for nodeType will make node correspond to the
			paper, author, or venue, etc. Default is paper.
			@param trainFileArray list of citation files
			@param ieInterface the interface to the extraction
			@param useCRFLocal use a CRF for segmentation
			@param numNBest number of viterbi paths to use
			@param nodeType type of node to create (paper, author, venue...)
	*/
	public static ArrayList computeNodes(ArrayList trainFileArray,
																			 IEInterface ieInterface,
																			 boolean useCRFLocal,
																			 int numNBest,
																			 int nthViterbi,
																			 String nodeType)
	{
		logger.fine("Computing nodes...");
		long timeStart = System.currentTimeMillis();
		Reader reader;
		ArrayList nodes = new ArrayList();
		
		HashMap hMap = new HashMap();  // keys are cluster IDs, values are publications
		int index = 0;
		for(int i=0; i<trainFileArray.size(); i++){
	    File file = (File)trainFileArray.get(i);
	    String fileID = file.toString();
	    logger.fine(i + ": " + fileID );
	    try {
				reader = new FileReader (file);
	    } catch (Exception e) {
				throw new IllegalArgumentException ("Can't read file "+file);
	    }

	    LineGroupIterator lineI = new LineGroupIterator (reader, Pattern.compile(SEPERATOR[1]), true);

	    while(lineI.hasNext()){
				String str = lineI.getLineGroup();
				if (nodeType.equals (PAPER))
					addPaperFromLine (str, nodes, ieInterface, useCRFLocal, numNBest, nthViterbi);
				else if (nodeType.equals (VENUE))
					addVenuesFromLine (str, nodes, ieInterface, useCRFLocal, numNBest, nthViterbi);
				else if (nodeType.equals (AUTHOR))
					addAuthorsFromLine (str, nodes, ieInterface, useCRFLocal, numNBest, nthViterbi);
				else
					throw new IllegalArgumentException ("Unrecognized node type: " + nodeType);
				lineI.nextLineGroup();
	    }

		}
		long timeEnd = System.currentTimeMillis();
		double timeElapse = (timeEnd - timeStart)/(1000.000);
		logger.info("Time elapses " + timeElapse + " seconds for computing " + nodes.size() + " nodes.");

		return nodes;
	}

	public static ArrayList computeNodesWPubs(ArrayList trainFileArray,
																						ArrayList publications,
																						IEInterface ieInterface,
																						int numNBest, int nthViterbi) {
		return computeNodesWPubs(trainFileArray, publications, ieInterface, false, numNBest, nthViterbi);
	}
	
	// xxx generalize this to make venues/authors objects as in
	// computeNodes above.
	protected static ArrayList computeNodesWPubs(ArrayList trainFileArray,
																							 ArrayList publications,
																							 IEInterface ieInterface,
																							 boolean useCRFLocal, int numNBest,
																							 int nthViterbi) {
		logger.fine("Computing nodes...");
		long timeStart = System.currentTimeMillis();
		Reader reader;
		ArrayList nodes = new ArrayList();

		HashMap hMap = new HashMap();  // keys are cluster IDs, values are publications

		int index = 0;
		for(int i=0; i<trainFileArray.size(); i++){
	    File file = (File)trainFileArray.get(i);
	    String fileID = file.toString();

	    logger.fine(i + ": " + fileID );

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
				HashMap clusterAttributes = SGMLStringOperation.locateAttributes ("meta", str);
				if (clusterAttributes.size() != 2) 
					throw new IllegalArgumentException ("Reference has no cluster or reference id: " + str);
				label = (String)clusterAttributes.get ("cluster_no");
				id = new Integer ((String)clusterAttributes.get ("reference_no"));				
				/*
				String s = SGMLStringOperation.locateField(start_tag, end_tag, str);
				String[] ss = s.split("\"");
				if (ss != null && ss.length == 5) {
					label = ss[3];
					label.intern();
					id = new Integer(ss[1]);
				}
				*/
				String start_tag = "<meta"; 
				String end_tag   = "</meta>";
				str = str.substring(str.indexOf(end_tag)+end_tag.length(), str.length());
				str = str.intern();
				//str = str.toLowerCase();

				Citation cit = null;
				if (useCRFLocal) {
					cit = new Citation(str, label, id.intValue(), ieInterface,
														 numNBest, nthViterbi);
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
		logger.fine("Time elapses " + timeElapse + " seconds for computing nodes.");
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

	public static void makeDistMetric(List list, StringDistance tfidf,
																		StringDistance triGramDistanceMetric) {
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

	public static InstanceList makePairs(Pipe instancePipe, ArrayList nodes) {
		return makePairs( instancePipe, nodes, 1.0 );
	}

	/** @param instancePipe to pipe instances through
	 * @param nodes Citation nodes
	 * @param negativeProb the probability of including each negative instance (to reduce class disparity)*/
	public static InstanceList makePairs(Pipe instancePipe, ArrayList nodes, double negativeProb) {
		logger.fine("PairIterator...");
		long timeStart = System.currentTimeMillis();
		InstanceList ilist = new InstanceList (instancePipe);
		ilist.add (new NodePairIterator (nodes, negativeProb) );
		logger.fine("====");
		long timeEnd = System.currentTimeMillis();
		double timeElapse = (timeEnd - timeStart)/(1000.000);
		logger.fine("Time elapses " + timeElapse + " seconds for computing pair iterator.");
		return ilist;
	}

	
		public static InstanceList makePairs(Pipe instancePipe, ArrayList nodes, List pairs) {
			logger.fine("PairIterator...");
			long timeStart = System.currentTimeMillis();
			InstanceList ilist = new InstanceList (instancePipe);
			ilist.add (new NodePairIterator (nodes, pairs) );
			logger.fine("====");
			long timeEnd = System.currentTimeMillis();
			double timeElapse = (timeEnd - timeStart)/(1000.000);
			logger.fine("Time elapses " + timeElapse + " seconds for computing pair iterator.");
			return ilist;
		}
	
	// this version assumes nodes are actually citations
	public static Collection makeCollections (ArrayList nodes) {
		HashMap map = new HashMap(); // keep an index of node label values to collections
		Collection collection = new LinkedHashSet();
		for (int i=0; i < nodes.size(); i++) {
	    Citation n = (Citation)nodes.get(i);
	    Object o1 = n.getLabel();
	    Collection c = (Collection)map.get(o1);
	    if (c != null) {
				c.add(n);
				//logger.fine("adding new node " + n + " to existing collection with " + o1);
	    } else {
				Collection newC = new LinkedHashSet();
				//logger.fine("Creating new collection -> id: " + o1);
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

	public static void evaluateClustering (Collection key, Collection pred,
																				 String description) {
		ClusterEvaluate evalTest = new ClusterEvaluate(key, pred);
		evalTest.evaluate();
		evalTest.printVerbose();
		PairEvaluate pairEvalTest = new PairEvaluate(key, pred);
		pairEvalTest.evaluate();
		
		//cl.exportGraph("/tmp/testGraphEdges");
		//eval.printVerbose();
		System.out.println ("EXPT: " + description);
		System.out.println("TESTING ObjFn Cluster F1: " + evalTest.getF1());
		System.out.println("TESTING ObjFn Cluster Recall: " + evalTest.getRecall());
		System.out.println("TESTING ObjFn Cluster Precision: " + evalTest.getPrecision());
		System.out.println("Number of clusters " + pred.size());
		System.out.println("TESTING ObjFn Pair F1: " + pairEvalTest.getF1());
		System.out.println("TESTING ObjFn Pair Recall: " + pairEvalTest.getRecall());
		System.out.println("TESTING ObjFn Pair Precision: " + pairEvalTest.getPrecision());		
	}
}
