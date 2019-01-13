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

package edu.umass.cs.mallet.projects.seg_plus_coref.anaphora;

import java.util.*;
import java.util.regex.*;

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.PropertyList;



public class AffixOfMentionPair extends Pipe
{

    public static final Pattern ABREV_PATTERN = Pattern.compile("[A-Za-z_0-9]\056");
    public static final Pattern ACRO_PATTERN = Pattern.compile("[A-Z]([^AaEeIiOoUuYy])*");

	public AffixOfMentionPair ()
	{
	}

    public static String getNormalizedMentionString (Mention men)
    {
	StringBuffer strBuf = new StringBuffer();
	MalletPhrase phrase = men.getMalletPhrase();
	Iterator preTermsIterator = phrase.getPreTerms().iterator();
	LinkedHashSet filteredPTs1 = new LinkedHashSet();
	LinkedHashSet filteredPTs = new LinkedHashSet();
	while (preTermsIterator.hasNext()) {
	    MalletPreTerm pt = (MalletPreTerm)preTermsIterator.next();
	    if (((pt != null) && (pt.getPartOfSpeech() != null)) &&
		((pt.getPartOfSpeech().equals("NNP")) ||
		 (pt.getPartOfSpeech().equals("NNPS"))))
		filteredPTs1.add(pt);
	}

	Iterator i2 = filteredPTs1.iterator();
	while (i2.hasNext()) {
	    MalletPreTerm p1 = (MalletPreTerm)i2.next();
	    if ((AffixOfMentionPair.validNNPString(p1)) ||
		(filteredPTs1.size() < 2))  // don't do this if phrase has a single token
		filteredPTs.add(p1);
	}
	Iterator i1 = filteredPTs.iterator();
	while (i1.hasNext()) {
	    MalletPreTerm pt = (MalletPreTerm)i1.next();
	    strBuf.append(pt.getString());
	}
	return strBuf.toString();
    }

    public static boolean validNNPString (MalletPreTerm pt)
    {
	
	String term = pt.getString();
	Matcher m1 = ABREV_PATTERN.matcher(term);
	Matcher m2 = ACRO_PATTERN.matcher(term);
	if (m1.matches() || m2.matches())
	    return false;
	else
	    return true;
    }

    public static boolean substringOf (String s1, String s2)
    {

	Pattern p1 = Pattern.compile(new String(".*").concat(s1).concat(".*"));
	Matcher m1 = p1.matcher(s2);
	return m1.matches();
    }

    

	private double mentionSubstringOf (Mention ant, Mention ref)
	{
	    String s1 = AffixOfMentionPair.getNormalizedMentionString (ant);
	    String s2 = AffixOfMentionPair.getNormalizedMentionString (ref);
	    if (substringOf (s2, s1))
		return 1.0;
	    else
		return 0.0;
	}

	private boolean affixOf (String s1, String s2)
	{
		return (s2.endsWith(s1) || s2.startsWith(s1));
	}

	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		if (!pair.nullPair())
		    pair.setFeatureValue (new String("affixString"), mentionSubstringOf(pair.getAntecedent(),pair.getReferent()));
		//System.out.println("Setting substring feature to: " + pair.getFeatureValue ("Substring"));
		return carrier;
	}

}
