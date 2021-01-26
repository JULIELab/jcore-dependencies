/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Ben Wellner
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.ClusterEvaluate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/*
	Yet another stochastic gradient decent algorithm.  Earlier code is so
	out-of-date that we restart here.
 */

public class SGDLearner {

	double decayRate = 0.9;
	int numEpochs = 20;
	Pipe pipe = null;
	Collection keyPart = null;
	Matrix2 parameters = null;
	int alphabetSize = 0;
	
	// takes a clusterer that performs inference
	public SGDLearner (double decayRate, int numEpochs, Pipe p, Collection keyPart) {
		this.alphabetSize = p.getDataAlphabet().size();
		this.decayRate = decayRate;
		this.numEpochs = numEpochs;
		this.pipe = p;
		this.keyPart = keyPart;
	}

	public Collection test (InstanceList testPairs, List tMentions) {
		CorefClusterAdv cl = new CorefClusterAdv (0.0, this.parameters, alphabetSize);
		return cl.clusterMentions(testPairs, tMentions);
	}

	public void train (InstanceList instPairs, List mentions) {

		int defaultFeatureIndex = alphabetSize;
		System.out.println("Feature vector size: " + defaultFeatureIndex);

		int numFeatures = defaultFeatureIndex+1;
		int numInstances = instPairs.size();

		Matrix2 constraints = new Matrix2(2, numFeatures);
		Matrix2 expectations = new Matrix2(2, numFeatures);
		Matrix2 lambdas = new Matrix2(2, numFeatures);


		Matrix2 expectationsSum = new Matrix2(2, numFeatures);

		Iterator i1 = instPairs.iterator();
		Collection curPart;

		// set up constriants here
		while (i1.hasNext()) {
			Instance mentionPair = (Instance)i1.next();
			FeatureVector vec = (FeatureVector) mentionPair.getData();
			NodePair pair = (NodePair)mentionPair.getSource();
			boolean cl = pair.getIdRel();  // get true class (yes or no)
			int ind;
			if (cl) ind = 1; else ind = 0;
			constraints.rowPlusEquals (ind, vec, 1.0);
			constraints.plusEquals (ind, defaultFeatureIndex, 1.0); // dummy
		}
		System.out.println("Constraints: ");
		constraints.print();
		ClusterEvaluate evaluator = null;
		CorefClusterAdv cl = null;

		for (int epoch = 0; epoch < numEpochs; epoch++) {
			System.out.println("-> EPOCH " + epoch);
			
			cl = new CorefClusterAdv (0.0, lambdas, defaultFeatureIndex);
			cl.setKeyPartitioning (keyPart); // set key
			curPart = cl.clusterMentions (instPairs, mentions);
			evaluator = new ClusterEvaluate (keyPart, curPart);
			evaluator.evaluate();
			//evaluator.printVerbose();
			System.out.println(" --> F1: " + evaluator.getF1());
			
			Iterator i2 = instPairs.iterator();
			while (i2.hasNext()) {
				Instance inst = (Instance)i2.next();
				FeatureVector v = (FeatureVector)inst.getData();
				NodePair np = (NodePair)inst.getSource();
				Citation c1 = (Citation)np.getObject1();
				Citation c2 = (Citation)np.getObject2();
				int ind;
				if (cl.inSameCluster(curPart, c1, c2)) ind = 1; else ind = 0;
				expectations.rowPlusEquals (ind, v, 1.0);
				expectations.plusEquals (ind, defaultFeatureIndex, 1.0);
			}

			System.out.println("Expectations: ");
			expectations.print();
			expectations.timesEquals (-1.0);
			DenseVector v0 = getDenseVectorOf(0, constraints);
			DenseVector v1 = getDenseVectorOf(1, constraints);
			System.out.println("Constraint vectors: ");
			v0.print();
			v1.print();
			// do addition in place
			expectations.rowPlusEquals (0, v0, 1.0);
			expectations.rowPlusEquals (1, v1, 1.0);
			DenseVector e0 = getDenseVectorOf(0, expectations);
			DenseVector e1 = getDenseVectorOf(1, expectations);
			// decay the update
			e0.timesEquals( Math.pow(decayRate, epoch));
			e1.timesEquals( Math.pow(decayRate, epoch));

			System.out.println("Adjustment: ");
			e0.print();
			e1.print();
			// modify the parameters according to gradient
			lambdas.rowPlusEquals (0, e0, 1.0);
			lambdas.rowPlusEquals (1, e1, 1.0);
			System.out.println("Parameters at iteration: " + epoch);
			lambdas.print();
			expectations.timesEquals (0.0); // need to reset expectation

		}
		this.parameters = lambdas; // set parameters to final lambdas
		System.out.println("Final lambdas:");
		lambdas.print();
		
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


}
