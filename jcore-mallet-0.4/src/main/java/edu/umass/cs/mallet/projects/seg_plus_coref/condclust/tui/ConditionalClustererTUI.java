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

import com.wcohen.secondstring.AbstractStatisticalTokenDistance;
import com.wcohen.secondstring.TFIDF;
import com.wcohen.secondstring.tokens.NGramTokenizer;
import com.wcohen.secondstring.tokens.SimpleTokenizer;
import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.classify.MaxEnt;
import edu.umass.cs.mallet.base.classify.MaxEntTrainer;
import edu.umass.cs.mallet.base.classify.Trial;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.PrintInputAndTarget;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.Target2Label;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.util.CommandOption;
import edu.umass.cs.mallet.base.util.MalletLogger;
import edu.umass.cs.mallet.base.util.RegexFileFilter;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.cluster.ConditionalClusterer;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.cluster.ConditionalClustererTrainer;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Interface to train and test a ConditionalClusterer. Uses Citeseer
 * data. */
	 
public class ConditionalClustererTUI {

	private static Logger logger = MalletLogger.getLogger(ConditionalClustererTUI.class.getName());

	static CommandOption.SpacedStrings trainingDirs =	new CommandOption.SpacedStrings
	(ConditionalClustererTUI.class, "training-dirs", "DIR...", true, null,
	 "The directories containing the citations to be clustered at training time. One file per cluster.", null);
	
	static CommandOption.SpacedStrings testingDirs =	new CommandOption.SpacedStrings
	(ConditionalClustererTUI.class, "testing-dirs", "DIR...", true, null,
	 "The directories containing the citations to be clustered at test time. One file per cluster.", null);

	static CommandOption.Boolean randomOrderClustering = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "random-order-clustering", "BOOL", false, false,
	 "At test time, choose the nodes to consider at random", null);

	static CommandOption.Boolean sampleTrainingInstances = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "sample-training-instances", "BOOL", false, true,
	 "Generate instances by sampling from true clusters", null);

	static CommandOption.Integer numberTrainingInstances = new CommandOption.Integer
	(ConditionalClustererTUI.class, "number-training-instances", "INTEGER", true, 5000,
	 "The number of training instances to sample", null);

	static CommandOption.Integer randomSeed = new CommandOption.Integer
	(ConditionalClustererTUI.class, "random-seed", "INTEGER", true, 1,
	 "Seed for random number in random order clustering", null);

	static CommandOption.Integer numRandomTrials = new CommandOption.Integer
	(ConditionalClustererTUI.class, "num-random-trials", "INTEGER", true, 5,
	 "number of random trials to run", null);

	static CommandOption.Boolean errorAnalysis = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "error-analysis", "BOOL", false, false,
	 "Print errors (False positives)", null);

	static CommandOption.Boolean useCRF = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "use-crf", "BOOL", false, false,
	 "Use CRF or not.", null);

	static CommandOption.Boolean useFeatureInduction = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "use-feature-induction", "BOOL", false, false,
	 "Use Feature Induction or Not.", null);

	static CommandOption.Boolean useClusterSize = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "use-cluster-size", "BOOL", true, true,
	 "add feature that is cluster's size", null);

	static CommandOption.Boolean useThereExists = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "use-there-exists", "BOOL", true, true,
	 "Use thereExists pipe.", null);

	static CommandOption.Boolean usePairwiseClassifier = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "use-pairwise-classifier", "BOOL", true, true,
	 "Use pairwise classifier to weight edges.", null);

	static CommandOption.Boolean useClusterHomogeneity = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "use-cluster-homogeneity", "BOOL", true, true,
	 "add feature that is within-cluster similarity.", null);

	static CommandOption.Boolean printInputAndTarget = new CommandOption.Boolean
	(ConditionalClustererTUI.class, "print-input-and-target", "BOOL", false, false,
	 "Print features and target.", null);

	static CommandOption.String crfInputFile = new CommandOption.String
	(ConditionalClustererTUI.class, "crf-input-file", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);
	
	static CommandOption.Integer numNBest = new CommandOption.Integer
	(ConditionalClustererTUI.class, "num-n-best", "INTEGER", true, 3,
	 "Number of n-best candidates to store.", null);
	
	static CommandOption.Integer nthViterbi = new CommandOption.Integer
	(ConditionalClustererTUI.class, "nth-viterbi", "INTEGER", true, 0,
	 "Number of n-best candidates to use .", null);

	static CommandOption.Double negativeClusterThreshold = new CommandOption.Double
	(ConditionalClustererTUI.class, "negative-cluster-threshold", "DECIMAL", true, 0.0,
	 "Decision threhold to place a node in a cluster. Takes opposite of input because CommandOptions seem to have trouble with negative inputs", null);

	static CommandOption.Double positiveInstanceRatio = new CommandOption.Double
	(ConditionalClustererTUI.class, "positive-instance-ratio", "DECIMAL", true, 0.1,
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
			positiveInstanceRatio
		});
	
	
	public static void main (String[] args) {
		commandOptions.process (args);
		commandOptions.logOptions (logger);
		IEInterface ieInterface = loadIEInterface ();
	  ArrayList[] trainingNodes = createNodesFromFiles (trainingDirs.value(), ieInterface, CitationUtils.PAPER);
		ArrayList[] testingNodes = createNodesFromFiles (testingDirs.value(), ieInterface, CitationUtils.PAPER);
		ArrayList allTrainingNodes = new ArrayList();
		for (int i=0; i < trainingNodes.length; i++)
			allTrainingNodes.addAll (trainingNodes[i]);
		ArrayList allTestingNodes = new ArrayList();
		for (int i=0; i < testingNodes.length; i++)
			allTestingNodes.addAll (testingNodes[i]);
		
		Collection trainingTruth = CitationUtils.makeCollections (allTrainingNodes);
		Collection testingTruth = CitationUtils.makeCollections (allTestingNodes);

		Classifier pairwiseClassifier = null;
		if (usePairwiseClassifier.value()) 
			pairwiseClassifier = trainPairwiseClassifier (trainingNodes, getPaperPipe(allTrainingNodes));
		Pipe p = getPipe (pairwiseClassifier);
		ConditionalClustererTrainer cct =
			new ConditionalClustererTrainer (p, -negativeClusterThreshold.value());
		ConditionalClusterer cc = cct.train (trainingTruth, useFeatureInduction.value(),
																				 sampleTrainingInstances.value(), positiveInstanceRatio.value(),
																				 numberTrainingInstances.value());
		System.err.println ("FINISHED TRAINING. BEGIN CLUSTERING.");
		Collection predictedClustering = null; 
		if (randomOrderClustering.value()) {
			for (int i=0; i < numRandomTrials.value(); i++) {
				predictedClustering =
					cc.clusterRandom (allTestingNodes,
														errorAnalysis.value() ? testingTruth : null,
														new java.util.Random (randomSeed.value() + i*10));
				System.err.println ("FINISHED CLUSTERING. BEGIN EVALUATION.");
				CitationUtils.evaluateClustering (testingTruth,
																					predictedClustering, "RANDOM TRIAL " + i + " COREFERENCE RESULTS");
			}
		}
		else {
			predictedClustering =
				cc.cluster (allTestingNodes, errorAnalysis.value() ? testingTruth : null);
			CitationUtils.evaluateClustering (testingTruth, predictedClustering, "GREEDY COREFERENCE RESULTS");
		}
	}
	
	private static Classifier trainPairwiseClassifier (ArrayList[] nodes, Pipe p) {
			InstanceList ilist = new InstanceList (p);
		for (int i=0; i < nodes.length; i++) 
			ilist.add (CitationUtils.makePairs (p, nodes[i]));
		MaxEnt me = (MaxEnt)(new MaxEntTrainer().train(ilist, null, null, null, null));
		ilist.getDataAlphabet().stopGrowth();
		Trial t = new Trial(me, ilist);
		System.out.println("Pairwise classifier: -> Training F1 on \"yes\" is: " + t.labelF1("yes"));
		return me;
	}

	private static Pipe getPaperPipe (ArrayList nodes) {
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
	private static Pipe getPipe (Classifier pairwiseClassifier) {
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
	private static IEInterface loadIEInterface () {
		IEInterface iei = null;
		if (useCRF.value()) {
			File crfFile = new File(crfInputFile.value());
			iei= new IEInterface(crfFile);
			iei.loadCRF(crfFile);			
		}
		return iei;
	}

	/** Read citation files and create nodes */
	private static ArrayList[] createNodesFromFiles (String[] dirNames, IEInterface ieInterface, String type) {
		ArrayList[] ret = new ArrayList[dirNames.length];
		ArrayList files = new ArrayList();
		for (int i=0; i < dirNames.length; i++) {
			FileIterator fi = new FileIterator (new File(dirNames[i]), new RegexFileFilter(Pattern.compile(".*"))); 
			ret[i] = CitationUtils.computeNodes (fi.getFileArray(), ieInterface, useCRF.value(),
																					 numNBest.value(), nthViterbi.value(), type);
		}
		return ret;
	}
}
