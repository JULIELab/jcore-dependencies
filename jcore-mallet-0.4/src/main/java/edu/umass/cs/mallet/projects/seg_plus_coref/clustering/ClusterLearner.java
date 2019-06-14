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

import edu.umass.cs.mallet.base.classify.MaxEnt;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.Mention;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.MentionPair;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.TUIGraph;

import java.lang.reflect.Array;
import java.util.*;

public class ClusterLearner
{
	int numEpochs = 15;
	Set trainingDocuments;  // instances are documents (of instances) here
	Pipe pipe;
	Matrix2 finalLambdas;
	Matrix2 initialLambdas;
	int yesIndex = -1;
	int noIndex = -1;
    
	public ClusterLearner (int numEpochs, Set trainingDocuments, Pipe p,
												 MaxEnt classifier, int yesIndex, int noIndex)
	{
		this(numEpochs, trainingDocuments, p, yesIndex, noIndex);
		double [] rawParams = classifier.getParameters();
		this.initialLambdas = new Matrix2(rawParams,2,Array.getLength(rawParams)/2);
		this.finalLambdas   = initialLambdas;
	}
	public ClusterLearner ( int numEpochs, Set trainingDocuments, Pipe p, int
													yesIndex, int noIndex )
	{
		// documentTrInstances should be a set of InstanceList types
		// where each InstanceList is the set of training instances for that document
		this.numEpochs = numEpochs;
		this.trainingDocuments = trainingDocuments;
		this.pipe = p;
		this.yesIndex = yesIndex;
		this.noIndex = noIndex;
	}

	protected double[][] getInitializedMatrix (int d1, int d2)
	{
		double matrix[][] = new double[d1][d2];
		for (int i=0; i < d1 ; i++) {
	    for (int j=0; j < d2;j++) {
				matrix[i][j] = 0;
	    }
		}
		return matrix;
	}
    
	public void initializePrevClusterings (HashMap map)
	{
		Clusterer clusterer = new Clusterer();
		Iterator iter = trainingDocuments.iterator();
		MappedGraph graph = new MappedGraph();
		while (iter.hasNext()) {
	    List trainingMentionPairs = (List)iter.next();
	    Iterator pairIterator = trainingMentionPairs.iterator();
	    while (pairIterator.hasNext()) {
				Instance mentionPair = (Instance)pairIterator.next();
				// xxx Do the inference with the latest single lambdas, or the average of lambdas[]?
				constructEdges (graph, mentionPair, initialLambdas);
	    }
	    clusterer.setGraph(graph);
	    Clustering cl = clusterer.getClustering(null);
	    map.put(trainingMentionPairs, cl);
		}
	}

	public void startTraining (Set testDocInstances)
	{
		Clusterer clusterer = new Clusterer();
		int defaultFeatureIndex = pipe.getDataAlphabet().size();
		System.out.println("Feature vector size: " + defaultFeatureIndex);

		int numFeatures = defaultFeatureIndex + 1; // +1 for the default feature
	
		//HashMap prevClusterings = new HashMap();
		//initializePrevClusterings(prevClusterings);

		double decayRate = 0.9;

		Alphabet trainingVocab = pipe.getDataAlphabet();
		int numInstances = trainingDocuments.size();
		int numAverages = numInstances * numEpochs;
		//	Matrix2 lambdasHistory[] = new Matrix2[numAverages]; 
		Matrix2 constraints[] = new Matrix2[numInstances];
		Matrix2 expectations = new Matrix2(2, numFeatures);
		Matrix2 lambdas = null;
	

		if (initialLambdas == null)
	    lambdas = new Matrix2(2, numFeatures);	
		else
	    lambdas = initialLambdas;

		Matrix2 expectationsSum = new Matrix2(2, numFeatures);
		//get constraints first
		Iterator iter = trainingDocuments.iterator();
		int documentIndex = 0;

		// this loop gets the constraints - i.e. the expected values for features over EACH DOCUMENT
		while (iter.hasNext()) {
	    constraints[documentIndex] = new Matrix2(2, numFeatures);
	    List trainingMentionPairs = (List) iter.next();
	    Iterator pIterator = trainingMentionPairs.iterator();
	    int corefIndex = -1;
	    //KeyClustering keyClustering = TUIGraph.collectAllKeyClusters(trainingMentionPairs);
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
	    //System.out.println("Key clustering: ");
	    //keyClustering.printDetailed();
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
				int numMentions = 1;
				Mention ref1 = null;
				while (pairIterator.hasNext()) {
					Instance mentionPair = (Instance)pairIterator.next();
					// xxx Do the inference with the latest single lambdas, or the average of lambdas[]?
					constructEdges (graph, mentionPair, lambdas);
					Mention cref = ((MentionPair)mentionPair.getSource()).getReferent();
					if ((cref != ref1)) {
						ref1 = cref;
						//numMentions++;
					}
				}
				// Do inference
				clusterer.setGraph(graph);
				// evaluate for debugging purposes
				KeyClustering keyClustering = TUIGraph.collectAllKeyClusters(trainingMentionPairs);
				Clustering clustering = clusterer.getClustering(); 
				//System.out.println("Clustering at: " + epoch);
				//clustering.printDetailed();
				ClusterEvaluate eval1 = new ClusterEvaluate (keyClustering, clustering);
				PairEvaluate pEval1 = new PairEvaluate (keyClustering, clustering);
				pEval1.evaluate();
				eval1.evaluate();
				//System.out.println("Error analysis: ");
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
				DenseVector v0 = getDenseVectorOf(0, constraints[docIndex]);
				DenseVector v1 = getDenseVectorOf(1, constraints[docIndex]);
				expectations.rowPlusEquals (0, v0, 1.0);
				expectations.rowPlusEquals (1, v1, 1.0);
				DenseVector e0 = getDenseVectorOf(0, expectations);
				DenseVector e1 = getDenseVectorOf(1, expectations);
				//System.out.println("Expecations after: " + expectations.toString());
				//System.out.println("Expectations 0: "); 
				//e0.print();
				//System.out.println("Constraints  0: ");
				//v0.print();
				//System.out.println("Lambdas before" + lambdas.toString());
				e0.timesEquals((1.0/(double)numPairs) * Math.pow(decayRate,epoch));
				e1.timesEquals((1.0/(double)numPairs) * Math.pow(decayRate,epoch));				
				lambdas.rowPlusEquals (0, e0, 1.0);
				lambdas.rowPlusEquals (1, e1, 1.0);
				//System.out.println("Lambdas after: " + lambdas.toString());
				expectations.timesEquals (0.0); // need to reset expectation (this is the experimental count)
				averageIndex++;
				docIndex++;
				normalizer += numMentions;
				//		prevClusterings.put(trainingMentionPairs, clustering);
	    }
	    double pairF1 = (2.0 * epochTotalPairWiseRecall  * epochTotalPairWisePrecision) / 
											(epochTotalPairWiseRecall + epochTotalPairWisePrecision);

	    System.out.println("Epoch #" + epoch +" training Cluster F1: " + (epochTotal / (double)normalizer));
	    System.out.println("Epoch #" + epoch +" training Pairwise F1: " + (pairF1 / (double)normalizer));
	    System.out.println(" -- training recall: " + (epochTotalPairWiseRecall / (double)normalizer));	    
	    System.out.println(" -- training precision: " + (epochTotalPairWisePrecision / (double)normalizer));	    
	    System.out.println("Epoch testing: ");
	    //testCurrentModel(testDocInstances, lambdas, instancePipe);
		}
		// Iterate through testing documents
		//   Iterate through mention pairs
		//     wp[p] = the w+() for this pair using exp (fv.dotProduct(lambda+))
		//     wp[n] = the w-() for this pair using exp (fv.dotProduct(lambda-))
		//   Run the graph clustering algorithm, which results in +/- labels on each pair
		//   Compare graph clusterings' +/- with truth to evaluate
	
		// need a method to average lambdas
		// use methods in Matrix2 to do the averaging
		// plusEquals and timesEquals
		finalLambdas = lambdas;
		//printLambdas(lambdas);
	}

	protected void testCurrentModel (Set testDocInstances, Matrix2 lambdas)
	{
		Iterator iter1 = testDocInstances.iterator();
		Clusterer clusterer = new Clusterer();
		double total = 0.0;
		double totalPairwise = 0.0;
		int cnt = 0;
		while (iter1.hasNext()) {
	    LinkedHashSet keyClusters = new LinkedHashSet();
	    MappedGraph graph = new MappedGraph(); // need a MappedGraph because we need to be able to copy
	    // Create the graph with all the correct edge weights, using the current (averaged?) lambdas
	    List testMentionPairs = (List)iter1.next();
	    KeyClustering keyClustering = TUIGraph.collectAllKeyClusters(testMentionPairs);
	    //keyClustering.print();
	    System.out.println("Number of pairs: " + testMentionPairs.size());
	    Iterator trPairIterator = testMentionPairs.iterator();
	    int numMentions = 0;
	    Mention ref = null;
	    while (trPairIterator.hasNext()) {
		    Instance mentionPair = (Instance)trPairIterator.next();
		    Mention curRef = ((MentionPair)mentionPair.getSource()).getReferent();
		    if (curRef != ref) {
					numMentions++;
					ref = curRef;
		    }
		    //constructEdgesUsingTargets (graph, mentionPair);
		    TUI.constructEdgesUsingTrainedClusterer (graph, mentionPair, lambdas, pipe);
		    //coalesceNewPair (keyClusters, mentionPair);
			}	    
	    clusterer.setGraph(graph);
	    Clustering clustering = clusterer.getClustering();
	    ClusterEvaluate eval1 = new ClusterEvaluate (keyClustering, clustering);
	    eval1.evaluate();
	    total += eval1.getF1()*(double)numMentions;
	    PairEvaluate pEval1 = new PairEvaluate (keyClustering, clustering);
	    pEval1.evaluate();
	    totalPairwise += pEval1.getF1()*(double)numMentions;
	    cnt += numMentions;
		}
		System.out.println("Cluster F1: " + (total / (double)cnt));
		System.out.println("Pairwise F1: " + (totalPairwise / (double)cnt));
	
	}

	// pretty gross that this has to happen .. does it?
	protected DenseVector getDenseVectorOf (int ri, Matrix2 matrix)
	{
		int dims[] = new int [2];
		matrix.getDimensions(dims);
		DenseVector vec = new DenseVector (dims[1]);
		for (int i=0; i < dims[1]; i++) {
	    vec.setValue (i, matrix.value(ri,i));
		}
		return vec;
	}

	public Matrix2 getFinalLambdas ()
	{
		return finalLambdas;
	}

	public void getUnNormalizedScores (Matrix2 lambdas, FeatureVector fv, double[] scores)
	{
		int defaultFeatureIndex = pipe.getDataAlphabet().size();
		assert (fv.getAlphabet ()
						== pipe.getDataAlphabet ());
		for (int li = 0; li < 2; li++) {
	    scores[li] = lambdas.value (li, defaultFeatureIndex)
									 + lambdas.rowDotProduct (li, fv, defaultFeatureIndex,null);
		}
	}

	protected void constructEdges (MappedGraph graph, Instance pair, Matrix2 lambdas)
	{
		MentionPair mentionPair = (MentionPair)pair.getSource(); // this needs to get stored in source
		Mention antecedent = mentionPair.getAntecedent();
		Mention referent =   mentionPair.getReferent();

		FeatureVector fv = (FeatureVector) pair.getData ();
		double scores[] = new double[2];
		getUnNormalizedScores (lambdas, fv, scores);

		if (lambdas == null)
	    System.out.println("LAMBDAS NULL");
		double edgeVal =  scores[yesIndex] - scores[noIndex];

		if (TUI.QUANTIZE_EDGE_VALUES) {
	    if (edgeVal >= 0.0)
				edgeVal = 1.0;
	    else
				edgeVal = -1.0;
		}
		try {
	    if ((antecedent != null) && (referent != null)) {
				//System.out.println("Adding edge: " + antecedent.getString() + ":" + referent.getString() + " with " + edgeVal);
				graph.addEdgeMap (antecedent, referent, edgeVal); // taking difference in weights for now
	    }
		} catch (Exception e) {e.printStackTrace();}
	}

}    
    
