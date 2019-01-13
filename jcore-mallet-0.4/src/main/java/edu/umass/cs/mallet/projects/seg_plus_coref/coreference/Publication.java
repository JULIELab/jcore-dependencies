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

import com.wcohen.secondstring.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.graphs.*;
import salvo.jesus.graph.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.util.Arrays;
import java.lang.*;
import java.io.*;

/**
	 Objects of this class represent underlying publications (in the citation
	 domain).  Multiple citations (mentions) may belong to a publication.  A
	 publication also keeps track of the canonical attribute values of the
	 publication.
 */

public class Publication extends Citation {


	Collection citations; // citations representing the publication.  citations
	private static StringDistance dist = new JaroWinkler();
	// are in fact objects of class Node

	public Publication () {}

	// a common initial case where each publication has one citation
	public Publication (Citation citation) {
		// basically make a copy of this
		super (citation.getString(), citation.getLabel(), citation.getIndex());
		citations = new LinkedHashSet ();
		citations.add(citation);

	}

	public Collection getCitations () {
		return citations;
	}

	public boolean hasCitation (Citation c) {
		return citations.contains(c);
	}

	// this is the method that does the actual work
	// it must add a new citation and re-normalize the publication attribute
	// values appropriately
	public void addNewCitation (Citation citation) {
		citations.add(citation);
		//renormalizeFields (citation);
	}

	public void mergeNewPublication (Publication pub) {
		Collection cits = pub.getCitations();
		// add citations to this one
		Iterator i1 = cits.iterator();
		while (i1.hasNext()) {
	    citations.add((Citation)i1.next());
		}
		renormalizeFields ((Citation)pub);  // treat pub as citation and
		// renormalize fields
	}

	public void renormalizeFields(Citation citation) {
		renormalizeFields2();
	}

	// first pass, just take longest field as canonical field value
	private void renormalizeFields1 (Citation citation) {

		String [] allFields = citation.getPossibleFields();
		for (int i=0; i<allFields.length; i++) {
	    String fName = (String)allFields[i];
	    String fVal = citation.getField(fName);
	    String thisVal = this.getField(fName);
	    if (thisVal == null || (fVal.length() > thisVal.length()))
				setField (fName, fVal);
		}

		//also set the global string of this
		if (citation.getString().length() > this.getString().length())
	    this.setString(citation.getString());
	}

	// for each field, sets the Publication field to that value
	// to the most frequently occuring value in the Citations
	// NOTE: performs a soft-match
	private void renormalizeFields2() {
		String[] fields = getPossibleFields();
		Collection cits = getCitations();
		Object[] citations = cits.toArray();
		String [] vals = new String[citations.length];   // use array because it's fast
		for (int i = 0; i < fields.length; i++) {
			String fName = fields[i];
			Arrays.fill(vals, "");
			for (int j = 0; j < vals.length; j++) {
				Citation citation = (Citation)citations[j];
				String fVal = citation.getField(fName);
				vals[j] = fVal == null? "" : fVal;
				//System.out.println("Field: " + fName + " citation: " + j + " value= " + fVal);
			}
			String fVal = findBestVal(vals);
			System.out.println("Best Choice for Field: " + fName + " value: " + fVal);
			setField (fName, fVal);
		}
		String [] fulls = new String[citations.length];   // use array because
		Arrays.fill(vals, "");
		for (int j = 0; j < vals.length; j++) {
			Citation citation = (Citation)citations[j];
			String fVal = citation.getString(); // get full string
			fulls[j] = fVal == null? "" : fVal;
		}
		String bfull = findBestVal(fulls);
		this.setString(bfull);
	}

	private String findBestVal(String[] vals) {
		if (vals.length == 0) {
			return null;
		}
		double maxScore = 0;
		String bestVal = "";
		double totalScore;
		for (int i = 0; i < vals.length; i++) {
			String val = vals[i];
			totalScore = 0;
			if (val.equals("")) {
				continue;
			}
			for (int j = 0; j < vals.length; j++) {
				String val2 = vals[j];
				totalScore += dist.score(val, val2);
				System.out.println("val='" + val + "' val2= '" + val2 + "' score= " + dist.score(val, val2));
				//System.out.println(dist.explainScore(val, val2));
			}
			//System.out.println("val='" + val + "' totalScore=" + totalScore);
			if (totalScore > maxScore) {
				bestVal = val;
			}
		}
		return bestVal;
	}

}
