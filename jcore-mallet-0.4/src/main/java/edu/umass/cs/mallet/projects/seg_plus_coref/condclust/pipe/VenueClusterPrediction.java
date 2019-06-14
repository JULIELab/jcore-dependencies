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
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.VenueCitation;

import java.util.Collection;

/** Feature is the output of the paperClusterClassifier */
public class VenueClusterPrediction extends Pipe
{
	Classifier classifier;
	
	public VenueClusterPrediction (Classifier _classifier)	
	{
		this.classifier = _classifier;
	}

	public Instance pipe (Instance carrier) {
		VenuePaperCluster vpc = (VenuePaperCluster)carrier.getData();
		VenueCitation venue = vpc.getVenue();
		Collection venueCluster = vpc.getVenueCluster ();
		NodeClusterPair pair = new NodeClusterPair (venue, venueCluster);
		Instance inst = new Instance (pair, "unknown", pair, null);
		Labeling labeling = classifier.classify(inst).getLabelVector();
		double val = (labeling.labelAtLocation(0).equals("yes")) ?
								 (labeling.valueAtLocation(0) - labeling.valueAtLocation(1)) :
								 (labeling.valueAtLocation(1) - labeling.valueAtLocation(0));		
		if (val > 0.9)
			vpc.setFeatureValue ("VenueClusterAgreementHigh", 1.0);
		else if (val > 0.75)
			vpc.setFeatureValue ("VenueClusterAgreementMed", 1.0);
		else if (val > 0.5)
			vpc.setFeatureValue ("VenueClusterAgreementWeak", 1.0);
		else if (val > 0.3)
			vpc.setFeatureValue ("VenueClusterAgreementLow", 1.0);
		else
			vpc.setFeatureValue ("VenueClusterAgreementMin", 1.0);
		if (val > 0)
			vpc.setFeatureValue ("VenueAndClusterAgree", 1.0);
		return carrier;
	}
}
