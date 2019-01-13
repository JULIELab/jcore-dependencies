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
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.types.*;

public class PairWiseEvaluator
{

	double     score;
	Set keys;
	Set responses;
	
	public PairWiseEvaluator (Set s1, Set s2)
	{
		keys = s1;
		responses = s2;
	}

	public double score ()
	{
		return score;
	}

	public double evaluate ()
	{
		double recall = 0.0;
		double precision = 0.0;
		int s1 = keys.size();
		int s2 = responses.size();
		int intersection = 0;
		Iterator i = keys.iterator();

		System.out.println("Set 1: " + s1);
		System.out.println("Set 2: " + s2);
		while (i.hasNext()) {
			MentionPair obj = (MentionPair)i.next();
			if (responses.contains(obj)) {
				intersection++;
			}
		}
		printEm();
		System.out.println("Size of intersection: " + intersection);
		if (s1 > 0)
			recall = (double)intersection/(double)s1;
		if (s2 > 0)
			precision = (double)intersection/(double)s2;
		System.out.println("recall " + recall + " precision: " + precision);
		double f1 = recall + precision;
		double denom = 2.0 * recall * precision;
		double fmeasure = (2 * recall * precision)/(recall + precision);
		System.out.println("f-measure: " + fmeasure);
		return fmeasure;
	}

	private void printEm ()
	{
		Iterator i1 = keys.iterator();
		Iterator i2 = responses.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			MentionPair p1 = (MentionPair)i1.next();
			MentionPair p2 = (MentionPair)i2.next();
			/*			
			System.out.println(p1.getAntecedent().getString() + " :: " + p1.getReferent().getString() +
			"      " + p2.getAntecedent().getString() + " :: " +
			p2.getReferent().getString()); */
		}
	}

}
