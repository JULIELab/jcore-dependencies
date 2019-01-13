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

import salvo.jesus.graph.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.graphs.*;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import java.io.*;
import java.util.*;

public class ClusterLearnerAvg extends ClusterLearner
{

	public ClusterLearnerAvg (int numEpochs, Set trainingDocuments, Pipe p,
														MaxEnt classifier, int yI, int nI)
	{
		super(numEpochs, trainingDocuments, p, classifier, yI, nI);
	}	
    public ClusterLearnerAvg ( int numEpochs, Set trainingDocuments, Pipe p,
															 int yI, int nI )
    {
	// documentTrInstances should be a set of Instance.List types
	// where each Instance.List is the set of training instances for that document
			super (numEpochs, trainingDocuments, p, yI, nI);
    }

    public void startTrainingAvg (Set testDocInstances)
    {
	Clusterer clusterer = new Clusterer();
	int defaultFeatureIndex = pipe.getDataAlphabet().size();
	System.out.println("Feature vector size: " + defaultFeatureIndex);
	int numFeatures = defaultFeatureIndex + 1; // +1 for the default feature

	double decayRate = 0.99;

	Alphabet trainingVocab = pipe.getDataAlphabet();
	int numInstances = trainingDocuments.size();
	int numAverages = numInstances * numEpochs;
	// change these to 
	Matrix2 lambdasHistory[] = new Matrix2[numAverages]; 
	Matrix2 constraints[] = new Matrix2[numInstances];
	Matrix2 expectations = new Matrix2(2, numFeatures);
	Matrix2 lambdas      = new Matrix2(2, numFeatures);

	Matrix2 avgDifferences = new Matrix2(2, numFeatures);

	if (initialLambdas == null)
	    lambdas = new Matrix2(2, numFeatures);
	else
	    lambdas = initialLambdas;

	//get constraints first
	Iterator iter = trainingDocuments.iterator();
	int documentIndex = 0;

	// this loop gets the constraints - i.e. the expected values for features over EACH DOCUMENT
	while (iter.hasNext()) {
	    constraints[documentIndex] = new Matrix2(2, numFeatures);
	    List trainingMentionPairs = (List) iter.next();
	    Iterator pIterator = trainingMentionPairs.iterator();
	    int corefIndex;
	    while (pIterator.hasNext()) {
		Instance mentionPair = (Instance)pIterator.next();
		FeatureVector vec = (FeatureVector) mentionPair.getData();
		MentionPair pair = (MentionPair)mentionPair.getSource();
		if (pair.getEntityReference() != null)
		    corefIndex = yesIndex;
		else
		    corefIndex = noIndex;
		constraints[documentIndex].rowPlusEquals (corefIndex, vec, 1.0);
		constraints[documentIndex].plusEquals (corefIndex, defaultFeatureIndex, 1.0);
	    }
	    documentIndex++;
	}

	int averageIndex = 0;
	for (int epoch = 0; epoch < numEpochs-1; epoch++) {

	    
	    Iterator iter1 = trainingDocuments.iterator();
	    int docIndex = 0;
	    double epochTotal = 0.0;
	    double epochTotalPairWiseRecall = 0.0;
	    double epochTotalPairWisePrecision = 0.0;
	    double normalizer = 0.0;
	    while (iter1.hasNext()) {  // iterates over doc training instances

		//System.out.println("Constraints: at " + docIndex + ":" + constraints[docIndex].toString());
		//lambdasHistory[averageIndex] = new Matrix2(2, numFeatures);
		// We should actually reuse the same graphs over training epochs
		// since the graph structures for those documents are unchanged
		//  -- we only need to update the edge weights
		MappedGraph graph = new MappedGraph(); // graph to build to get clusters out of
		// Create the graph with all the correct edge weights, using the current (averaged?) lambdas
		List trainingMentionPairs = (List)iter1.next();
		Iterator pairIterator = trainingMentionPairs.iterator();
		System.out.println("Number of pairs: " + trainingMentionPairs.size());
		int numMentions = 0;
		Mention ref1 = null;
		while (pairIterator.hasNext()) {
		    Instance mentionPair = (Instance)pairIterator.next();
		    // xxx Do the inference with the latest single lambdas, or the average of lambdas[]?
		    super.constructEdges (graph, mentionPair, lambdas);
		    Mention cref = ((MentionPair)mentionPair.getSource()).getReferent();
		    if ((cref != ref1)) {
			ref1 = cref;
			numMentions++;
		    }
		}

		// Do inference
		clusterer.setGraph(graph);
		// evaluate for debugging purposes
		KeyClustering keyClustering = TUIGraph.collectAllKeyClusters(trainingMentionPairs);
		Clustering clustering = clusterer.getClustering(); // this could have memory of graphs
		//System.out.println("Clustering at: " + epoch);
		//clustering.printDetailed();
		ClusterEvaluate eval1 = new ClusterEvaluate (keyClustering,
							     clustering);
		PairEvaluate pEval1 = new PairEvaluate (keyClustering, clustering);
		pEval1.evaluate();
		eval1.evaluate();
		//eval1.printErrors(true);
		epochTotal += eval1.getF1()*(double)numMentions;
		epochTotalPairWiseRecall += pEval1.getRecall()*(double)numMentions;
		epochTotalPairWisePrecision += pEval1.getPrecision()*(double)numMentions;
		Iterator pairIterator1 = trainingMentionPairs.iterator();
		int numPairs = 0;
		while (pairIterator1.hasNext()) {
		    Instance mentionPair = (Instance)pairIterator1.next();
		    FeatureVector vec = (FeatureVector) mentionPair.getData();
		    MentionPair p = (MentionPair)mentionPair.getSource();
		    Mention ant = p.getAntecedent();
		    Mention ref = p.getReferent();
		    int corefIndex = clustering.inSameCluster(ant,ref) ? yesIndex : noIndex;
		    expectations.rowPlusEquals (corefIndex, vec, 1.0);
		    expectations.plusEquals (corefIndex, defaultFeatureIndex, 1.0);
		    numPairs++;
		}
		//System.out.println("Expectations via data: " + expectations.toString());
		// Do a percepton update of the lambdas parameters
		//System.out.println("Expectations before: " + expectations.toString());
		expectations.timesEquals (-1.0);
		DenseVector v0 = super.getDenseVectorOf(0, constraints[docIndex]);
		DenseVector v1 = super.getDenseVectorOf(1, constraints[docIndex]);
		expectations.rowPlusEquals (0, v0, 1.0);
		expectations.rowPlusEquals (1, v1, 1.0);

		DenseVector e0 = super.getDenseVectorOf(0, expectations);
		DenseVector e1 = super.getDenseVectorOf(1, expectations);
		e0.timesEquals((1.0/(double)numPairs) * Math.pow(decayRate, epoch));
		e1.timesEquals((1.0/(double)numPairs) * Math.pow(decayRate, epoch));		

		// on the fly averaging of constraints - expectations
		avgDifferences.timesEquals (epoch);
		avgDifferences.rowPlusEquals (0, e0, 1.0);
		avgDifferences.rowPlusEquals (1, e1, 1.0);
		avgDifferences.timesEquals (1.0/(double)(epoch + 1));

		expectations.timesEquals(0.0); // reset local expectaions counter
		//System.out.println("Lambdas, current: " + lambdas[averageIndex].toString());
		averageIndex++;
		docIndex++;
		normalizer += numMentions;
	    }
	    //System.out.println("Epoch " + epoch + " - cur lambdas: " + lambdas);
	    //System.out.println("Epoch " + epoch + " - avgDifferences: " + avgDifferences);
	    DenseVector avg0 = super.getDenseVectorOf(0, avgDifferences);
	    DenseVector avg1 = super.getDenseVectorOf(1, avgDifferences);

	    //lambdas.timesEquals (epoch);
	    lambdas.rowPlusEquals (0, avg0, 1.0);
	    lambdas.rowPlusEquals (1, avg1, 1.0);
	    //lambdas.timesEquals (1.0/(double)(epoch + 1));


	    //avgDifferences.timesEquals(0.0);

	    double pairF1 = (2.0 * epochTotalPairWiseRecall  * epochTotalPairWisePrecision) / 
		(epochTotalPairWiseRecall + epochTotalPairWisePrecision);

	    System.out.println("Epoch #" + epoch +" training Cluster F1: " + (epochTotal / (double)normalizer));
	    System.out.println("Epoch #" + epoch +" training Pairwise F1: " + (pairF1 / (double)normalizer));
	    System.out.println(" -- training recall: " + (epochTotalPairWiseRecall / (double)normalizer));	    
	    System.out.println(" -- training precision: " + (epochTotalPairWisePrecision / (double)normalizer));	    
	    System.out.println("Epoch testing: ");
	}


	finalLambdas = lambdas;
    }

}    
    
