/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Aron Culotta
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;

/** A Citation object which is treated as a Paper */
public class PaperCitation extends Citation {
	
	public PaperCitation (String s, String label, int v) {
		super (s, label, v);
	}

	public PaperCitation (String s, Object label, int index, IEInterface ieInterface, int n, int nthToUse) {
		super (s, label, index, ieInterface, n, nthToUse);
	}
}
