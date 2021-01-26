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

/** A Citation object which is treated as an author */
public class AuthorCitation extends Citation {
	private int authorNumber; // which author in a citation this object refers to

	public AuthorCitation (String s, String label, int v) {
		super (s, label, v);
	}
	
	public AuthorCitation (String s, Object label, int index, IEInterface ieInterface, int n, int nthToUse) {
		super (s, label, index, ieInterface, n, nthToUse);
	}
		
	public void setAuthorNumber (int i) { this.authorNumber = i; }
	public int getAuthorNumber () { return authorNumber; }
}
