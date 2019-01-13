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

package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.cluster;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe.iterator.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.classify.*;
import java.util.*;  

/** Trains the conditional clusterer to predict "yes" or "no" for a
 * NodeClusterPair; i.e. does this nodes belong in this cluster?*/
public class ConditionalClustererTrainer {

	Pipe p;
	Classifier classifier;
	ClassifierTrainer classifierTrainer;
	double threshold;
	
	public ConditionalClustererTrainer (Pipe _p, ClassifierTrainer _classifierTrainer, double _threshold) {
		this.p = _p;
		this.classifierTrainer = _classifierTrainer;
		this.threshold = _threshold;
	}
	
	public ConditionalClustererTrainer (Pipe _p, ClassifierTrainer _classifierTrainer) {
		this (_p, _classifierTrainer, 0.0);
	}

	public ConditionalClustererTrainer (Pipe _p, double _threshold) {
		this (_p, new MaxEntTrainer(), _threshold);
	}

	public ConditionalClustererTrainer (Pipe _p) { this(_p, new MaxEntTrainer());}

	
	public ConditionalClusterer train (AbstractPipeInputIterator instanceIterator, boolean useFeatureInduction) {
		InstanceList trainingList = new InstanceList (p);
		trainingList.add (instanceIterator);
		System.err.println ("Training on " + trainingList.size() + " instances with distribution " +
												trainingList.targetLabelDistribution() + " and " + trainingList.getPipe().getDataAlphabet().size() +
												" features");;
		InfoGain ig = new InfoGain (trainingList);
		for (int i=0; i < ig.numLocations(); i++)
			System.err.println ("InfoGain["+ig.getObjectAtRank(i)+"]="+ig.getValueAtRank(i));
		if (useFeatureInduction) {
			System.err.println ("Beginning Feature Induction");
			RankedFeatureVector.Factory gainFactory = new InfoGain.Factory();
	    FeatureInducer fi = new FeatureInducer (gainFactory,
																							trainingList, 20);
	    fi.induceFeaturesFor(trainingList, false, false);
		}

		classifier = classifierTrainer.train (trainingList);
		classifier.getInstancePipe().getDataAlphabet().stopGrowth();
		return new ConditionalClusterer(p, classifier, threshold);
	}
	
	public ConditionalClusterer train (Collection trainingCluster, boolean useFeatureInduction,
																		 boolean generateSampledInstances,
																		 double positiveInstanceRatio, int numberTrainingInstances) {
		return train (new NodeClusterPairIterator (trainingCluster, new Random(1),
																				positiveInstanceRatio, generateSampledInstances,
																				numberTrainingInstances), useFeatureInduction);
	}

	private LabelVector[] getLabelVectors (ArrayList classifications) {
		LabelVector[] lvs = new LabelVector[classifications.size()];
		for (int i=0; i < classifications.size(); i++) 
			lvs[i] = ((Classification)classifications.get(i)).getLabelVector();
		return lvs;
	}
}
