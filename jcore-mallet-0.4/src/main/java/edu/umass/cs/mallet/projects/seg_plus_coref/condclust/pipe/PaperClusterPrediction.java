/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.pipe;

import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Labeling;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.NodeClusterPair;
import edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types.VenuePaperCluster;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.PaperCitation;

import java.util.Collection;

/** Feature is the output of the paperClusterClassifier */
public class PaperClusterPrediction extends Pipe
{
	Classifier classifier;
	
	public PaperClusterPrediction (Classifier _classifier)	
	{
		this.classifier = _classifier;
	}

	public Instance pipe (Instance carrier) {
		VenuePaperCluster vpc = (VenuePaperCluster)carrier.getData();
		PaperCitation paper = vpc.getPaper();
		Collection paperCluster = vpc.getPaperCluster ();
		NodeClusterPair pair = new NodeClusterPair (paper, paperCluster);
		Instance inst = new Instance (pair, "unknown", pair, null);
		Labeling labeling = classifier.classify(inst).getLabelVector();
		double val = (labeling.labelAtLocation(0).equals("yes")) ?
								 (labeling.valueAtLocation(0) - labeling.valueAtLocation(1)) :
								 (labeling.valueAtLocation(1) - labeling.valueAtLocation(0));		
		if (val > 0.9)
			vpc.setFeatureValue ("PaperClusterAgreementHigh", 1.0);
		else if (val > 0.75)
			vpc.setFeatureValue ("PaperClusterAgreementMed", 1.0);
		else if (val > 0.5)
			vpc.setFeatureValue ("PaperClusterAgreementWeak", 1.0);
		else if (val > 0.3)
			vpc.setFeatureValue ("PaperClusterAgreementLow", 1.0);
		else
			vpc.setFeatureValue ("PaperClusterAgreementMin", 1.0);
		if (val > 0)
			vpc.setFeatureValue ("PaperAndClusterAgree", 1.0);
		return carrier;
	}
}
