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
import edu.umass.cs.mallet.base.pipe.iterator.AbstractPipeInputIterator;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.util.CommandOption;
import edu.umass.cs.mallet.base.util.MalletLogger;
import edu.umass.cs.mallet.base.util.RegexFileFilter;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.cluster.ConditionalClusterer;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.cluster.ConditionalClustererTrainer;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe.iterator.NodeClusterPairIterator;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe.iterator.VenuePaperClusterIterator;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Interface to train and test a ConditionalClusterer to cluster
 * Papers and Venues simultaneously. Uses Citeseer data. */
	 
public class JointConditionalClustererTUI {

	private static Logger logger = MalletLogger.getLogger(JointConditionalClustererTUI.class.getName());

	static CommandOption.SpacedStrings trainingDirs =	new CommandOption.SpacedStrings
	(JointConditionalClustererTUI.class, "training-dirs", "DIR...", true, null,
	 "The directories containing the citations to be clustered at training time. One file per cluster.", null);
	
	static CommandOption.SpacedStrings testingDirs =	new CommandOption.SpacedStrings
	(JointConditionalClustererTUI.class, "testing-dirs", "DIR...", true, null,
	 "The directories containing the citations to be clustered at test time. One file per cluster.", null);

	static CommandOption.Boolean randomOrderClustering = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "random-order-clustering", "BOOL", false, false,
	 "At test time, choose the nodes to consider at random", null);

	static CommandOption.Boolean sampleTrainingInstances = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "sample-training-instances", "BOOL", false, true,
	 "Generate instances by sampling from true clusters", null);

	static CommandOption.Integer numberTrainingInstances = new CommandOption.Integer
	(JointConditionalClustererTUI.class, "number-training-instances", "INTEGER", true, 5000,
	 "The number of training instances to sample", null);

	static CommandOption.Integer randomSeed = new CommandOption.Integer
	(JointConditionalClustererTUI.class, "random-seed", "INTEGER", true, 1,
	 "Seed for random number in random order clustering", null);

	static CommandOption.Integer numRandomTrials = new CommandOption.Integer
	(JointConditionalClustererTUI.class, "num-random-trials", "INTEGER", true, 5,
	 "number of random trials to run", null);

	static CommandOption.Boolean errorAnalysis = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "error-analysis", "BOOL", false, false,
	 "Print errors (False positives)", null);

	static CommandOption.Boolean useCRF = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "use-crf", "BOOL", false, false,
	 "Use CRF or not.", null);

	static CommandOption.Boolean useFeatureInduction = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "use-feature-induction", "BOOL", false, false,
	 "Use Feature Induction or Not.", null);

	static CommandOption.Boolean useClusterSize = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "use-cluster-size", "BOOL", true, true,
	 "add feature that is cluster's size", null);

	static CommandOption.Boolean useThereExists = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "use-there-exists", "BOOL", true, true,
	 "Use thereExists pipe.", null);

	static CommandOption.Boolean usePairwiseClassifier = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "use-pairwise-classifier", "BOOL", true, true,
	 "Use pairwise classifier to weight edges.", null);

	static CommandOption.Boolean useClusterHomogeneity = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "use-cluster-homogeneity", "BOOL", true, true,
	 "add feature that is within-cluster similarity.", null);

	static CommandOption.Boolean printInputAndTarget = new CommandOption.Boolean
	(JointConditionalClustererTUI.class, "print-input-and-target", "BOOL", false, false,
	 "Print features and target.", null);

	static CommandOption.String crfInputFile = new CommandOption.String
	(JointConditionalClustererTUI.class, "crf-input-file", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);
	
	static CommandOption.Integer numNBest = new CommandOption.Integer
	(JointConditionalClustererTUI.class, "num-n-best", "INTEGER", true, 3,
	 "Number of n-best candidates to store.", null);
	
	static CommandOption.Integer nthViterbi = new CommandOption.Integer
	(JointConditionalClustererTUI.class, "nth-viterbi", "INTEGER", true, 0,
	 "Number of n-best candidates to use .", null);

	static CommandOption.Double negativeClusterThreshold = new CommandOption.Double
	(JointConditionalClustererTUI.class, "negative-cluster-threshold", "DECIMAL", true, 0.0,
	 "Decision threhold to place a node in a cluster. Takes opposite of input because CommandOptions seem to have trouble with negative inputs", null);

	static CommandOption.Double positiveInstanceRatio = new CommandOption.Double
	(JointConditionalClustererTUI.class, "positive-instance-ratio", "DECIMAL", true, 0.1,
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
		// load papers
	  ArrayList[] paperTrainingNodes = createNodesFromFiles (trainingDirs.value(), ieInterface, CitationUtils.PAPER);
		ArrayList[] paperTestingNodes = createNodesFromFiles (testingDirs.value(), ieInterface, CitationUtils.PAPER);
		ArrayList allPaperTrainingNodes = new ArrayList();
		for (int i=0; i < paperTrainingNodes.length; i++)
			allPaperTrainingNodes.addAll (paperTrainingNodes[i]);
		ArrayList allPaperTestingNodes = new ArrayList();
		for (int i=0; i < paperTestingNodes.length; i++)
			allPaperTestingNodes.addAll (paperTestingNodes[i]);
		Collection paperTrainingTruth = CitationUtils.makeCollections (allPaperTrainingNodes);
		Collection paperTestingTruth = CitationUtils.makeCollections (allPaperTestingNodes);
		// load venues
	  ArrayList[] venueTrainingNodes = createNodesFromFiles (trainingDirs.value(), ieInterface, CitationUtils.VENUE);
		ArrayList[] venueTestingNodes = createNodesFromFiles (testingDirs.value(), ieInterface, CitationUtils.VENUE);
		ArrayList allVenueTrainingNodes = new ArrayList();
		for (int i=0; i < venueTrainingNodes.length; i++)
			allVenueTrainingNodes.addAll (venueTrainingNodes[i]);
		ArrayList allVenueTestingNodes = new ArrayList();
		for (int i=0; i < venueTestingNodes.length; i++)
			allVenueTestingNodes.addAll (venueTestingNodes[i]);		
		Collection venueTrainingTruth = CitationUtils.makeCollections (allVenueTrainingNodes);
		Collection venueTestingTruth = CitationUtils.makeCollections (allVenueTestingNodes);

		// train pairwise classifiers
		Classifier paperPairwiseClassifier = null;
		Classifier venuePairwiseClassifier = null;
		if (usePairwiseClassifier.value()) {
			System.err.println ("TRAINING PAIRWISE CLASSIFIERS");
			AbstractStatisticalTokenDistance distanceMetric =
				(AbstractStatisticalTokenDistance)CitationUtils.computeDistanceMetric (allPaperTrainingNodes);
			TFIDF tfidf = new TFIDF();
			NGramTokenizer nGramTokenizer =
				new NGramTokenizer(3,3,false, new SimpleTokenizer(true, true));				
			TFIDF triGramDistanceMetric = new TFIDF (nGramTokenizer);
			CitationUtils.makeDistMetric(allPaperTrainingNodes, tfidf, triGramDistanceMetric);						
			paperPairwiseClassifier = trainPairwiseClassifier (paperTrainingNodes, getPaperPipe(distanceMetric,
																																													triGramDistanceMetric));
			venuePairwiseClassifier = trainPairwiseClassifier (venueTrainingNodes, getVenuePipe(distanceMetric,
																																													triGramDistanceMetric));
		}
		// train solo clusterers
		AbstractPipeInputIterator paperInstanceIterator = new NodeClusterPairIterator (paperTrainingTruth,
																																									 new java.util.Random
																																									 (randomSeed.value()),
																																									 positiveInstanceRatio.value(),
																																									 sampleTrainingInstances.value(),
																																									 numberTrainingInstances.value());
		AbstractPipeInputIterator venueInstanceIterator = new NodeClusterPairIterator (venueTrainingTruth,
																																									 new java.util.Random
																																									 (randomSeed.value()),
																																									 positiveInstanceRatio.value(),
																																									 sampleTrainingInstances.value(),
																																									 numberTrainingInstances.value());		
		System.err.println ("TRAINING SOLO PAPER CLUSTERER");
		ConditionalClusterer paperClusterer = getClusterer (paperInstanceIterator,
																												getPaperClusterPipe(paperPairwiseClassifier));
		System.err.println ("TRAINING SOLO VENUE CLUSTERER");
		ConditionalClusterer venueClusterer  = getClusterer (venueInstanceIterator,
																												 getVenueClusterPipe(venuePairwiseClassifier));

		// train joint clusterer
		System.err.println ("TRAINING JOINT CLUSTERER.");
		Pipe p = getJointPipe (paperPairwiseClassifier, venuePairwiseClassifier,
													 paperClusterer.getClassifier(), venueClusterer.getClassifier());
		AbstractPipeInputIterator jointInstanceIterator = new VenuePaperClusterIterator (paperTrainingTruth,
																																										 venueTrainingTruth,
																																										 new java.util.Random
																																										 (randomSeed.value()),
																																										 positiveInstanceRatio.value());
		ConditionalClustererTrainer jointTrainer =
			new ConditionalClustererTrainer (p, -negativeClusterThreshold.value());
		ConditionalClusterer jointClusterer = jointTrainer.train (jointInstanceIterator, false);
		System.err.println ("DONE TRAINING JOINT CLUSTERER. BEGIN CLUSTERING.");
		Collection predictedClustering = null; 
		if (randomOrderClustering.value()) {
			for (int i=0; i < numRandomTrials.value(); i++) {
				predictedClustering = jointClusterer.clusterPapersAndVenues (allPaperTestingNodes, allVenueTestingNodes,
																																		paperTestingTruth, venueTestingTruth,
																																		paperClusterer.getClassifier(),
																																		venueClusterer.getClassifier(),
																																		new java.util.Random (randomSeed.value() + i*10));
				System.err.println ("FINISHED CLUSTERING. BEGIN EVALUATION.");				
				CitationUtils.evaluateClustering (paperTestingTruth,
																					getPaperClusters (predictedClustering),
																					"RANDOM TRIAL " + i + " PAPER COREFERENCE RESULTS");
				CitationUtils.evaluateClustering (venueTestingTruth,
																					getVenueClusters (predictedClustering),
																					"RANDOM TRIAL " + i + " VENUE COREFERENCE RESULTS");
			}
		}
	}

	private static ConditionalClusterer getClusterer (AbstractPipeInputIterator instanceIterator, Pipe p) {
		ConditionalClustererTrainer cct = new ConditionalClustererTrainer (p, -negativeClusterThreshold.value());
		return cct.train (instanceIterator, useFeatureInduction.value());
	}

	private static Collection getPaperClusters (Collection clustering) {
		Collection ret = new ArrayList();
		Iterator iter = clustering.iterator ();
		while (iter.hasNext()) {
			Collection cluster = (Collection) iter.next();
			Iterator subIter = cluster.iterator();
			Object node = subIter.next();
			if (node instanceof PaperCitation)
				ret.add (cluster);
			else if (!(node instanceof VenueCitation))
				throw new IllegalArgumentException ("Node is neither venue nor paper, it's a " + node.getClass().getName());
		}
		return ret;
	}

	private static Collection getVenueClusters (Collection clustering) {
		Collection ret = new ArrayList();
		Iterator iter = clustering.iterator ();
		while (iter.hasNext()) {
			Collection cluster = (Collection) iter.next();
			Iterator subIter = cluster.iterator();
			Object node = subIter.next();
			if (node instanceof VenueCitation)
				ret.add (cluster);
			else if (!(node instanceof PaperCitation))
				throw new IllegalArgumentException ("Node is neither venue nor paper, it's a " + node.getClass().getName());
		}
		return ret;
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

	private static Pipe getVenueClusterPipe (Classifier pairwiseClassifier) {
		// same for now
		return getPaperClusterPipe(pairwiseClassifier);
	}

	private static Pipe getPaperClusterPipe (Classifier pairwiseClassifier) {
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
	
	private static Pipe getPaperPipe (AbstractStatisticalTokenDistance distanceMetric,
																		TFIDF triGramDistanceMetric) {
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
	
	private static Pipe getVenuePipe (AbstractStatisticalTokenDistance distanceMetric,
																		TFIDF triGramDistanceMetric) {
		Pipe p = new SerialPipes (new Pipe[] {
	    new ExactFieldMatchPipe(Citation.corefFields),
	    new PageMatchPipe(),
	    new YearsWithinFivePipe(),
	    new FieldStringDistancePipe(triGramDistanceMetric, Citation.corefFields, "trigramTFIDF"),
	    new GlobalPipe(distanceMetric),
			new AuthorPipe(distanceMetric),
			new VenuePipe(distanceMetric),
			new VenueAcronymPipe(),
	    new HeuristicPipe(Citation.corefFields),
	    new InterFieldPipe(),
	    new NodePair2FeatureVector (),
	    new Target2Label (),
		});
		return p;
	}

	/** Create pipe for conditionalClusterer */
	private static Pipe getJointPipe (Classifier paperPairwiseClassifier, Classifier venuePairwiseClassifier,
																		Classifier paperClusterClassifier, Classifier venueClusterClassifier) {
		ArrayList pipes = new ArrayList ();
		pipes.add (new PaperClusterPrediction (paperClusterClassifier));
		pipes.add (new VenueClusterPrediction (venueClusterClassifier));
		/*if (useThereExists.value())
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
		*/
 		pipes.add (new VenuePaperCluster2FeatureVector ());
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
