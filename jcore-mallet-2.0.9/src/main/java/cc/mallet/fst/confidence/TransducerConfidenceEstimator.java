/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

/** 
		@author Aron Culotta <a href="mailto:culotta@cs.umass.edu">culotta@cs.umass.edu</a>
*/

package cc.mallet.fst.confidence;


import cc.mallet.fst.Segment;
import cc.mallet.fst.SumLatticeDefault;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.Noop;
import cc.mallet.pipe.iterator.SegmentIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.MalletLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Abstract class that estimates the confidence of a {@link Segment}
 * extracted by a {@link Transducer}.
 */
abstract public class TransducerConfidenceEstimator implements Serializable
{
	private static Logger logger = MalletLogger.getLogger(TransducerConfidenceEstimator.class.getName());

	protected Transducer model; // the trained Transducer which
															// performed the extractions.

	java.util.Vector segmentConfidences; 

	public TransducerConfidenceEstimator (Transducer model) {
		this.model = model;
	}
	
	/**
		 Calculates the confidence in the tagging of a {@link Segment}.
	 */
	public double estimateConfidenceFor (Segment segment) {
		return estimateConfidenceFor (segment, null);
	}

	abstract public double estimateConfidenceFor (Segment segment, SumLatticeDefault lattice);

	public java.util.Vector getSegmentConfidences () {return this.segmentConfidences;}

	/**
		 Ranks all {@link Segment}s in this {@link InstanceList} by
		 confidence estimate.
		 @param ilist list of segmentation instances
		 @param startTags represent the labels for the start states (B-)
		 of all segments
		 @param continueTags represent the labels for the continue state
		 (I-) of all segments
		 @return array of {@link Segment}s ordered by non-decreasing
		 confidence scores, as calculated by <code>estimateConfidenceFor</code>
	 */
	public Segment[] rankSegmentsByConfidence (InstanceList ilist, Object[] startTags,
																						 Object[] continueTags) {
		ArrayList segmentList = new ArrayList ();
		SegmentIterator iter = new SegmentIterator (this.model, ilist, startTags, continueTags);			
		if (this.segmentConfidences == null)
			segmentConfidences = new java.util.Vector ();
		while (iter.hasNext ()) {
			Segment segment = (Segment) iter.nextSegment ();
			double confidence = estimateConfidenceFor (segment);
			segment.setConfidence (confidence);
			logger.fine ("confidence=" + segment.getConfidence() + " for segment\n"
									 + segment.sequenceToString() + "\n");
			segmentList.add (segment);
		}
		Collections.sort (segmentList);
		Segment[] ret = new Segment[1];
		ret = (Segment[]) segmentList.toArray (ret);
		return ret;
	}

	/**
		 ranks the segments in one {@link Instance}
		 @param instance instances to be segmented
		 @param startTags represent the labels for the start states (e.g. B-)
		 of all segments
		 @param continueTags represent the labels for the continue state
		 (e.g. I-) of all segments
		 @return array of {@link Segment}s ordered by non-decreasing
		 confidence scores, as calculated by <code>estimateConfidenceFor</code>
	 */
	public Segment[] rankSegmentsByConfidence (Instance instance, Object[] startTags,
																						 Object[] continueTags) {
		InstanceList ilist = new InstanceList (new Noop(instance.getDataAlphabet(),instance.getTargetAlphabet()));
		ilist.add (instance);
		return rankSegmentsByConfidence (ilist, startTags, continueTags);
	}

        public Transducer getTransducer() { return this.model; }
}
