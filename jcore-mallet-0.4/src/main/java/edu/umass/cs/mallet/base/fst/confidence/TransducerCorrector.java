/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

/** 
		@author Aron Culotta <a href="mailto:culotta@cs.umass.edu">culotta@cs.umass.edu</a>
*/

package edu.umass.cs.mallet.base.fst.confidence;

import edu.umass.cs.mallet.base.fst.Segment;
import edu.umass.cs.mallet.base.fst.Transducer;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.Labeling;

import java.util.ArrayList;

/**
 *
 * Interface for transducerCorrectors, which correct a subset of the
 * {@link Segment}s produced by a {@link Transducer}. It's primary
 * purpose is to find the {@link Segment}s that the {@link Transducer}
 * is least confident in and correct those using the true {@link
 * Labeling} (<code>correctLeastConfidenceSegments</code>).
 */
public interface TransducerCorrector 
{
		
	public ArrayList correctLeastConfidentSegments (InstanceList ilist, Object[] startTags,
																										Object[] continueTags);
}
