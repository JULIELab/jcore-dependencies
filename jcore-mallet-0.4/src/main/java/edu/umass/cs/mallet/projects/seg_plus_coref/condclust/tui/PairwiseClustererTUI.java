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
	 @author Aron Culotta
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.tui;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.cluster.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;

import com.wcohen.secondstring.*;
import com.wcohen.secondstring.tokens.NGramTokenizer;
import com.wcohen.secondstring.tokens.SimpleTokenizer;

import java.util.logging.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/** Interface to train and test a PairwiseClusterer. Uses Citeseer
 * data.  Note - to port this to Rexa, I had to bring over a few lib
 * dependencies (secondstring, jgraph, old version of jdom,
 * mallet-deps...) */
	 
public class PairwiseClustererTUI {

	private static Logger logger = MalletLogger.getLogger(PairwiseClustererTUI.class.getName());

	static CommandOption.SpacedStrings trainingDirs =	new CommandOption.SpacedStrings
	(PairwiseClustererTUI.class, "training-dirs", "DIR...", true, null,
	 "The directories containing the citations to be clustered at training time. One file per cluster.", null);
	
	static CommandOption.SpacedStrings testingDirs =	new CommandOption.SpacedStrings
	(PairwiseClustererTUI.class, "testing-dirs", "DIR...", true, null,
	 "The directories containing the citations to be clustered at test time. One file per cluster.", null);

	static CommandOption.Boolean randomOrderClustering = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "random-order-clustering", "BOOL", false, false,
	 "At test time, choose the nodes to consider at random", null);

	static CommandOption.Boolean sampleTrainingInstances = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "sample-training-instances", "BOOL", false, true,
	 "Generate instances by sampling from true clusters", null);

	static CommandOption.Integer numberTrainingInstances = new CommandOption.Integer
	(PairwiseClustererTUI.class, "number-training-instances", "INTEGER", true, 5000,
	 "The number of training instances to sample", null);

	static CommandOption.Integer randomSeed = new CommandOption.Integer
	(PairwiseClustererTUI.class, "random-seed", "INTEGER", true, 1,
	 "Seed for random number in random order clustering", null);

	static CommandOption.Integer numRandomTrials = new CommandOption.Integer
	(PairwiseClustererTUI.class, "num-random-trials", "INTEGER", true, 5,
	 "number of random trials to run", null);

	static CommandOption.Boolean errorAnalysis = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "error-analysis", "BOOL", false, false,
	 "Print errors (False positives)", null);

	static CommandOption.Boolean useCRF = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "use-crf", "BOOL", false, false,
	 "Use CRF or not.", null);

	static CommandOption.Boolean useFeatureInduction = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "use-feature-induction", "BOOL", false, false,
	 "Use Feature Induction or Not.", null);

	static CommandOption.Boolean useClusterSize = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "use-cluster-size", "BOOL", true, true,
	 "add feature that is cluster's size", null);

	static CommandOption.Boolean useThereExists = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "use-there-exists", "BOOL", true, true,
	 "Use thereExists pipe.", null);

	static CommandOption.Boolean usePairwiseClassifier = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "use-pairwise-classifier", "BOOL", true, true,
	 "Use pairwise classifier to weight edges.", null);

	static CommandOption.Boolean useClusterHomogeneity = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "use-cluster-homogeneity", "BOOL", true, true,
	 "add feature that is within-cluster similarity.", null);

	static CommandOption.Boolean printInputAndTarget = new CommandOption.Boolean
	(PairwiseClustererTUI.class, "print-input-and-target", "BOOL", false, false,
	 "Print features and target.", null);

	static CommandOption.String crfInputFile = new CommandOption.String
	(PairwiseClustererTUI.class, "crf-input-file", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);
	
	static CommandOption.File classifierFile = new CommandOption.File
	(PairwiseClustererTUI.class, "classifier-file", "FILENAME", false, null,
	 "The name of the file to save the trained classifier to.", null);
	
	static CommandOption.Integer numNBest = new CommandOption.Integer
	(PairwiseClustererTUI.class, "num-n-best", "INTEGER", true, 3,
	 "Number of n-best candidates to store.", null);
	
	static CommandOption.Integer nthViterbi = new CommandOption.Integer
	(PairwiseClustererTUI.class, "nth-viterbi", "INTEGER", true, 0,
	 "Number of n-best candidates to use .", null);

	static CommandOption.Double negativeClusterThreshold = new CommandOption.Double
	(PairwiseClustererTUI.class, "negative-cluster-threshold", "DECIMAL", true, 0.0,
	 "Decision threhold to place a node in a cluster. Takes opposite of input because CommandOptions seem to have trouble with negative inputs", null);

	static CommandOption.Double positiveInstanceRatio = new CommandOption.Double
	(PairwiseClustererTUI.class, "positive-instance-ratio", "DECIMAL", true, 0.1,
	 "Ratio of positive to negative training instances", null);

	static final CommandOption.List commandOptions =
	new CommandOption.List (
		"Training and testing a conditional clusterer.",
		new CommandOption[] {
			trainingDirs,
			testingDirs,
			sampleTrainingInstances,
			numberTrainingInstances,
			errorAnalysis,
			useCRF,
			useFeatureInduction,
			crfInputFile,
			numNBest,
			nthViterbi,
			negativeClusterThreshold,
			randomOrderClustering,
			randomSeed,
			numRandomTrials,
			usePairwiseClassifier,
			useThereExists,
			useClusterSize,
			useClusterHomogeneity,
			printInputAndTarget,
			positiveInstanceRatio,
			classifierFile
		});
	

	public PairwiseClustererTUI () {
	}
	
	public static void main (String[] args) {
		run (args);
	}
	
	public static void run (String[] args) {
		commandOptions.process (args);
		commandOptions.logOptions (logger);
		PairwiseClustererTUI pc = new PairwiseClustererTUI ();
		IEInterface ieInterface = pc.loadIEInterface ();
	  ArrayList[] trainingNodes = pc.createNodesFromFiles (trainingDirs.value(), ieInterface, CitationUtils.PAPER);
		ArrayList[] testingNodes = testingDirs.value() == null ? null :
															 pc.createNodesFromFiles (testingDirs.value(), ieInterface, CitationUtils.PAPER);
		ArrayList allTrainingNodes = new ArrayList();
		for (int i=0; i < trainingNodes.length; i++)
			allTrainingNodes.addAll (trainingNodes[i]);
		Collection trainingTruth = CitationUtils.makeCollections (allTrainingNodes);

		ArrayList allTestingNodes = null;
		Collection testingTruth = null;
		if (testingNodes != null) {
			allTestingNodes = new ArrayList();		
			for (int i=0; i < testingNodes.length; i++)
				allTestingNodes.addAll (testingNodes[i]);
			testingTruth = CitationUtils.makeCollections (allTestingNodes);
		}

		Classifier pairwiseClassifier = pc.trainPairwiseClassifier (trainingNodes, pc.getPaperPipe(allTrainingNodes));
		Pipe p = pc.getPipe (pairwiseClassifier);
		System.err.println ("FINISHED TRAINING.");
		
		if (testingNodes != null) {
			System.err.println ("BEGIN TESTING.");
			Collection predictedClustering = null; 
			if (randomOrderClustering.value()) {
				for (int i=0; i < numRandomTrials.value(); i++) {
				}
				System.err.println ("FINISHED CLUSTERING. BEGIN EVALUATION.");
			}			
			else {
				CitationUtils.evaluateClustering (testingTruth, predictedClustering, "GREEDY COREFERENCE RESULTS");
			}			
		}
		if (classifierFile.value() != null) {
			System.err.println ("Saving classifier to " + classifierFile.value());
			try {
				ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (classifierFile.value()));
				oos.writeObject (pairwiseClassifier);
				oos.close();
			} catch (IOException e) {
				System.err.println ("Exception writing classifier: " + e);
			}
		}
	}
	
	public Classifier trainPairwiseClassifier (ArrayList[] nodes, Pipe p) {
			InstanceList ilist = new InstanceList (p);
		for (int i=0; i < nodes.length; i++) 
			ilist.add (CitationUtils.makePairs (p, nodes[i]));
		System.err.println ("Training size: " + ilist.size() + "\tNum features: " + ilist.getDataAlphabet().size());
		MaxEnt me = (MaxEnt)(new MaxEntTrainer().train(ilist, null, null, null, null));
		ilist.getDataAlphabet().stopGrowth();
		Trial t = new Trial(me, ilist);
		System.out.println("Pairwise classifier: -> Training F1 on \"yes\" is: " + t.labelF1("yes"));
		System.out.println("Pairwise classifier: -> Training F1 on \"no\" is: " + t.labelF1("no"));
		return me;
	}

	public Pipe getPaperPipe (ArrayList nodes) {
		AbstractStatisticalTokenDistance distanceMetric =
	    (AbstractStatisticalTokenDistance)CitationUtils.computeDistanceMetric (nodes);
		TFIDF tfidf = new TFIDF();
		NGramTokenizer nGramTokenizer =
	    new NGramTokenizer(3,3,false, new SimpleTokenizer(true, true));				
		TFIDF triGramDistanceMetric = new TFIDF (nGramTokenizer);
		CitationUtils.makeDistMetric(nodes, tfidf, triGramDistanceMetric);

		Pipe p = new SerialPipes (new Pipe[] {
	    new ExactFieldMatchPipe(Citation.corefFields),			
	    new PageMatchPipe(),
	    new YearsWithinFivePipe(),
	    new FieldStringDistancePipe(triGramDistanceMetric, Citation.corefFields, "trigramTFIDF"),			
	    new GlobalPipe(distanceMetric),
	    new AuthorPipe(distanceMetric),
	    new HeuristicPipe(Citation.corefFields),
	    new InterFieldPipe(),
	    new NodePair2FeatureVector (),
	    new Target2Label (),
		});
		return p;
	}
	
	/** Create pipe for conditionalClusterer */
	public Pipe getPipe (Classifier pairwiseClassifier) {
		ArrayList pipes = new ArrayList ();
		pipes.add (new ForAll (Citation.corefFields));
		if (useThereExists.value())
			pipes.add (new ThereExists(Citation.corefFields));
		if (pairwiseClassifier != null) {
			//pipes.add (new ClosestSingleLink (pairwiseClassifier, true));
			//pipes.add (new FarthestSingleLink (pairwiseClassifier));
			//pipes.add (new AverageLink (pairwiseClassifier));
			//pipes.add (new NNegativeNodes (pairwiseClassifier, 1));
			// previous 4 pipes subsumed by AllLinks - saves time
			pipes.add (new AllLinks (pairwiseClassifier));
			if (useClusterHomogeneity.value())
				pipes.add (new ClusterHomogeneity(pairwiseClassifier));
		}
		if (useClusterSize.value())
			pipes.add (new ClusterSize ());
		//  didn't help:  pipes.add (new ThereExistsMatch (new NeedlemanWunsch()));
		pipes.add (new NodeClusterPair2FeatureVector ());
		if (printInputAndTarget.value())
			pipes.add (new PrintInputAndTarget());
	  pipes.add (new Target2Label ());
		Pipe p = new SerialPipes ((Pipe[])pipes.toArray (new Pipe[] {}));
		return p;
	}

	/** if useCRF==true, load the CRF and create a IEInterface object to
	 * be used during coref*/
	public IEInterface loadIEInterface () {
		IEInterface iei = null;
		if (useCRF.value()) {
			File crfFile = new File(crfInputFile.value());
			iei= new IEInterface(crfFile);
			iei.loadCRF(crfFile);			
		}
		return iei;
	}

	/** Read citation files and create nodes */
	public ArrayList[] createNodesFromFiles (String[] dirNames, IEInterface ieInterface, String type) {
		ArrayList[] ret = new ArrayList[dirNames.length];
		ArrayList files = new ArrayList();
		for (int i=0; i < dirNames.length; i++) {
			//FileIterator fi = new FileIterator (new File(dirNames[i]), new RegexFileFilter(Pattern.compile(".*"))); 
			FileIterator fi = new FileIterator (new File(dirNames[i]), Pattern.compile(".*")); 
			ret[i] = CitationUtils.computeNodes (fi.getFileArray(), ieInterface, useCRF.value(),
																					 numNBest.value(), nthViterbi.value(), type);
		}
		return ret;
	}
}
